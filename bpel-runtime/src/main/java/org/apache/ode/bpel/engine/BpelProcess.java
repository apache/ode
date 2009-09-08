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

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.agents.memory.SizingAgent;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.DeferredProcessInstanceCleanable;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.engine.extvar.ExternalVariableConf;
import org.apache.ode.bpel.engine.extvar.ExternalVariableManager;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.bpel.intercept.InstanceCountThrottler;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.o.OElementVarType;
import org.apache.ode.bpel.o.OExpressionLanguage;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.bpel.runtime.ExpressionLanguageRuntimeRegistry;
import org.apache.ode.bpel.runtime.InvalidProcessException;
import org.apache.ode.bpel.runtime.PROCESS;
import org.apache.ode.bpel.runtime.PropertyAliasEvaluationContext;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.jacob.soup.ReplacementMap;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.utils.Properties;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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

    /** Mapping from a myrole to a {"Service Name" (QNAME) / port}. It's actually more a tuple than a map as
     * it's important to note that the same process with the same endpoint can have 2 different myroles. */
    private volatile Map<PartnerLinkMyRoleImpl, Endpoint> _endpointToMyRoleMap;

    /** Mapping from a potentially shared endpoint to its EPR */
    private SharedEndpoints _sharedEps;

    // Backup hashmaps to keep initial endpoints handy after dehydration
    private Map<Endpoint, EndpointReference> _myEprs = new HashMap<Endpoint, EndpointReference>();
    private Map<Endpoint, EndpointReference> _partnerEprs = new HashMap<Endpoint, EndpointReference>();
    private Map<Endpoint, PartnerRoleChannel> _partnerChannels = new HashMap<Endpoint, PartnerRoleChannel>();

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

    /** {@link MessageExchangeInterceptor}s registered for this process. */
    private final List<MessageExchangeInterceptor> _mexInterceptors = new ArrayList<MessageExchangeInterceptor>();

    /** Latch-like thing to control hydration/dehydration. */
    private HydrationLatch _hydrationLatch;

    /** Deploy-time configuraton for external variables. */
    private ExternalVariableConf _extVarConf;

    private ExternalVariableManager _evm;

    public static final QName PROP_PATH = new QName("PATH"); 
    public static final QName PROP_SVG = new QName("SVG"); 
    public static final QName PROP_LAZY_HYDRATE = new QName("process.hydration.lazy");
    public static final QName PROP_MAX_INSTANCES = new QName("process.instance.throttled.maximum.count");
    
    // The ratio of in-memory vs serialized size of compiled bpel object.
    private static final int PROCESS_MEMORY_TO_SERIALIZED_SIZE_RATIO = 5;
    
    public BpelProcess(ProcessConf conf) {
        _pid = conf.getProcessId();
        _pconf = conf;
        _hydrationLatch = new HydrationLatch();
    }
    

    /**
     * Retrives the base URI to use for local resource resolution.
     *
     * @return URI - instance representing the absolute file path to the physical location of the process definition folder.
     */
    public URI getBaseResourceURI() {
        return this._pconf.getBaseURI();
    }

    /**
     * Intiialize the external variable configuration/engine manager. This is called from hydration logic, so it 
     * is possible to change the external variable configuration at runtime.
     *
     */
    void initExternalVariables() {
        List<Element> conf = _pconf.getExtensionElement(ExternalVariableConf.EXTVARCONF_ELEMENT);
        _extVarConf = new ExternalVariableConf(conf);
        _evm = new ExternalVariableManager(_pid, _extVarConf, _engine._contexts.externalVariableEngines, _oprocess);
    }


    public String toString() {
        return "BpelProcess[" + _pid + "]";
    }

    public ExternalVariableManager getEVM() {
        return _evm;
    }

    public void recoverActivity(ProcessInstanceDAO instanceDAO, String channel, long activityId, String action, FaultData fault) {
        if (__log.isDebugEnabled())
            __log.debug("Recovering activity in process " + instanceDAO.getInstanceId() + " with action " + action);
        markused();
        BpelRuntimeContextImpl processInstance = createRuntimeContext(instanceDAO, null, null);
        processInstance.recoverActivity(channel, activityId, action, fault);
    }
    
    protected DebuggerSupport createDebuggerSupport() {
        return new DebuggerSupport(this);
    }
    
    protected DebuggerSupport getDebuggerSupport() {
        return _debugger;
    }

    static String generateMessageExchangeIdentifier(String partnerlinkName, String operationName) {
        StringBuffer sb = new StringBuffer(partnerlinkName);
        sb.append('.');
        sb.append(operationName);
        return sb.toString();
    }

    public interface InvokeHandler {
        boolean invoke(PartnerLinkMyRoleImpl target, PartnerLinkMyRoleImpl.RoutingInfo routingInfo, boolean createInstance);
    }
    
    public void invokeProcess(MyRoleMessageExchangeImpl mex, InvokeHandler invokeHandler) {
        boolean routed = false;

        try {
            _hydrationLatch.latch(1);
            List<PartnerLinkMyRoleImpl> targets = getMyRolesForService(mex.getServiceName());
            if (targets.isEmpty()) {
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

            // Ideally, if Java supported closure, the routing code would return null or the appropriate
            // closure to handle the route.
            List<PartnerLinkMyRoleImpl.RoutingInfo> routings = null;
            for (PartnerLinkMyRoleImpl target : targets) {
                routings = target.findRoute(mex);
                boolean createInstance = target.isCreateInstance(mex);

                if (mex.getStatus() != MessageExchange.Status.FAILURE) {
                    for (PartnerLinkMyRoleImpl.RoutingInfo routing : routings) {
                        routed = routed || invokeHandler.invoke(target, routing, createInstance);
                    }
                }
                if (routed) {
                    break;
                }
            }

            // Nothing found, saving for later
            if (!routed) {
                // TODO this is kind of hackish when no match and more than one myrole is selected.
                // we save the routing on the last myrole
                // actually the message queue should be attached to the instance instead of the correlator
                targets.get(targets.size()-1).noRoutingMatch(mex, routings);
            } else {
                // Now we have to update our message exchange status. If the <reply> was not hit during the
                // invocation, then we will be in the "REQUEST" phase which means that either this was a one-way
                // or a two-way that needs to delivery the reply asynchronously.
                if (mex.getStatus() == MessageExchange.Status.REQUEST) {
                    mex.setStatus(MessageExchange.Status.ASYNC);
                }

                markused();
            }
        } finally {
            _hydrationLatch.release(1);
        }

        // For a one way, once the engine is done, the mex can be safely released.
        // Sean: not really, if route is not found, we cannot delete the mex yet
        if (mex.getPattern().equals(MessageExchange.MessageExchangePattern.REQUEST_ONLY) && routed && getCleanupCategories(false).contains(CLEANUP_CATEGORY.MESSAGES)) {
            mex.release();
        }
    }
    
    /**
     * Entry point for message exchanges aimed at the my role.
     *
     * @param mex
     */
    void invokeProcess(final MyRoleMessageExchangeImpl mex) {
        invokeProcess(mex, new InvokeHandler() {
            public boolean invoke(PartnerLinkMyRoleImpl target, PartnerLinkMyRoleImpl.RoutingInfo routing, boolean createInstance) {
                  if (routing.messageRoute == null && createInstance) {
                      // No route but we can create a new instance
                      target.invokeNewInstance(mex, routing);
                      return true;
                  } else if (routing.messageRoute != null) {
                      // Found a route, hitting it
                      target.invokeInstance(mex, routing);
                      return true;
                  }
                  return false;
            }
        });
    }

    /** Several myroles can use the same service in a given process */
    private List<PartnerLinkMyRoleImpl> getMyRolesForService(QName serviceName) {
        List<PartnerLinkMyRoleImpl> myRoles = new ArrayList<PartnerLinkMyRoleImpl>(5);
        for (Map.Entry<PartnerLinkMyRoleImpl,Endpoint> e : getEndpointToMyRoleMap().entrySet()) {
            if (e.getValue().serviceName.equals(serviceName))
                myRoles.add(e.getKey());
        }
        return myRoles;
    }

    void initMyRoleMex(MyRoleMessageExchangeImpl mex) {
        markused();

        PartnerLinkMyRoleImpl target = null;
        for (Map.Entry<PartnerLinkMyRoleImpl,Endpoint> e : getEndpointToMyRoleMap().entrySet()) {
            if (e.getValue().serviceName.equals(mex.getServiceName())) {
                // First one is fine as we're only interested in the portType and operation here and
                // even if a process has 2 myrole partner links
                target = e.getKey();
                break;
            }
        }
        if (target != null) {
            mex.setPortOp(target._plinkDef.myRolePortType, target._plinkDef.getMyRoleOperation(mex.getOperationName()));
        } else {
            __log.warn("Couldn't find endpoint from service " + mex.getServiceName() + " when initializing a myRole mex.");
        }
    }

    /**
     * Extract the value of a BPEL property from a BPEL messsage variable.
     *
     * @param msgData
     *            message variable data
     * @param alias
     *            alias to apply
     * @param target
     *            description of the data (for error logging only)
     * @return value of the property
     * @throws FaultException
     */
    String extractProperty(Element msgData, OProcess.OPropertyAlias alias, String target) throws FaultException {
        markused();
        PropertyAliasEvaluationContext ectx = new PropertyAliasEvaluationContext(msgData, alias);
        Node lValue = ectx.getRootNode();

        if (alias.location != null) {
            try {
                lValue = _expLangRuntimeRegistry.evaluateNode(alias.location, ectx);
            } catch (EvaluationException ec) {
                throw new FaultException(getOProcess().constants.qnSelectionFailure, alias.getDescription());
            }
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
            // with text and attribute nodes.
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
     * Get the element name for a given WSDL part. If the part is an <em>element</em> part, the name of that element is returned.
     * If the part is an XML schema typed part, then the name of the part is returned in the null namespace.
     *
     * @param part
     *            WSDL {@link javax.wsdl.Part}
     * @return name of element containing said part
     */
    static QName getElementNameForPart(OMessageVarType.Part part) {
        return (part.type instanceof OElementVarType) ? ((OElementVarType) part.type).elementType : new QName(null, part.name);
    }

    /**
     * Process the message-exchange interceptors.
     *
     * @param mex
     *            message exchange
     * @return <code>true</code> if execution should continue, <code>false</code> otherwise
     */
    public boolean processInterceptors(MyRoleMessageExchangeImpl mex, InterceptorInvoker invoker) {
        InterceptorContextImpl ictx = new InterceptorContextImpl(_engine._contexts.dao.getConnection(), getProcessDAO(), _pconf, _engine, this);

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
        try {
            _hydrationLatch.latch(1);
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
                MyRoleMessageExchangeImpl mex = (MyRoleMessageExchangeImpl) _engine.getMessageExchange(we.getMexId());
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
                        if( procInstance.getState() == ProcessState.STATE_COMPLETED_OK 
                                || procInstance.getState() == ProcessState.STATE_COMPLETED_WITH_FAULT ) {
                            __log.debug("A matcher event was aborted. The process is already completed.");
                            return;
                        }
                        processInstance.matcherEvent(we.getCorrelatorId(), we.getCorrelationKeySet());
                }
            }
        } finally {
            _hydrationLatch.release(1);
        }
    }

    private void setRoles(OProcess oprocess) {
        _partnerRoles = new HashMap<OPartnerLink, PartnerLinkPartnerRoleImpl>();
        _myRoles = new HashMap<OPartnerLink, PartnerLinkMyRoleImpl>();
        _endpointToMyRoleMap = new HashMap<PartnerLinkMyRoleImpl, Endpoint>();

        // Create myRole endpoint name mapping (from deployment descriptor)
        HashMap<OPartnerLink, Endpoint> myRoleEndpoints = new HashMap<OPartnerLink, Endpoint>();
        for (Map.Entry<String, Endpoint> provide : _pconf.getProvideEndpoints().entrySet()) {
            OPartnerLink plink = oprocess.getPartnerLink(provide.getKey());
            if (plink == null) {
                String errmsg = "Error in deployment descriptor for process " + _pid + "; reference to unknown partner link "
                        + provide.getKey();
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }
            myRoleEndpoints.put(plink, provide.getValue());
        }

        // Create partnerRole initial value mapping
        for (Map.Entry<String, Endpoint> invoke : _pconf.getInvokeEndpoints().entrySet()) {
            OPartnerLink plink = oprocess.getPartnerLink(invoke.getKey());
            if (plink == null) {
                String errmsg = "Error in deployment descriptor for process " + _pid + "; reference to unknown partner link "
                        + invoke.getKey();
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }
            __log.debug("Processing <invoke> element for process " + _pid + ": partnerlink " + invoke.getKey() + " --> "
                    + invoke.getValue());
        }

        for (OPartnerLink pl : oprocess.getAllPartnerLinks()) {
            if (pl.hasMyRole()) {
                Endpoint endpoint = myRoleEndpoints.get(pl);
                if (endpoint == null)
                    throw new IllegalArgumentException("No service name for myRole plink " + pl.getName());
                PartnerLinkMyRoleImpl myRole = new PartnerLinkMyRoleImpl(this, pl, endpoint);
                _myRoles.put(pl, myRole);
                _endpointToMyRoleMap.put(myRole, endpoint);
            }

            if (pl.hasPartnerRole()) {
                Endpoint endpoint = _pconf.getInvokeEndpoints().get(pl.getName());
                if (endpoint == null && pl.initializePartnerRole)
                    throw new IllegalArgumentException(pl.getName() + " must be bound to an endpoint in deploy.xml");
                PartnerLinkPartnerRoleImpl partnerRole = new PartnerLinkPartnerRoleImpl(this, pl, endpoint);
                _partnerRoles.put(pl, partnerRole);
            }
        }
    }

    public ProcessDAO getProcessDAO() {
        return _pconf.isTransient() ? _engine._contexts.inMemDao.getConnection().getProcess(_pid) : getEngine()._contexts.dao
                .getConnection().getProcess(_pid);
    }

    static String genCorrelatorId(OPartnerLink plink, String opName) {
        return plink.getName() + "." + opName;
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
        _sharedEps = _engine.getSharedEndpoints();
        _debugger = createDebuggerSupport();

        if (getInstanceMaximumCount() < Integer.MAX_VALUE)
            registerMessageExchangeInterceptor(new InstanceCountThrottler());
        
        __log.debug("Activating " + _pid);
        // Activate all the my-role endpoints.
        for (Map.Entry<String, Endpoint> entry : _pconf.getProvideEndpoints().entrySet()) {
            Endpoint endpoint = entry.getValue();
            EndpointReference initialEPR = null;
            if (isShareable(endpoint)) {
                // Check if the EPR already exists for the given endpoint
                initialEPR = _sharedEps.getEndpointReference(endpoint);
                if (initialEPR == null) {
                    // Create an EPR by physically activating the endpoint
                    initialEPR = _engine._contexts.bindingContext.activateMyRoleEndpoint(_pid, endpoint);
                    _sharedEps.addEndpoint(endpoint, initialEPR);
                    __log.debug("Activated " + _pid + " myrole " + entry.getKey() + ": EPR is " + initialEPR);
                }
                // Increment the reference count on the endpoint
                _sharedEps.incrementReferenceCount(endpoint);
            } else {
                // Create an EPR by physically activating the endpoint
                initialEPR = _engine._contexts.bindingContext.activateMyRoleEndpoint(_pid, endpoint);
                __log.debug("Activated " + _pid + " myrole " + entry.getKey() + ": EPR is " + initialEPR);
            }
            _myEprs.put(endpoint, initialEPR);
        }
        __log.debug("Activated " + _pid);

        markused();
    }

    void deactivate() {
        // Deactivate all the my-role endpoints.
        for (Endpoint endpoint : _myEprs.keySet()) {
            // Deactivate the EPR only if there are no more references
            // to this endpoint from any (active) BPEL process.
            if (isShareable(endpoint)) {
                __log.debug("deactivating shared endpoint " + endpoint);
                if (!_sharedEps.decrementReferenceCount(endpoint)) {
                    _engine._contexts.bindingContext.deactivateMyRoleEndpoint(endpoint);
                    _sharedEps.removeEndpoint(endpoint);
                }
            } else {
                __log.debug("deactivating non-shared endpoint " + endpoint);
                _engine._contexts.bindingContext.deactivateMyRoleEndpoint(endpoint);
            }
        }
        // TODO Deactivate all the partner-role channels
    }

    private boolean isShareable(Endpoint endpoint) {
        return _pconf.isSharedService(endpoint.serviceName);
    }

    protected EndpointReference getInitialPartnerRoleEPR(OPartnerLink link) {
        try {
            _hydrationLatch.latch(1);
            PartnerLinkPartnerRoleImpl prole = _partnerRoles.get(link);
            if (prole == null)
                throw new IllegalStateException("Unknown partner link " + link);
            return prole.getInitialEPR();
        } finally {
            _hydrationLatch.release(1);
        }
    }

    protected Endpoint getInitialPartnerRoleEndpoint(OPartnerLink link) {
        try {
            _hydrationLatch.latch(1);
            PartnerLinkPartnerRoleImpl prole = _partnerRoles.get(link);
            if (prole == null)
                throw new IllegalStateException("Unknown partner link " + link);
            return prole._initialPartner;
        } finally {
            _hydrationLatch.release(1);
        }
    }

    protected EndpointReference getInitialMyRoleEPR(OPartnerLink link) {
        try {
            _hydrationLatch.latch(1);
            PartnerLinkMyRoleImpl myRole = _myRoles.get(link);
            if (myRole == null)
                throw new IllegalStateException("Unknown partner link " + link);
            return myRole.getInitialEPR();
        } finally {
            _hydrationLatch.release(1);
        }
    }

    public QName getPID() {
        return _pid;
    }

    protected PartnerRoleChannel getPartnerRoleChannel(OPartnerLink partnerLink) {
        try {
            _hydrationLatch.latch(1);
            PartnerLinkPartnerRoleImpl prole = _partnerRoles.get(partnerLink);
            if (prole == null)
                throw new IllegalStateException("Unknown partner link " + partnerLink);
            return prole._channel;
        } finally {
            _hydrationLatch.release(1);
        }
    }

    public void saveEvent(ProcessInstanceEvent event, ProcessInstanceDAO instanceDao) {
        saveEvent(event, instanceDao, null);
    }

    public void saveEvent(ProcessInstanceEvent event, ProcessInstanceDAO instanceDao, List<String> scopeNames) {
        markused();
        if (_pconf.isEventEnabled(scopeNames, event.getType())) {
            // notify the listeners
            _engine.fireEvent(event);

            if (instanceDao != null)
                instanceDao.insertBpelEvent(event);
            else
                __log.debug("Couldn't find instance to save event, no event generated!");
        }
    }

    /**
     * Ask the process to dehydrate.
     */
    void dehydrate() {
        try {
            _hydrationLatch.latch(0);
            // We don't actually need to do anything, the latch will run the doDehydrate method
            // when necessary..
        } finally {
            _hydrationLatch.release(0);
        }

    }

    void hydrate() {
        try {
            _hydrationLatch.latch(1);
            // We don't actually need to do anything, the latch will run the doHydrate method
            // when necessary..
        } finally {
            _hydrationLatch.release(1);
        }
    }

    public OProcess getOProcess() {
        try {
            _hydrationLatch.latch(1);
            return _oprocess;
        } finally {
            _hydrationLatch.release(1);
        }
    }

    private Map<PartnerLinkMyRoleImpl,Endpoint> getEndpointToMyRoleMap() {
        try {
            _hydrationLatch.latch(1);
            return _endpointToMyRoleMap;
        } finally {
            _hydrationLatch.release(1);
        }
    }

    public ReplacementMap getReplacementMap(QName processName) {
        try {
            _hydrationLatch.latch(1);

            if (processName.equals(_pid)) return _replacementMap;
            else
                try {
                    // We're asked for an older version of this process, fetching it
                    OProcess oprocess = _engine.getOProcess(processName);
                    if (oprocess == null) {
                        String errmsg = "The process " + _pid + " is not available anymore.";
                        __log.error(errmsg);
                        throw new InvalidProcessException(errmsg, InvalidProcessException.RETIRED_CAUSE_CODE);
                    }
                    // Older versions may ventually need more expression languages
                    registerExprLang(oprocess);

                    return new ReplacementMapImpl(oprocess);
                } catch (Exception e) {
                    String errmsg = "The process " + _pid + " is not available anymore.";
                    __log.error(errmsg, e);
                    throw new InvalidProcessException(errmsg, InvalidProcessException.RETIRED_CAUSE_CODE);
                }
        } finally {
            _hydrationLatch.release(1);
        }
    }

    protected BpelEngineImpl getEngine() {
        return _engine;
    }

    public boolean isInMemory() {
        return _pconf.isTransient();
    }

    public long getLastUsed() {
        return _lastUsed;
    }

    QName getProcessType() {
        return _pconf.getType();
    }

    /**
     * Get a hint as to whether this process is hydrated. Note this is only a hint, since things could change.
     */
    public boolean hintIsHydrated() {
        return _oprocess != null;
    }

    /** Keep track of the time the process was last used. */
    private final void markused() {
        _lastUsed = System.currentTimeMillis();
    }

    /** Create a version-appropriate runtime context. */
    protected BpelRuntimeContextImpl createRuntimeContext(ProcessInstanceDAO dao, PROCESS template,
                                                MyRoleMessageExchangeImpl instantiatingMessageExchange) {
        return new BpelRuntimeContextImpl(this, dao, template, instantiatingMessageExchange);

    }

    private class HydrationLatch extends NStateLatch {
        HydrationLatch() {
            super(new Runnable[2]);
            _transitions[0] = new Runnable() {
                public void run() {
                    doDehydrate();
                }
            };
            _transitions[1] = new Runnable() {
                public void run() {
                    doHydrate();
                }
            };
        }

        private void doDehydrate() {
            if (_oprocess != null) {
                _oprocess.dehydrate();
                _oprocess = null;
            }
            if (_myRoles != null) {
                _myRoles.clear();
            }
            if (_endpointToMyRoleMap != null) {
                _endpointToMyRoleMap.clear();
            }
            if (_partnerRoles != null) {
                _partnerRoles.clear();
            }
            // Don't clear stuff you can't re-populate
//            if (_myEprs != null) {
//                _myEprs.clear();
//            }
//            if (_partnerChannels != null) {
//                _partnerChannels.clear();
//            }
//            if (_partnerEprs != null) {
//                _partnerEprs.clear();
//            }
            _replacementMap = null;
            _expLangRuntimeRegistry = null;
        }

        private void doHydrate() {
            markused();
            __log.debug("Rehydrating process " + _pconf.getProcessId());
            try {
                InputStream inputStream = _pconf.getCBPInputStream();
                try {
                    _oprocess = deserializeCompiledProcess(inputStream);
                } finally {
                    inputStream.close();
                }
            } catch (Exception e) {
                String errmsg = "The process " + _pid + " is no longer available.";
                __log.error(errmsg, e);
                throw new BpelEngineException(errmsg, e);
            }
            
            if (_partnerRoles == null) {
                _partnerRoles = new HashMap<OPartnerLink, PartnerLinkPartnerRoleImpl>();
            }
            if (_myRoles == null) {
                _myRoles = new HashMap<OPartnerLink, PartnerLinkMyRoleImpl>();
            }
            if (_endpointToMyRoleMap == null) {
                _endpointToMyRoleMap = new HashMap<PartnerLinkMyRoleImpl, Endpoint>();
            }
            if (_myEprs == null) {
                _myEprs = new HashMap<Endpoint, EndpointReference>();
            }
            if (_partnerChannels == null) {
                _partnerChannels = new HashMap<Endpoint, PartnerRoleChannel>();
            }
            if (_partnerEprs == null) {
                _partnerEprs = new HashMap<Endpoint, EndpointReference>();
            }
            
            _replacementMap = new ReplacementMapImpl(_oprocess);

            // Create an expression language registry for this process
            _expLangRuntimeRegistry = new ExpressionLanguageRuntimeRegistry();
            registerExprLang(_oprocess);

            setRoles(_oprocess);
            initExternalVariables();

            if (!_hydratedOnce) {
                for (PartnerLinkPartnerRoleImpl prole : _partnerRoles.values()) {
                    // Null for initializePartnerRole = false
                    if (prole._initialPartner != null) {
                        PartnerRoleChannel channel = _engine._contexts.bindingContext.createPartnerRoleChannel(_pid,
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
                }
                _engine.setProcessSize(_pid, true);
                _hydratedOnce = true;
            }

            for (PartnerLinkMyRoleImpl myrole : _myRoles.values()) {
                myrole._initialEPR = _myEprs.get(myrole._endpoint);
            }

            for (PartnerLinkPartnerRoleImpl prole : _partnerRoles.values()) {
                prole._channel = _partnerChannels.get(prole._initialPartner);
                if (_partnerEprs.get(prole._initialPartner) != null) {
                    prole._initialEPR = _partnerEprs.get(prole._initialPartner);
                }
            }

            /*
             * If necessary, create an object in the data store to represent the process. We'll re-use an existing object if it already
             * exists and matches the GUID.
             */
            if (isInMemory()) {
                createProcessDAO(_engine._contexts.inMemDao.getConnection(), _pid, _pconf.getVersion(), _oprocess);
            } else if (_engine._contexts.scheduler.isTransacted()) {
                // If we have a transaction, we do this in the current transaction
                if(__log.isDebugEnabled()) __log.debug("Creating new process DAO for " + _pid + " (guid=" + _oprocess.guid + ")...");
                createProcessDAO(_engine._contexts.dao.getConnection(), _pid, _pconf.getVersion(), _oprocess);
                if(__log.isInfoEnabled()) __log.info("Created new process DAO for " + _pid + " (guid=" + _oprocess.guid + ").");
            } else {
                try {
                    _engine._contexts.scheduler.execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            bounceProcessDAOInDB(_engine._contexts.dao.getConnection(), _pid, _pconf.getVersion(), _oprocess);
                            return null;
                        }
                    });
                } catch( RuntimeException re ) {
                    throw re;
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    private void bounceProcessDAOInMemory(BpelDAOConnection conn, final QName pid, final long version, final OProcess oprocess) {
        if(__log.isInfoEnabled()) __log.info("Creating new process DAO[mem] for " + pid + " (guid=" + oprocess.guid + ").");
        createProcessDAO(conn, pid, version, oprocess);
    }

    private void bounceProcessDAOInDB(final BpelDAOConnection conn, final QName pid, final long version, final OProcess oprocess) {
        if(__log.isDebugEnabled()) __log.debug("Creating new process DAO for " + pid + " (guid=" + oprocess.guid + ")...");
        createProcessDAO(conn, pid, version, oprocess);
        if(__log.isInfoEnabled()) __log.info("Created new process DAO for " + pid + " (guid=" + oprocess.guid + ").");
    }

    public int getInstanceInUseCount() {
        return hintIsHydrated() ? _hydrationLatch.getDepth(1) : 0;
    }

    private void createProcessDAO(BpelDAOConnection conn, final QName pid, final long version, final OProcess oprocess) {
        try {
            boolean create = true;
            ProcessDAO old = conn.getProcess(pid);
            if (old != null) {
                __log.debug("Found ProcessDAO for " + pid + " with GUID " + old.getGuid());
                if (oprocess.guid == null) {
                    // No guid, old version assume its good
                    create = false;
                } else {
                    if (old.getGuid().equals(oprocess.guid)) {
                        // Guids match, no need to create
                        create = false;
                    }
                }
            }

            if (create) {
                if(__log.isDebugEnabled()) __log.debug("Creating process DAO for " + pid + " (guid=" + oprocess.guid + ")");

                ProcessDAO newDao = conn.createProcess(pid, oprocess.getQName(), oprocess.guid, (int) version);
                for (String correlator : oprocess.getCorrelators()) {
                    newDao.addCorrelator(correlator);
                }
                if(__log.isInfoEnabled()) __log.info("Created new process DAO for " + pid + " (guid=" + oprocess.guid + ")");
            }
        } catch (BpelEngineException ex) {
            throw ex;
        } catch (Exception dce) {
            __log.error("DbError", dce);
            throw new BpelEngineException("DbError", dce);
        }
    }

    private void registerExprLang(OProcess oprocess) {
        for (OExpressionLanguage elang : oprocess.expressionLanguages) {
            try {
                _expLangRuntimeRegistry.registerRuntime(elang);
            } catch (ConfigurationException e) {
                String msg = __msgs.msgExpLangRegistrationError(elang.expressionLanguageUri, elang.properties);
                __log.error(msg, e);
                throw new BpelEngineException(msg, e);
            }
        }
    }

    public boolean isCleanupCategoryEnabled(boolean instanceSucceeded, CLEANUP_CATEGORY category) {
        return _pconf.isCleanupCategoryEnabled(instanceSucceeded, category);
    }

    public Set<CLEANUP_CATEGORY> getCleanupCategories(boolean instanceSucceeded) {
        return _pconf.getCleanupCategories(instanceSucceeded);
    }

    public Node getProcessProperty(QName propertyName) {
        Map<QName, Node> properties = _pconf.getProcessProperties();
        if (properties != null) {
            return properties.get(propertyName);
        }
        return null;
    }

    public ProcessConf getConf() {
        return _pconf;
    }

    public boolean hasActiveInstances() {
        try {
            _hydrationLatch.latch(1);
            if (isInMemory() || _engine._contexts.scheduler.isTransacted()) { 
                return hasActiveInstances(getProcessDAO());
            } else {
                // If we do not have a transaction we need to create one. 
                try {
                    return (Boolean) _engine._contexts.scheduler.execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            return hasActiveInstances(getProcessDAO());
                        }
                    });
                } catch (Exception e) {
                    String errmsg = "DbError";
                    __log.error(errmsg, e);
                    return false;
                }
            }
        } finally {
            _hydrationLatch.release(1);
        }
    }
    
    private boolean hasActiveInstances(ProcessDAO processDAO) {
        // Select count of instances instead of all active instances 
        // Collection<ProcessInstanceDAO> activeInstances = processDAO.getActiveInstances();
        // return (activeInstances != null && activeInstances.size() > 0);
        return processDAO.getNumInstances() > 0;
    }

    public void registerMessageExchangeInterceptor(MessageExchangeInterceptor interceptor) {
        _mexInterceptors.add(interceptor);
    }
    
    public void unregisterMessageExchangeInterceptor(MessageExchangeInterceptor interceptor) {
        _mexInterceptors.remove(interceptor);
    }
    
    public long sizeOf() {
        // try to get actual size from sizing agent, if enabled
        long footprint = SizingAgent.deepSizeOf(this);
        // if unsuccessful, estimate size (this is a inaccurate guess)
        if (footprint == 0) {
            footprint = getEstimatedHydratedSize();
        }
        // add the sizes of all the services this process provides
        for (EndpointReference myEpr : _myEprs.values()) {
            footprint += _engine._contexts.bindingContext.calculateSizeofService(myEpr);
        }
        // return the total footprint
        return footprint;
    }
    
    public String getProcessProperty(QName property, String defaultValue) {
        Text text = (Text) getProcessProperty(property);
        if (text == null) {
            return defaultValue;
        }
        String value = text.getWholeText();
        return (value == null) ?  defaultValue : value;
    }

    public boolean isHydrationLazy() {
        return Boolean.valueOf(getProcessProperty(PROP_LAZY_HYDRATE, "true"));
    }
    
    public boolean isHydrationLazySet() {
        return getProcessProperty(PROP_LAZY_HYDRATE) != null;
    }

    public int getInstanceMaximumCount() {
        return Integer.valueOf(getProcessProperty(PROP_MAX_INSTANCES, Integer.toString(_engine.getInstanceThrottledMaximumCount())));
    }

    public long getEstimatedHydratedSize() {
        return _pconf.getCBPFileSize() * 
                    PROCESS_MEMORY_TO_SERIALIZED_SIZE_RATIO;
    }

    public long getTimeout(OPartnerLink partnerLink) {
        // OPartnerLink, PartnerLinkPartnerRoleImpl
        final PartnerLinkPartnerRoleImpl linkPartnerRole = _partnerRoles.get(partnerLink);
        long timeout = Properties.DEFAULT_MEX_TIMEOUT;
        String timeout_property = _pconf.getEndpointProperties(linkPartnerRole._initialEPR).get(Properties.PROP_MEX_TIMEOUT);
        if (timeout_property != null) {
            try {
                timeout = Long.parseLong(timeout_property);
            } catch (NumberFormatException e) {
                if (__log.isWarnEnabled())
                    __log.warn("Mal-formatted Property: [" + Properties.PROP_MEX_TIMEOUT + "=" + timeout_property + "] Default value (" + timeout + ") will be used");
            }
        }

        return timeout;
    }

    public int getVersion() {
        return Integer.parseInt(_pid.getLocalPart().substring(_pid.getLocalPart().lastIndexOf('-') + 1));
    }
}
