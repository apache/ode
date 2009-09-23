package org.apache.ode.bpel.rtrep.v2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.net.URI;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evt.ProcessInstanceStartedEvent;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.rapi.*;
import org.apache.ode.bpel.rtrep.v2.channels.*;
import org.apache.ode.bpel.extension.ExtensionOperation;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.evar.IncompleteKeyException;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.extension.ExtensionBundleRuntime;
import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.jacob.vpu.JacobVPU;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Implementation of {@link OdeInternalInstance} for the "modern" runtime. This class also serves as a repository for kitchen sink type
 * methods that the activities all use. A lot of these methods are simply deferals to similar methods on
 * {@link OdeRTInstanceContext}; however here these methods use representation-specific classes (e.g.
 * {@link OPartnerLink) while the {@link OdeRTInstanceContext} methods use only the general (non-representation specific) interfaces
 * (e.g. {@link PartnerLink}.
 * 
 * @author Maciej Szefler
 * 
 */
public class RuntimeInstanceImpl implements OdeInternalInstance, OdeRTInstance {
    private static final Log __log = LogFactory.getLog(RuntimeInstanceImpl.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private OdeRTInstanceContext _brc;

    /** JACOB VPU */
    protected JacobVPU _vpu;

    /** JACOB ExecutionQueue (state) */
    protected ExecutionQueueImpl _soup;

    private RuntimeImpl _runtime;

    public RuntimeInstanceImpl(RuntimeImpl runtime, ExecutionQueueImpl soup) {
        _runtime = runtime;
        _vpu = new JacobVPU();
        _vpu.registerExtension(OdeRTInstanceContext.class, this);
        if (soup == null) {
            _soup = new ExecutionQueueImpl(getClass().getClassLoader());
            _soup.setGlobalData(new OutstandingRequestManager());
        } else {
            _soup = soup;
        }

        _soup.setReplacementMap(_runtime._replacementMap);
        _vpu.setContext(_soup);
    }

    public ProcessModel getProcessModel() {
        return _runtime._oprocess;
    }

    public boolean isCorrelationInitialized(CorrelationSetInstance correlationSet) {
        return _brc.isCorrelationInitialized(correlationSet);
    }

    public boolean isVariableInitialized(VariableInstance var) {
        return _brc.isVariableInitialized(var);
    }

    public boolean isPartnerRoleEndpointInitialized(PartnerLinkInstance pLink) {
        return _brc.isPartnerRoleEndpointInitialized(pLink);
    }
    
    public void completedFault(FaultData faultData) {
        cleanupOutstandingMyRoleExchanges(faultData);
        _brc.completedFault(faultData);
    }

    public void completedOk() {
        cleanupOutstandingMyRoleExchanges(null);
        _brc.completedOk();
    }

    public Long createScopeInstance(Long parentScopeId, String name, int modelId) {
        return _brc.createScopeInstance(parentScopeId, name, modelId);
    }

    public void initializePartnerLinks(Long parentScopeId, Collection<OPartnerLink> partnerLinks) {
        _brc.initializePartnerLinks(parentScopeId, partnerLinks);
    }

    public void initializeResource(Long scopeInstanceId, OResource resource, String url) {        
        _brc.initializeResource(scopeInstanceId, resource, url);
    }

    public void initializeInstantiatingUrl(String url) {
        _brc.initializeInstantiatingUrl(url);
    }

    public String getInstantiatingUrl() {
        return _brc.getInstantiatingUrl();
    }

    public void cancelOutstandingRequests(String channelId) {
        getORM().cancel(channelId);
    }

    public void select(PickResponseChannel pickResponseChannel, Date timeout, boolean createInstance, Selector[] selectors)
            throws FaultException {

        final String pickResponseChannelStr = pickResponseChannel.export();

        int conflict = getORM().findConflict(selectors);
        if (conflict != -1)
            throw new FaultException(_runtime._oprocess.constants.qnConflictingReceive, selectors[conflict].toString());

        getORM().register(pickResponseChannelStr, selectors);

        _brc.select(pickResponseChannelStr, timeout, selectors);
    }

    public void checkResourceRoute(ResourceInstance resourceInstance, String mexRef,
                                   PickResponseChannel pickResponseChannel, int selectorIdx) {
        final String pickResponseChannelStr = pickResponseChannel.export();
        getORM().register(pickResponseChannelStr, resourceInstance, resourceInstance.getModel().getMethod(), mexRef);
        _brc.checkResourceRoute(resourceInstance, pickResponseChannelStr, selectorIdx);
    }

    public CorrelationKey readCorrelation(CorrelationSetInstance cset) {
        return _brc.readCorrelation(cset);
    }

    public Node fetchVariableData(VariableInstance variable, ScopeFrame scopeFrame, boolean forWriting) throws FaultException {
        if (variable.declaration.extVar != null) {
            // Note, that when using external variables, the database will not contain the value of the
        	// variable, instead we need to go the external variable subsystems.
        	Element reference = (Element) _brc.fetchVariableData(scopeFrame.resolve(variable.declaration.extVar.related), false);
            try {
                Node ret = _brc.readExtVar(variable, reference);
                if (ret == null) {
                    throw new FaultException(_runtime._oprocess.constants.qnUninitializedVariable,
                            "The external variable \"" + variable.declaration.name + "\" has not been initialized.");
                }
                return ret;
            } catch (IncompleteKeyException ike) {
                // This indicates that the external variable needed to be written do, put has not been.
                __log.error("External variable could not be read due to incomplete key; the following key " +
                        "components were missing: " + ike.getMissing());
                throw new FaultException(_runtime._oprocess.constants.qnUninitializedVariable,
                        "The extenral variable \"" + variable.declaration.name + "\" has not been properly initialized;" +
                                "the following key compoenents were missing:" + ike.getMissing());
            } catch (ExternalVariableModuleException e) {
                throw new BpelEngineException(e);
            }
        } else /* not external */ {
            Node data = _brc.fetchVariableData(variable, forWriting);
            if (data == null) {
                // Special case of messageType variables with no part
                if (variable.declaration.type instanceof OMessageVarType) {
                    OMessageVarType msgType = (OMessageVarType) variable.declaration.type;
                    if (msgType.parts.size() == 0) {
                        Document doc = DOMUtils.newDocument();
                        Element root = doc.createElement("message");
                        doc.appendChild(root);
                        return root;
                    }
                }
                throw new FaultException(_runtime._oprocess.constants.qnUninitializedVariable,
                        "The variable " + variable.declaration.name + " isn't properly initialized.");
            }
            return data;
        }
    }

    public Node fetchVariableData(VariableInstance var, ScopeFrame scopeFrame,
                                  OMessageVarType.Part part, boolean forWriting) throws FaultException {
        Node val = fetchVariableData(var, scopeFrame, forWriting);
        if (part != null) return getPartData((Element) val, part);
        return val;
    }

    public void writeCorrelation(CorrelationSetInstance cset, CorrelationKey ckeyVal) throws FaultException {
        OScope.CorrelationSet csetdef = cset.declaration;
        QName[] propNames = new QName[csetdef.properties.size()];
        for (int m = 0; m < csetdef.properties.size(); m++) {
            OProcess.OProperty oProperty = csetdef.properties.get(m);
            propNames[m] = oProperty.name;
        }

        ckeyVal.setUnique(cset.declaration.isUnique());
        _brc.writeCorrelation(cset, propNames, ckeyVal);

    }

    /**
     * Proxy to {@link OdeRTInstanceContext#sendEvent(org.apache.ode.bpel.evt.ProcessInstanceEvent)}.
     * 
     * @param event
     */
    public void sendEvent(ScopeEvent event) {
        _brc.sendEvent(event);
    }

    public void unregisterActivityForRecovery(ActivityRecoveryChannel recoveryChannel) {
        _brc.unregisterActivityForRecovery(recoveryChannel.export());
    }

    /**
     * Proxy to {@link RecoveryContext#registerActivityForRecovery(String, long, String, Date, Element, String[], int)}.
     */
    public void registerActivityForRecovery(ActivityRecoveryChannel recoveryChannel, long id, String reason, Date dateTime,
            Element details, String[] actions, int retryCount) {
        _brc.registerActivityForRecovery(recoveryChannel.export(), id, reason, dateTime, details, actions, retryCount);
    }

    /**
     * Proxy to {@link IOContext#registerTimer(String, Date)} .
     */
    public void registerTimer(TimerResponseChannel timerChannel, Date future) {
        _brc.registerTimer(timerChannel.export(), future);
    }

    /**
     * Proxy to {@link VariableContext#readVariableProperty(Variable, QName)}.
     */
    public String readProperty(VariableInstance variable, OProcess.OProperty property) throws FaultException {
        try {
            return _brc.readVariableProperty(variable, property.name);
        } catch (UninitializedVariableException e) {
            throw new FaultException(_runtime._oprocess.constants.qnUninitializedVariable);
        }
    }

    /**
     * Proxy to {@link OdeRTInstanceContext#genId() }.
     */
    public long genId() {
        return _brc.genId();
    }

    /**
     * Proxy to {@link OdeRTInstanceContext#initializeVariable(Variable, Node)} then write properties.
     */
    public Node initializeVariable(VariableInstance var, ScopeFrame scopeFrame, Node val) throws ExternalVariableModuleException {
        try {
            if (var.declaration.extVar != null) /* external variable */ {
                if (__log.isDebugEnabled())
                    __log.debug("Initialize external variable: name=" + var.declaration + " value="+DOMUtils.domToString(val));
                Node reference = null;
                try {
                    reference = fetchVariableData(var, scopeFrame, true);
                } catch (FaultException fe) {
                    // In this context this is not necessarily a problem, since the assignment may re-init the related var
                }
                if (reference != null) val = _brc.readExtVar(var, reference);
                return val;
            } else /* normal variable */ {
                if (__log.isDebugEnabled()) __log.debug("Initialize variable: name=" + var.declaration +
                        " value=" + DOMUtils.domToString(val));
                return _brc.initializeVariable(var, val);
            }
        } finally {
            writeProperties(var, val);
        }
    }

    /**
     * Proxy to {@link VariableContext#fetchMyRoleEndpointReferenceData(PartnerLink)}.
     */
    public Node fetchMyRoleEndpointReferenceData(PartnerLinkInstance link) {
        return _brc.fetchMyRoleEndpointReferenceData(link);
    }

    public Node fetchPartnerRoleEndpointReferenceData(PartnerLinkInstance link) throws FaultException {
        Element epr = _brc.fetchPartnerRoleEndpointReferenceData(link);
        if (epr == null) {
            throw new FaultException(_runtime._oprocess.constants.qnUninitializedPartnerRole);
        }

        return epr;

    }

    /**
     * Proxy to {@link OdeRTInstanceContext#convertEndpointReference(Element, Node) }.
     */
    public Node convertEndpointReference(Element epr, Node lvaluePtr) {
        return _brc.convertEndpointReference(epr, lvaluePtr);
    }

    public void commitChanges(VariableInstance var, ScopeFrame scopeFrame, Node value) throws ExternalVariableModuleException {
        if (var.declaration.extVar != null) /* external variable */ {
            if (__log.isDebugEnabled())
                __log.debug("Write external variable: name="+var.declaration + " value="+DOMUtils.domToString(value));
            VariableInstance related = scopeFrame.resolve(var.declaration.extVar.related);
            Node reference = null;
            try {
                reference = fetchVariableData(var, scopeFrame, true);
            } catch (FaultException fe) {
                // In this context this is not necessarily a problem, since the assignment may re-init the related var
            }
            VariableContext.ValueReferencePair vrp  = _brc.writeExtVar(var, reference, value);
            commitChanges(related, scopeFrame, vrp.reference);
        } else /* normal variable */ {
            if (__log.isDebugEnabled())
                __log.debug("Write variable: name="+var.declaration + " value="+DOMUtils.domToString(value));
            _brc.commitChanges(var, value);
        }
        writeProperties(var, value);
    }


    /**
     * Proxy to {@link BpelRuntimeContext# }.
     */
    public void writeEndpointReference(PartnerLinkInstance plval, Element element) {
        _brc.writeEndpointReference(plval, element);
    }

    /**
     * Proxy to {@link OdeRTInstanceContext#createScopeInstance(Long, String, int)}.
     */
    public Long createScopeInstance(Long scopeInstanceId, OScope scopedef) {
        return _brc.createScopeInstance(scopeInstanceId, scopedef.name, scopedef.getId());
    }

    /**
     * Proxy to {@link BpelRuntimeContext# }.
     */
    public String fetchMySessionId(PartnerLinkInstance linkInstance) {
        return _brc.fetchMySessionId(linkInstance);
    }

    /**
     * Proxy to {@link BpelRuntimeContext# }.
     */
    public void cancel(PickResponseChannel responseChannel) {
        final String id = responseChannel.export();
        _brc.cancelSelect(id);

        getORM().cancel(id);

        _vpu.inject(new JacobRunnable() {
            private static final long serialVersionUID = 6157913683737696396L;

            public void run() {
                TimerResponseChannel responseChannel = importChannel(id, TimerResponseChannel.class);
                responseChannel.onCancel();
            }
        });
    }

    /**
     * Proxy to {@link BpelRuntimeContext# }.
     */
    public Element getMyRequest(String mexId) {
        return _brc.getMyRequest(mexId);
    }

    public Map<String,String> getProperties(String mexId) {
        return _brc.getProperties(mexId);
    }

    public void setInstantiatingMex(String mexId) {
        _brc.setInstantiatingMex(mexId);
    }

    /**
     * Proxy to {@link BpelRuntimeContext# }.
     */
    public void initializePartnersSessionId(PartnerLinkInstance instance, String partnersSessionId) {
        _brc.initializePartnersSessionId(instance, partnersSessionId);
    }

    /**
     * Proxy to {@link IOContext#getSourceSessionId(String) }.
     */
    public String getSourceSessionId(String mexId) {
        return _brc.getSourceSessionId(mexId);
    }

    public Node getSourceEPR(String mexId) {
        return _brc.getSourceEPR(mexId);
    }

    public ExtensionOperation createExtensionActivityImplementation(QName name) {
        if (name == null) return null;
        ExtensionBundleRuntime bundle = _runtime._extensionRegistry.get(name.getNamespaceURI());
        if (bundle == null) {
            return null;
        } else {
            try {
                return bundle.getExtensionOperationInstance(name.getLocalPart());
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    public void completeExtensionActivity(final String channelId, final FaultData faultData) {
        _vpu.inject(new JacobRunnable() {
			private static final long serialVersionUID = 5198590543947804763L;

            public void run() {
                ParentScopeChannel channel = importChannel(channelId, ParentScopeChannel.class);
                if (channel == null) {
                	throw new RuntimeException("Could not resolve channel ID (" + channelId + ") for extension activity.");
                }
                channel.completed(faultData, CompensationHandler.emptySet());
            }
        });
    }

    /**
     * Proxy to {@link ProcessControlContext# }.
     */
    public Long getInstanceId() {
        return _brc.getInstanceId();
    }

    /**
     * Proxy to {@link IOContext#getPartnerResponse(String)}.
     */
    public Element getPartnerResponse(String mexId) {
        return _brc.getPartnerResponse(mexId);
    }

    /**
     * Proxy to {@link IOContext#releasePartnerMex(String) }.
     */
    public void releasePartnerMex(String mexId, boolean instanceSucceeded) {
        _brc.releasePartnerMex(mexId, instanceSucceeded);
    }

    /**
     * Proxy to {@link IOContext#getPartnerFault(String) }.
     */
    public QName getPartnerFault(String mexId) {
        return _brc.getPartnerFault(mexId);
    }

    /**
     * Proxy to {@link IOContext#getPartnerResponseType(String) }.
     */
    public QName getPartnerResponseType(String mexId) {
        return _brc.getPartnerResponseType(mexId);
    }

    /**
     * Proxy to {@link IOContext#getPartnerFaultExplanation(String) }.
     */
    public String getPartnerFaultExplanation(String mexId) {
        return _brc.getPartnerFaultExplanation(mexId);
    }

    /**
     * Proxy to {@link OdeRTInstanceContext#sendEvent(org.apache.ode.bpel.evt.ProcessInstanceEvent) }.
     */
    public void sendEvent(ProcessInstanceStartedEvent evt) {
        _brc.sendEvent(evt);
    }

    public void associateEvent(PartnerLinkInstance plinkInstance, String opName, CorrelationKey key, String mexRef, String mexDAO) throws FaultException {
        if(!getORM().associateEvent(plinkInstance, opName, key, mexRef, mexDAO)) {
            //For conflicting request, we need to reply immediately to incoming event.
            try {
                _brc.reply(mexDAO, plinkInstance, opName, null, _runtime._oprocess.constants.qnConflictingRequest);
            } catch (NoSuchOperationException e) {
                throw new IllegalStateException(e);
            }
            throw new FaultException(_runtime._oprocess.constants.qnConflictingRequest);
        }
    }

    public void associateEvent(ResourceInstance resourceInstance, String mexRef, String scopeIid) {
        getORM().associateEvent(resourceInstance, resourceInstance.getModel().getMethod(), mexRef, scopeIid);
    }

    public void reply(PartnerLinkInstance plink, String opName, String bpelmex, Element element, QName fault) throws FaultException {
        String mexid = getORM().release(plink, opName, bpelmex);
        if (mexid == null)
            throw new FaultException(_runtime._oprocess.constants.qnMissingRequest);

        try {
            _brc.reply(mexid, plink, opName, element, fault);
        } catch (NoSuchOperationException e) {
            // reply to operation that is either not defined or one-way. Perhaps this should be detected at compile time?
            throw new FaultException(_runtime._oprocess.constants.qnMissingRequest,
                    "Undefined two-way operation \"" + opName + "\".");
        }
    }

    public void reply(ResourceInstance resource, String bpelmex, Element element, QName fault) throws FaultException {
        String mexid = getORM().release(resource, resource.getModel().getMethod(), bpelmex);
        if (mexid == null)
            throw new FaultException(_runtime._oprocess.constants.qnMissingRequest);

        try {
            _brc.reply(mexid, resource, element, fault);
        } catch (NoSuchOperationException e) {
            // reply to operation that is either not defined or one-way. Perhaps this should be detected at compile time?
            throw new FaultException(_runtime._oprocess.constants.qnMissingRequest,
                    "Undefined two-way operation \"" + resource + "\".");
        }
    }

    /**
     * Proxy to {@link ProcessControlContext#forceFlush() }.
     */
    public void forceFlush() {
        _brc.forceFlush();
    }

    /**
     * Proxy to {@link ProcessControlContext#forceRollback() }.
     */
    public void forceRollback() {
        _brc.forceRollback();
    }
    
    /**
     * Proxy to {@link ProcessControlContext#terminate()}.
     */
    public void terminate() {
        cleanupOutstandingMyRoleExchanges(null);
        _brc.terminate();
    }

    /**
     * Record all values of properties of a 'MessageType' variable for efficient lookup.
     */
    private void writeProperties(VariableInstance variable, Node value) {
        if (variable.declaration.type instanceof OMessageVarType) {
            for (OProcess.OProperty property : variable.declaration.getOwner().properties) {
                OProcess.OPropertyAlias alias = property.getAlias(variable.declaration.type);
                if (alias != null) {
                    try {
                        String val = extractProperty((Element) value, alias, variable.declaration.getDescription());
                        if (val != null)
                            _brc.writeVariableProperty(variable, property.name, val);
                    } catch (UninitializedVariableException uve) {
                        // This really should not happen, since we are writing to a variable that we just modified.
                        __log.fatal("Couldn't extract property '" + property.toString() + "' in property pre-extraction: " + uve);
                        throw new RuntimeException(uve);
                    } catch (FaultException e) {
                        // This will fail as we're basically trying to extract properties on all received messages
                        // for optimization purposes.
                        if (__log.isDebugEnabled())
                            __log.debug("Couldn't extract property '" + property.toString() + "' in property pre-extraction: "
                                    + e.toString());
                    }
                }
            }
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
        PropertyAliasEvaluationContext ectx = new PropertyAliasEvaluationContext(msgData, alias);
        Node lValue = ectx.getRootNode();

        if (alias.location != null)
            lValue = _runtime._expLangRuntimeRegistry.evaluateNode(alias.location, ectx);

        if (lValue == null) {
            String errmsg = __msgs.msgPropertyAliasReturnedNullSet(alias.getDescription(), target);
            if (__log.isErrorEnabled()) {
                __log.error(errmsg);
            }
            throw new FaultException(_runtime._oprocess.constants.qnSelectionFailure, errmsg);
        }

        if (lValue.getNodeType() == Node.ELEMENT_NODE) {
            // This is a bit hokey, we concatenate all the children's values; we really should be checking
            // to make sure that we are only dealing with text and attribute nodes.
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

    public Node getPartData(Element message, OMessageVarType.Part part) {
        // borrowed from ASSIGN.evalQuery()
        Node ret = DOMUtils.findChildByName(message, new QName(null, part.name));
        if (part.type instanceof OElementVarType) {
            ret = DOMUtils.getFirstChildElement((Element) ret);
        } else if (part.type == null) {
            // Special case of header parts never referenced in the WSDL def
            if (ret != null && ret.getNodeType() == Node.ELEMENT_NODE
                    && ((Element)ret).getAttribute("headerPart") != null
                    && DOMUtils.getTextContent(ret) == null)
                ret = DOMUtils.getFirstChildElement((Element) ret);
            // The needed part isn't there, dynamically creating it
            if (ret == null) {
                ret = message.getOwnerDocument().createElementNS(null, part.name);
                ((Element)ret).setAttribute("headerPart", "true");
                message.appendChild(ret);
            }
        }

        return ret;
    }

    /**
     * @param instance
     * @param operation
     * @param outboundMsg
     * @param object
     */
    public String invoke(String invokeId, PartnerLinkInstance instance, Operation operation, Element outboundMsg, Object object)
            throws FaultException {
        try {
            return _brc.invoke(invokeId, instance, operation, outboundMsg);
        } catch (UninitializedPartnerEPR e) {
            throw new FaultException(_runtime._oprocess.constants.qnUninitializedPartnerRole);
        }
    }

    public String invoke(String requestId, org.apache.ode.bpel.iapi.Resource resource, Element outgoingMessage)
            throws FaultException {
        return _brc.invoke(requestId, resource, outgoingMessage);
    }

    /**
     * @return
     */
    public ExpressionLanguageRuntimeRegistry getExpLangRuntime() {
        return _runtime._expLangRuntimeRegistry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ode.bpel.engine.rapi.OdeInternalInstance#onMyRoleMessageExchange(java.lang.String, java.lang.String)
     */
    public void onSelectEvent(final String selectId, final String messageExchangeId, final int selectorIdx) {
//        getORM().associate(selectId, messageExchangeId, selectorIdx);

        _vpu.inject(new JacobRunnable() {
            private static final long serialVersionUID = 3168964409165899533L;

            public void run() {
                // NOTE: we chose the selectId to be the exported representation of the pick response channel!
                PickResponseChannel responseChannel = importChannel(selectId, PickResponseChannel.class);
                responseChannel.onRequestRcvd(selectorIdx, messageExchangeId);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ode.bpel.engine.rapi.OdeInternalInstance#onTimerEvent(java.lang.String)
     */
    public void onTimerEvent(final String timerId) {
        getORM().cancel(timerId);

        _vpu.inject(new JacobRunnable() {
            private static final long serialVersionUID = -7767141033611036745L;

            public void run() {
                // NOTE: note short cut, we chose timer id to be the same as the exported channel representation.
                TimerResponseChannel responseChannel = importChannel(timerId, TimerResponseChannel.class);
                responseChannel.onTimeout();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ode.bpel.engine.rapi.OdeInternalInstance#execute()
     */
    public boolean execute() {
        return _vpu.execute();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ode.bpel.engine.rapi.OdeInternalInstance#onInvokeResponse(java.lang.String, java.lang.String)
     */
    public void onInvokeResponse(final String invokeId, InvokeResponseType irt, final String mexid) {
        // NOTE: do the switch outside the inject, since we don't want to end up serializing InvokeResponseType objects!
        switch (irt) {
        case REPLY:
            _vpu.inject(new BpelJacobRunnable() {
                private static final long serialVersionUID = -1095444335740879981L;

                public void run() {
                    importChannel(invokeId, InvokeResponseChannel.class).onResponse();
                }
            });
            break;
        case FAULT:
            _vpu.inject(new BpelJacobRunnable() {
                private static final long serialVersionUID = -1095444335740879981L;

                public void run() {
                    importChannel(invokeId, InvokeResponseChannel.class).onFault();
                }
            });
            break;
        case FAILURE:
            _vpu.inject(new BpelJacobRunnable() {
                private static final long serialVersionUID = -1095444335740879981L;

                public void run() {
                    importChannel(invokeId, InvokeResponseChannel.class).onFailure();
                }
            });
            break;
        }
    }

    public void recoverActivity(final String channel, final long activityId, final String action, FaultInfo fault) {
        // TODO: better translation here?
        final FaultData fdata = (fault != null) ? new FaultData(fault.getFaultName(), null, fault.getExplanation()) : null;

        _vpu.inject(new JacobRunnable() {
            private static final long serialVersionUID = 3168964409165899533L;

            public void run() {
                ActivityRecoveryChannel recovery = importChannel(channel, ActivityRecoveryChannel.class);
                __log.info("ActivityRecovery: Recovering activity " + activityId +
                        " with action " + action + " on channel " + recovery);
                if (recovery != null) {
                    if ("cancel".equals(action)) recovery.cancel();
                    else if ("retry".equals(action)) recovery.retry();
                    else if ("fault".equals(action)) recovery.fault(fdata);
                }
            }
        });
    }

    private OutstandingRequestManager getORM() {
        return (OutstandingRequestManager) _soup.getGlobalData();
    }

    /**
     * Called when the process completes to clean up any outstanding message exchanges.
     * 
     */
    private void cleanupOutstandingMyRoleExchanges(FaultInfo optionalFaultData) {
        // TODO: all this should be moved into the engine. We don't really need the ORM to find
        // these mexs, we can just scan the database
        String[] mexRefs = getORM().releaseAll();
        for (String mexId : mexRefs) {
            _brc.noreply(mexId, optionalFaultData);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ode.bpel.engine.rapi.OdeInternalInstance#saveState()
     */
    public Object saveState(OutputStream bos) throws IOException {
        if (bos != null) _soup.write(bos);
        return _soup;
    }
    
    /* (non-Javadoc)
     * @see org.apache.ode.bpel.engine.rapi.OdeInternalInstance#createInstance(java.lang.String)
     */
    public void onCreateInstance(String messageExchangeId) {
        _vpu.inject(new PROCESS(_runtime._oprocess));
    }

    /* (non-Javadoc)
     * @see org.apache.ode.bpel.engine.rapi.OdeInternalInstance#setContext(org.apache.ode.bpel.engine.rapi.OdeRTInstanceContext)
     */
    public void setContext(OdeRTInstanceContext ctx) {
        _brc = ctx;
    }

    public URI getBaseResourceURI() {
        return _runtime._pconf.getBaseURI();
    }
    
    public int getRetryDelay() {
    	return _brc.getAtomicScopeRetryDelay();
    }

	public boolean isFirstTry() {
		return _brc.isAtomicScopeFirstTry();
	}

	public boolean isRetryable() {
		return _brc.isAtomicScopeRetryable();
	}

	public void setRetriedOnce() {
		_brc.setAtomicScopeRetriedOnce();		
	}

	public void setRetriesDone() {
		_brc.setAtomicScopeRetriesDone();
	}

	public void setAtomicScope(boolean atomicScope) {
		_brc.setAtomicScope(atomicScope);
	}
	
	public Node getProcessProperty(QName propertyName) {
		return _brc.getProcessProperty(propertyName);
	}
}
