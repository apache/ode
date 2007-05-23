/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.o.OElementVarType;
import org.apache.ode.bpel.o.OExpressionLanguage;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.bpel.runtime.ExpressionLanguageRuntimeRegistry;
import org.apache.ode.bpel.runtime.PROCESS;
import org.apache.ode.bpel.runtime.PropertyAliasEvaluationContext;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.jacob.soup.ReplacementMap;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Entry point into the runtime of a BPEL process.
 * 
 * @author mszefler
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class BpelProcess {
    static final Log __log = LogFactory.getLog(BpelProcess.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private volatile Map<OPartnerLink, PartnerLinkPartnerRoleImpl> _partnerRoles;
    private volatile Map<OPartnerLink, PartnerLinkMyRoleImpl> _myRoles;
    /** Mapping from {"Service Name" (QNAME) / port} to a myrole. */
    private volatile Map<Endpoint, PartnerLinkMyRoleImpl> _endpointToMyRoleMap;

    // Backup hashmaps to keep initial endpoints handy after dehydration
    private Map<Endpoint,EndpointReference> _myEprs =
            new HashMap<Endpoint, EndpointReference>();
    private Map<Endpoint,EndpointReference> _partnerEprs =
            new HashMap<Endpoint, EndpointReference>();
    private Map<Endpoint,PartnerRoleChannel> _partnerChannels =
            new HashMap<Endpoint, PartnerRoleChannel>();

    final QName _pid;
    private volatile OProcess _oprocess;
    // Has the process already been hydrated before?
    private boolean _hydratedOnce = false;
    /** Last time the process was used. */
    private volatile long _lastUsed;

    BpelEngineImpl _engine;
    DebuggerSupport _debugger;
    ExpressionLanguageRuntimeRegistry _expLangRuntimeRegistry;
    private ReplacementMap _replacementMap;
    final ProcessConf _pconf;
    // Notifying the server when a process hydrates
    private ProcessLifecycleCallback _lifeCallback;
    /** {@link MessageExchangeInterceptor}s registered for this process. */
    private final List<MessageExchangeInterceptor> _mexInterceptors = new ArrayList<MessageExchangeInterceptor>();

    public BpelProcess(ProcessConf conf, BpelEventListener debugger, ProcessLifecycleCallback lifeCallback) {
        _pid = conf.getProcessId();
        _pconf = conf;
        _lifeCallback = lifeCallback;
    }

    public String toString() {
        return "BpelProcess[" + _pid + "]";
    }

    public void recoverActivity(ProcessInstanceDAO instanceDAO, String channel, long activityId,
                                String action, FaultData fault) {
        if (__log.isDebugEnabled())
            __log.debug("Recovering activity in process " + instanceDAO.getInstanceId() + " with action " + action);
        markused();
        BpelRuntimeContextImpl processInstance = createRuntimeContext(instanceDAO, null, null);
        processInstance.recoverActivity(channel, activityId, action, fault);
    }

    static String generateMessageExchangeIdentifier(String partnerlinkName, String operationName) {
        StringBuffer sb = new StringBuffer(partnerlinkName);
        sb.append('.');
        sb.append(operationName);
        return sb.toString();
    }

    /**
     * Entry point for message exchanges aimed at the my role.
     * @param mex
     */
    void invokeProcess(MyRoleMessageExchangeImpl mex) {
        PartnerLinkMyRoleImpl target = getMyRoleForService(mex.getServiceName());
        if (target == null) {
            String errmsg = __msgs.msgMyRoleRoutingFailure(mex.getMessageExchangeId());
            __log.error(errmsg);
            mex.setFailure(MessageExchange.FailureType.UNKNOWN_ENDPOINT, errmsg, null);
            return;
        }

        mex.getDAO().setProcess(getProcessDAO());

        if (!processInterceptors(mex, InterceptorInvoker.__onProcessInvoked)) {
            __log.debug("Aborting processing of mex " + mex + " due to interceptors.");
            return;
        }

        markused();
        target.invokeMyRole(mex);
        markused();
    }

    private PartnerLinkMyRoleImpl getMyRoleForService(QName serviceName) {
        for (Map.Entry<Endpoint, PartnerLinkMyRoleImpl> e : getEndpointToMyRoleMap().entrySet()) {
            if (e.getKey().serviceName.equals(serviceName))
                return e.getValue();
        }
        return null;
    }

    void initMyRoleMex(MyRoleMessageExchangeImpl mex) {
        markused();
        PartnerLinkMyRoleImpl target = null;
        for (Endpoint endpoint : getEndpointToMyRoleMap().keySet()) {
            if (endpoint.serviceName.equals(mex.getServiceName()))
                target = getEndpointToMyRoleMap().get(endpoint);
        }
        if (target != null) {
            mex.setPortOp(target._plinkDef.myRolePortType, target._plinkDef.getMyRoleOperation(mex.getOperationName()));
        } else {
            __log.warn("Couldn't find endpoint from service " + mex.getServiceName()
                    + " when initializing a myRole mex.");
        }
    }

    /**
     * Extract the value of a BPEL property from a BPEL messsage variable.
     * @param msgData message variable data
     * @param alias alias to apply
     * @param target description of the data (for error logging only)
     * @return value of the property
     * @throws FaultException
     */
    String extractProperty(Element msgData, OProcess.OPropertyAlias alias, String target) throws FaultException {
        markused();
        PropertyAliasEvaluationContext ectx = new PropertyAliasEvaluationContext(msgData, alias);
        Node lValue = ectx.getRootNode();

        if (alias.location != null)
            try {
                lValue = _expLangRuntimeRegistry.evaluateNode(alias.location, ectx);
            } catch (EvaluationException ec) {
                throw new FaultException(getOProcess().constants.qnSelectionFailure, alias.getDescription());
            }

        if (lValue == null) {
            String errmsg = __msgs.msgPropertyAliasReturnedNullSet(alias.getDescription(), target);
            if (__log.isErrorEnabled()) {
                __log.error(errmsg);
            }
            throw new FaultException(getOProcess().constants.qnSelectionFailure, errmsg);
        }

        if (lValue.getNodeType() == Node.ELEMENT_NODE) {
            // This is a bit hokey, we concatenate all the children's values; we
            // really should be checking to make sure that we are only dealing
            // with
            // text and attribute nodes.
            StringBuffer val = new StringBuffer();
            NodeList nl = lValue.getChildNodes();
            for (int i = 0; i < nl.getLength(); ++i) {
                Node n = nl.item(i);
                val.append(n.getNodeValue());
            }
            return val.toString();
        } else if (lValue.getNodeType() == Node.TEXT_NODE) {
            return ((Text) lValue).getWholeText();
        } else
            return null;
    }

    /**
     * Get the element name for a given WSDL part. If the part is an
     * <em>element</em> part, the name of that element is returned. If the
     * part is an XML schema typed part, then the name of the part is returned
     * in the null namespace.
     * @param part WSDL {@link javax.wsdl.Part}
     * @return name of element containing said part
     */
    static QName getElementNameForPart(OMessageVarType.Part part) {
        return (part.type instanceof OElementVarType) ? ((OElementVarType) part.type).elementType
                : new QName(null, part.name);
    }

    /**
     * Process the message-exchange interceptors.
     * 
     * @param mex
     *            message exchange
     * @return <code>true</code> if execution should continue,
     *         <code>false</code> otherwise
     */
    boolean processInterceptors(MyRoleMessageExchangeImpl mex, InterceptorInvoker invoker) {
        InterceptorContextImpl ictx = new InterceptorContextImpl(getEngine()._contexts.dao.getConnection(),
                getProcessDAO(), _pconf);

        for (MessageExchangeInterceptor i : _mexInterceptors)
            if (!mex.processInterceptor(i, mex, ictx, invoker))
                return false;
        for (MessageExchangeInterceptor i : getEngine().getGlobalInterceptors())
            if (!mex.processInterceptor(i, mex, ictx, invoker))
                return false;

        return true;
    }


    /**
     * @see org.apache.ode.bpel.engine.BpelProcess#handleWorkEvent(java.util.Map<java.lang.String,java.lang.Object>)
     */
    public void handleWorkEvent(Map<String, Object> jobData) {
        markused();

        if (__log.isDebugEnabled()) {
            __log.debug(ObjectPrinter.stringifyMethodEnter("handleWorkEvent", new Object[] { "jobData", jobData }));
        }

        WorkEvent we = new WorkEvent(jobData);

        // Process level events
        if (we.getType().equals(WorkEvent.Type.INVOKE_INTERNAL)) {
            if (__log.isDebugEnabled()) {
                __log.debug("InvokeInternal event for mexid " + we.getMexId());
            }
            MyRoleMessageExchangeImpl mex = (MyRoleMessageExchangeImpl) getEngine().getMessageExchange(we.getMexId());
            if (mex == null) throw new ContextException("Unable to find MEX " + we.getMexId());
            invokeProcess(mex);
        } else {
            // Instance level events
            ProcessInstanceDAO procInstance = getProcessDAO().getInstance(we.getIID());
            if (procInstance == null) {
                if (__log.isDebugEnabled()) {
                    __log.debug("handleWorkEvent: no ProcessInstance found with iid " + we.getIID() + "; ignoring.");
                }
                return;
            }

            BpelRuntimeContextImpl processInstance = createRuntimeContext(procInstance, null, null);
            switch (we.getType()) {
            case TIMER:
                if (__log.isDebugEnabled()) {
                    __log.debug("handleWorkEvent: TimerWork event for process instance " + processInstance);
                }
                processInstance.timerEvent(we.getChannel());
                break;
            case RESUME:
                if (__log.isDebugEnabled()) {
                    __log.debug("handleWorkEvent: ResumeWork event for iid " + we.getIID());
                }
                processInstance.execute();
                break;
            case INVOKE_RESPONSE:
                if (__log.isDebugEnabled()) {
                    __log.debug("InvokeResponse event for iid " + we.getIID());
                }
                processInstance.invocationResponse(we.getMexId(), we.getChannel());
                processInstance.execute();
                break;
            case MATCHER:
                if (__log.isDebugEnabled()) {
                    __log.debug("Matcher event for iid " + we.getIID());
                }
                processInstance.matcherEvent(we.getCorrelatorId(), we.getCorrelationKey());
            }
        }
    }

    private void setRoles(OProcess oprocess) {
        _partnerRoles = new HashMap<OPartnerLink, PartnerLinkPartnerRoleImpl>();
        _myRoles = new HashMap<OPartnerLink, PartnerLinkMyRoleImpl>();
        _endpointToMyRoleMap = new HashMap<Endpoint, PartnerLinkMyRoleImpl>();

        // Create myRole endpoint name mapping (from deployment descriptor)
        HashMap<OPartnerLink, Endpoint> myRoleEndpoints = new HashMap<OPartnerLink, Endpoint>();
        for (Map.Entry<String, Endpoint> provide : _pconf.getProvideEndpoints().entrySet()) {
            OPartnerLink plink = oprocess.getPartnerLink(provide.getKey());
            if (plink == null) {
                String errmsg = "Error in deployment descriptor for process " + _pid
                        + "; reference to unknown partner link " + provide.getKey();
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }
            myRoleEndpoints.put(plink, provide.getValue());
        }

        // Create partnerRole initial value mapping
        for (Map.Entry<String, Endpoint> invoke : _pconf.getInvokeEndpoints().entrySet()) {
            OPartnerLink plink = oprocess.getPartnerLink(invoke.getKey());
            if (plink == null) {
                String errmsg = "Error in deployment descriptor for process " + _pid
                        + "; reference to unknown partner link " + invoke.getKey();
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }
            __log.debug("Processing <invoke> element for process " + _pid + ": partnerlink " + invoke.getKey()
                    + " --> " + invoke.getValue());
        }

        for (OPartnerLink pl : oprocess.getAllPartnerLinks()) {
            if (pl.hasMyRole()) {
                Endpoint endpoint = myRoleEndpoints.get(pl);
                if (endpoint == null)
                    throw new IllegalArgumentException("No service name for myRole plink " + pl.getName());
                PartnerLinkMyRoleImpl myRole = new PartnerLinkMyRoleImpl(this, pl, endpoint);
                _myRoles.put(pl, myRole);
                _endpointToMyRoleMap.put(endpoint, myRole);
            }

            if (pl.hasPartnerRole()) {
                PartnerLinkPartnerRoleImpl partnerRole = new PartnerLinkPartnerRoleImpl(
                        this, pl, _pconf.getInvokeEndpoints().get(pl.getName()));
                _partnerRoles.put(pl, partnerRole);
            }
        }
    }

    ProcessDAO getProcessDAO() {
        return _pconf.isTransient() ? getEngine()._contexts.inMemDao.getConnection().getProcess(_pid)
                : getEngine()._contexts.dao.getConnection().getProcess(_pid);
    }

    static String genCorrelatorId(OPartnerLink plink, String opName) {
        return plink.getId() + "." + opName;
    }

    /**
     * De-serialize the compiled process representation from a stream.
     *
     * @param is
     *            input stream
     * @return process information from configuration database
     */
    private OProcess deserializeCompiledProcess(InputStream is) throws Exception {
        OProcess compiledProcess;
        Serializer ofh = new Serializer(is);
        compiledProcess = ofh.readOProcess();
        return compiledProcess;
    }

    /**
     * Get all the services that are implemented by this process.
     * 
     * @return list of qualified names corresponding to the myroles.
     */
    public Set<Endpoint> getServiceNames() {
        Set<Endpoint> endpoints = new HashSet<Endpoint>();
        for (Endpoint provide : _pconf.getProvideEndpoints().values()) {
            endpoints.add(provide);
        }
        return endpoints;
    }

    void activate(BpelEngineImpl engine) {
        _engine = engine;
        _debugger = new DebuggerSupport(this);

        __log.debug("Activating " + _pid);
        // Activate all the my-role endpoints.
        for (Map.Entry<String, Endpoint> entry : _pconf.getProvideEndpoints().entrySet()) {
            EndpointReference initialEPR = getEngine()._contexts
                    .bindingContext.activateMyRoleEndpoint(_pid, entry.getValue());
            __log.debug("Activated " + _pid + " myrole " + entry.getKey() + ": EPR is " + initialEPR);
            _myEprs.put(entry.getValue(), initialEPR);
        }
        __log.debug("Activated " + _pid);

        markused();
    }

    void deactivate() {
        // Deactivate all the my-role endpoints.
        for (Endpoint endpoint : _myEprs.keySet())
            getEngine()._contexts.bindingContext.deactivateMyRoleEndpoint(endpoint);

         // TODO Deactivate all the partner-role channels
    }

    EndpointReference getInitialPartnerRoleEPR(OPartnerLink link) {
        PartnerLinkPartnerRoleImpl prole = getPartnerRoles().get(link);
        if (prole == null)
            throw new IllegalStateException("Unknown partner link " + link);
        return prole.getInitialEPR();
    }

    Endpoint getInitialPartnerRoleEndpoint(OPartnerLink link) {
        PartnerLinkPartnerRoleImpl prole = getPartnerRoles().get(link);
        if (prole == null)
            throw new IllegalStateException("Unknown partner link " + link);
        return prole._initialPartner;
    }

    EndpointReference getInitialMyRoleEPR(OPartnerLink link) {
        PartnerLinkMyRoleImpl myRole = getMyRoles().get(link);
        if (myRole == null)
            throw new IllegalStateException("Unknown partner link " + link);
        return myRole.getInitialEPR();
    }

    QName getPID() {
        return _pid;
    }

    PartnerRoleChannel getPartnerRoleChannel(OPartnerLink partnerLink) {
        PartnerLinkPartnerRoleImpl prole = getPartnerRoles().get(partnerLink);
        if (prole == null)
            throw new IllegalStateException("Unknown partner link " + partnerLink);
        return prole._channel;
    }

    public void saveEvent(ProcessInstanceEvent event, ProcessInstanceDAO instanceDao) {
        markused();
        List<String> scopeNames = null;
        if (event instanceof ScopeEvent) {
            scopeNames = ((ScopeEvent) event).getParentScopesNames();
        }

        boolean enabled = _pconf.isEventEnabled(scopeNames, event.getType());
        if (enabled) {
            if (instanceDao != null) saveInstanceEvent(event, instanceDao);
            else __log.debug("Couldn't find instance to save event, no event generated!");
        }
    }

    void saveInstanceEvent(ProcessInstanceEvent event, ProcessInstanceDAO instanceDao) {
        instanceDao.insertBpelEvent(event);
    }

    void dehydrate() {
        _oprocess = null;
        _partnerRoles = null;
        _myRoles = null;
        _endpointToMyRoleMap = null;
        _replacementMap = null;
        _expLangRuntimeRegistry = null;
    }

    private void hydrate() {
        markused();
        __log.debug("Rehydrating process " + _pconf.getProcessId());
        try {
            _oprocess = deserializeCompiledProcess(_pconf.getCBPInputStream());
        } catch (Exception e) {
            String errmsg = "Error reloading compiled process " + _pid + "; the file appears to be corrupted.";
            __log.error(errmsg);
            throw new BpelEngineException(errmsg, e);
        }

        _replacementMap = new ReplacementMapImpl(_oprocess);

        // Create an expression language registry for this process
        ExpressionLanguageRuntimeRegistry elangRegistry = new ExpressionLanguageRuntimeRegistry();
        for (OExpressionLanguage elang : _oprocess.expressionLanguages) {
            try {
                elangRegistry.registerRuntime(elang);
            } catch (ConfigurationException e) {
                String msg = __msgs.msgExpLangRegistrationError(elang.expressionLanguageUri, elang.properties);
                __log.error(msg, e);
                throw new BpelEngineException(msg, e);
            }
        }
        _expLangRuntimeRegistry = elangRegistry;

        setRoles(getOProcess());

        if (!_hydratedOnce) {
            for (PartnerLinkPartnerRoleImpl prole : getPartnerRoles().values()) {
                PartnerRoleChannel channel = getEngine()._contexts.bindingContext.createPartnerRoleChannel(_pid,
                        prole._plinkDef.partnerRolePortType, prole._initialPartner);
                prole._channel = channel;
                _partnerChannels.put(prole._initialPartner, prole._channel);
                EndpointReference epr = channel.getInitialEndpointReference();
                if (epr != null) {
                    prole._initialEPR = epr;
                    _partnerEprs.put(prole._initialPartner, epr);
                }
                __log.debug("Activated " + _pid + " partnerrole " + prole.getPartnerLinkName() + ": EPR is "
                        + prole._initialEPR);
            }
            _hydratedOnce = true;
        }

        for (PartnerLinkMyRoleImpl myrole : getMyRoles().values()) {
            myrole._initialEPR = _myEprs.get(myrole._endpoint);
        }

        for (PartnerLinkPartnerRoleImpl prole : getPartnerRoles().values()) {
            prole._channel = _partnerChannels.get(prole._initialPartner);
            if (_partnerEprs.get(prole._initialPartner) != null) {
                prole._initialEPR = _partnerEprs.get(prole._initialPartner);
            }
        }

        _lifeCallback.hydrated(this);
    }

    OProcess getOProcess() {
        if (_oprocess == null) hydrate();
        return _oprocess;
    }

    public Map<OPartnerLink, PartnerLinkMyRoleImpl> getMyRoles() {
        if (_myRoles == null) hydrate();
        return _myRoles;
    }

    public Map<OPartnerLink, PartnerLinkPartnerRoleImpl> getPartnerRoles() {
        if (_partnerRoles == null) hydrate();
        return _partnerRoles;
    }

    private Map<Endpoint, PartnerLinkMyRoleImpl> getEndpointToMyRoleMap() {
        if (_endpointToMyRoleMap == null) hydrate();
        return _endpointToMyRoleMap;
    }

    public ReplacementMap getReplacementMap() {
        if (_replacementMap == null) hydrate();
        assert _replacementMap != null;
        return _replacementMap;
    }

    BpelEngineImpl getEngine() {
        return _engine;
    }

    public boolean isInMemory() {
        return _pconf.isTransient();
    }

    public long getLastUsed() {
        return _lastUsed;
    }

    /** Keep track of the time the process was last used. */
    private final void markused() {
        _lastUsed = System.currentTimeMillis();
    }

    /** Create a version-appropriate runtime context. */
    BpelRuntimeContextImpl createRuntimeContext(ProcessInstanceDAO dao, PROCESS template,
            MyRoleMessageExchangeImpl instantiatingMessageExchange) {
        return new BpelRuntimeContextImpl(this, dao, template, instantiatingMessageExchange);
    }
}
