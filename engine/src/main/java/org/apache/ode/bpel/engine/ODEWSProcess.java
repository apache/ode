package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.rapi.PartnerLinkModel;
import org.apache.ode.bpel.rapi.ProcessModel;
import org.apache.ode.bpel.rapi.ConstantsModel;
import org.apache.ode.bpel.rapi.InvalidProcessException;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.bpel.iapi.Scheduler.JobType;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.utils.*;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.wsdl.Operation;
import java.util.*;
import java.util.concurrent.Callable;
import java.io.InputStream;

public class ODEWSProcess extends ODEProcess {

    private volatile Map<PartnerLinkModel, PartnerLinkPartnerRoleImpl> _partnerRoles;

    private volatile Map<PartnerLinkModel, PartnerLinkMyRoleImpl> _myRoles;

    /** Mapping from {"Service Name" (QNAME) / port} to a myrole. */
    private volatile Map<Endpoint, PartnerLinkMyRoleImpl> _endpointToMyRoleMap;

    // Backup hashmaps to keep initial endpoints handy after dehydration
    private Map<Endpoint, EndpointReference> _myEprs = new HashMap<Endpoint, EndpointReference>();

    private Map<Endpoint, EndpointReference> _partnerEprs = new HashMap<Endpoint, EndpointReference>();

    private Map<Endpoint, PartnerRoleChannel> _partnerChannels = new HashMap<Endpoint, PartnerRoleChannel>();

    /** Mapping from a potentially shared endpoint to its EPR */
    private SharedEndpoints _sharedEps;

    // Has the process already been hydrated before?
    private boolean _hydratedOnce = false;

    /** Latch-like thing to control hydration/dehydration. */
    HydrationLatch _hydrationLatch;

    ODEWSProcess(BpelServerImpl server, ProcessConf conf, BpelEventListener debugger, IncomingMessageExchangeCache mexCache) {
        super(server, conf, debugger, mexCache);
        _hydrationLatch = new HydrationLatch();
    }

    private PartnerLinkMyRoleImpl getMyRoleForService(QName serviceName) {
        assert isLatched(1);
        for (Map.Entry<Endpoint, PartnerLinkMyRoleImpl> e : _endpointToMyRoleMap.entrySet()) {
            if (e.getKey().serviceName.equals(serviceName))
                return e.getValue();
        }
        return null;
    }

    void activate(Contexts contexts) {
        _contexts = contexts;
        _sharedEps = _server.getSharedEndpoints();
        _debugger = new DebuggerSupport(this);

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
                    initialEPR = _contexts.bindingContext.activateMyRoleEndpoint(_pid, entry.getValue());
                    _sharedEps.addEndpoint(endpoint, initialEPR);
                    __log.debug("Activated " + _pid + " myrole " + entry.getKey() + ": EPR is " + initialEPR);
                }
                // Increment the reference count on the endpoint
                _sharedEps.incrementReferenceCount(endpoint);
            } else {
                // Create an EPR by physically activating the endpoint
                initialEPR = _contexts.bindingContext.activateMyRoleEndpoint(_pid, entry.getValue());
                __log.debug("Activated " + _pid + " myrole " + entry.getKey() + ": EPR is " + initialEPR);
            }
            _myEprs.put(endpoint, initialEPR);
        }
        __log.debug("Activated " + _pid);

        markused();
    }

    void deactivate() {
        // the BindingContext contains only the endpoints for the latest process version
        if (org.apache.ode.bpel.iapi.ProcessState.ACTIVE.equals(_pconf.getState())) {
            // Deactivate all the my-role endpoints.
            for (Endpoint endpoint : _myEprs.keySet()) {
                // Deactivate the EPR only if there are no more references
                // to this endpoint from any (active) BPEL process.
                if (isShareable(endpoint)) {
                    __log.debug("deactivating shared endpoint " + endpoint);
                    if (!_sharedEps.decrementReferenceCount(endpoint)) {
                        _contexts.bindingContext.deactivateMyRoleEndpoint(endpoint);
                        _sharedEps.removeEndpoint(endpoint);
                    }
                } else {
                    __log.debug("deactivating non-shared endpoint " + endpoint);
                    _contexts.bindingContext.deactivateMyRoleEndpoint(endpoint);
                }
            }
            // TODO Deactivate all the partner-role channels
        } else {
            if (__log.isDebugEnabled()) __log.debug("pid " + _pid + " is not ACTIVE, no endpoints to deactivate");
        }
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

    EndpointReference getInitialPartnerRoleEPR(PartnerLinkModel link) {
        latch(1);
        try {
            PartnerLinkPartnerRoleImpl prole = _partnerRoles.get(link);
            if (prole == null)
                throw new IllegalStateException("Unknown partner link " + link);
            return prole.getInitialEPR();
        } finally {
            releaseLatch(1);
        }
    }

    Endpoint getInitialPartnerRoleEndpoint(PartnerLinkModel link) {
        latch(1);
        try {
            PartnerLinkPartnerRoleImpl prole = _partnerRoles.get(link);
            if (prole == null)
                throw new IllegalStateException("Unknown partner link " + link);
            return prole._initialPartner;
        } finally {
            releaseLatch(1);
        }
    }

    EndpointReference getInitialMyRoleEPR(PartnerLinkModel link) {
        latch(1);
        try {
            PartnerLinkMyRoleImpl myRole = _myRoles.get(link);
            if (myRole == null) throw new IllegalStateException("Unknown partner link " + link);
            return myRole.getInitialEPR();
        } finally {
            releaseLatch(1);
        }
    }

    PartnerRoleChannel getPartnerRoleChannel(PartnerLinkModel partnerLink) {
        latch(1);
        try {
            PartnerLinkPartnerRoleImpl prole = _partnerRoles.get(partnerLink);
            if (prole == null)
                throw new IllegalStateException("Unknown partner link " + partnerLink);
            return prole._channel;
        } finally {
            releaseLatch(1);
        }
    }

    /**
     * Find the partner-link-my-role that corresponds to the given service name.
     *
     * @param serviceName
     *            name of service
     * @return corresponding {@link PartnerLinkMyRoleImpl}
     */
    private PartnerLinkMyRoleImpl getPartnerLinkForService(QName serviceName) {
        assert isLatched(1);

        PartnerLinkMyRoleImpl target = null;
        for (Endpoint endpoint : _endpointToMyRoleMap.keySet())
            if (endpoint.serviceName.equals(serviceName))
                target = _endpointToMyRoleMap.get(endpoint);

        return target;

    }
    
    private void setRoles(ProcessModel oprocess) {
        _partnerRoles = new HashMap<PartnerLinkModel, PartnerLinkPartnerRoleImpl>();
        _myRoles = new HashMap<PartnerLinkModel, PartnerLinkMyRoleImpl>();
        _endpointToMyRoleMap = new HashMap<Endpoint, PartnerLinkMyRoleImpl>();

        // Create myRole endpoint name mapping (from deployment descriptor)
        HashMap<PartnerLinkModel, Endpoint> myRoleEndpoints = new HashMap<PartnerLinkModel, Endpoint>();
        for (Map.Entry<String, Endpoint> provide : _pconf.getProvideEndpoints().entrySet()) {
            PartnerLinkModel plink = oprocess.getPartnerLink(provide.getKey());
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
            PartnerLinkModel plink = oprocess.getPartnerLink(invoke.getKey());
            if (plink == null) {
                String errmsg = "Error in deployment descriptor for process " + _pid + "; reference to unknown partner link "
                        + invoke.getKey();
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }
            __log.debug("Processing <invoke> element for process " + _pid + ": partnerlink " + invoke.getKey() + " --> "
                    + invoke.getValue());
        }

        for (PartnerLinkModel pl : oprocess.getAllPartnerLinks()) {
            if (pl.hasMyRole()) {
                Endpoint endpoint = myRoleEndpoints.get(pl);
                if (endpoint == null && pl.isInitializePartnerRoleSet())
                    throw new IllegalArgumentException("No service name for myRole plink " + pl.getName());
                PartnerLinkMyRoleImpl myRole = new PartnerLinkMyRoleImpl(this, pl, endpoint);
                _myRoles.put(pl, myRole);
                _endpointToMyRoleMap.put(endpoint, myRole);
            }

            if (pl.hasPartnerRole()) {
                Endpoint endpoint = _pconf.getInvokeEndpoints().get(pl.getName());
                if (endpoint == null)
                    throw new IllegalArgumentException(pl.getName() + " must be bound to an endpoint in deloy.xml");
                PartnerLinkPartnerRoleImpl partnerRole = new PartnerLinkPartnerRoleImpl(this, pl, endpoint);
                _partnerRoles.put(pl, partnerRole);
            }
        }
    }

    protected void latch(int s) {
        _hydrationLatch.latch(s);
    }
    protected void releaseLatch(int s) {
        _hydrationLatch.release(s);
    }
    protected boolean isLatched(int s) {
        return _hydrationLatch.isLatched(s);
    }

    class HydrationLatch extends NStateLatch {

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
            _processModel = null;
            _partnerRoles = null;
            _myRoles = null;
            _endpointToMyRoleMap = null;
        }

        private void doHydrate() {
            markused();
            try {
                InputStream inputStream = _pconf.getCBPInputStream();
                try {
                    _processModel = deserializeCompiledProcess(inputStream);
                } finally {
                    inputStream.close();
                }
            } catch (Exception e) {
                String errmsg = "Error reloading compiled process " + _pconf.getProcessId() + "; the file appears to be corrupted.";
                __log.error(errmsg);
                throw new BpelEngineException(errmsg, e);
            }
            _runtime = buildRuntime(_processModel.getModelVersion());
            _runtime.init(_pconf, _processModel);

            setRoles(_processModel);
            initExternalVariables();

            if (!_hydratedOnce) {
                for (PartnerLinkPartnerRoleImpl prole : _partnerRoles.values()) {
                    if (prole._initialPartner != null) {
                        PartnerRoleChannel channel = _contexts.bindingContext.createPartnerRoleChannel(_pid,
                                prole._plinkDef.getPartnerRolePortType(), prole._initialPartner);
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

            bounceProcessDAO();
        }
    }

    /**
     * Entry point for message exchanges aimed at the my role.
     * @param mexdao
     */
    void invokeProcess(final MessageExchangeDAO mexdao) {
        InvocationStyle istyle = mexdao.getInvocationStyle();
        ConstantsModel constants = null;

        _hydrationLatch.latch(1);
        try {
            // The following check is mostly for sanity purposes. MexImpls should prevent this from
            // happening.
            PartnerLinkMyRoleImpl target = getMyRoleForService(mexdao.getCallee());
            constants = target._process.getProcessModel().getConstantsModel();
            MessageExchange.Status oldstatus = mexdao.getStatus();
            if (target == null) {
                String errmsg = __msgs.msgMyRoleRoutingFailure(mexdao.getMessageExchangeId());
                __log.error(errmsg);
                MexDaoUtil.setFailed(mexdao, MessageExchange.FailureType.UNKNOWN_ENDPOINT, errmsg);
                onMyRoleMexAck(mexdao, oldstatus);
                return;
            }

            Operation op = target._plinkDef.getMyRoleOperation(mexdao.getOperation());
            if (op == null) {
                String errmsg = __msgs.msgMyRoleRoutingFailure(mexdao.getMessageExchangeId());
                __log.error(errmsg);
                MexDaoUtil.setFailed(mexdao, MessageExchange.FailureType.UNKNOWN_OPERATION, errmsg);
                onMyRoleMexAck(mexdao, oldstatus);
                return;
            }

            mexdao.setPattern((op.getOutput() == null) ? MessageExchange.MessageExchangePattern.REQUEST_ONLY
                    : MessageExchange.MessageExchangePattern.REQUEST_RESPONSE);
            if (!processInterceptors(mexdao, InterceptorInvoker.__onProcessInvoked)) {
                __log.debug("Aborting processing of mex " + mexdao.getMessageExchangeId() + " due to interceptors.");
                onMyRoleMexAck(mexdao, oldstatus);
                return;
            }

            // "Acknowledge" any one-way invokes
            if (op.getOutput() == null) {
                mexdao.setStatus(MessageExchange.Status.ACK);
                mexdao.setAckType(MessageExchange.AckType.ONEWAY);
                onMyRoleMexAck(mexdao, oldstatus);
            }

            mexdao.setProcess(getProcessDAO());

            markused();
            MyRoleMessageExchange.CorrelationStatus cstatus = target.invokeMyRole(mexdao);
            if (cstatus == null) {
                ; // do nothing
            } else if (cstatus == MyRoleMessageExchange.CorrelationStatus.CREATE_INSTANCE) {
                doInstanceWork(mexdao.getInstance().getInstanceId(), new Callable<Void>() {
                    public Void call() {
                        executeCreateInstance(mexdao);
                        return null;
                    }
                });

            } else if (cstatus == MyRoleMessageExchange.CorrelationStatus.MATCHED) {
                // This should not occur for in-memory processes, since they are technically not allowed to
                // have any <receive>/<pick> elements that are not start activities.
                if (isInMemory())
                    __log.warn("In-memory process " + _pid + " is participating in a non-createinstance exchange!");

                // We don't like to do the work in the same TX that did the matching, since this creates fertile
                // conditions for deadlock in the correlation tables. However if invocation style is transacted,
                // we need to do the work right then and there.

                if (istyle == InvocationStyle.TRANSACTED) {
                    doInstanceWork(mexdao.getInstance().getInstanceId(), new Callable<Void>() {
                        public Void call() {
                            executeContinueInstanceMyRoleRequestReceived(mexdao);
                            return null;
                        }
                    });
                } else if (istyle == InvocationStyle.P2P_TRANSACTED) /* transact p2p invoke in the same thread */ {
                    executeContinueInstanceMyRoleRequestReceived(mexdao);
                } else /* non-transacted style */{
                    JobDetails j = new JobDetails();
                    j.setType(JobType.MYROLE_INVOKE);
                    j.setInstanceId(mexdao.getInstance().getInstanceId());
                    j.setMexId(mexdao.getMessageExchangeId());
                    // Could be different to this pid when routing to an older version
                    j.setProcessId(mexdao.getInstance().getProcess().getProcessId());

                    scheduleJob(j, null);
                }
            } else if (cstatus == MyRoleMessageExchange.CorrelationStatus.QUEUED) {
                ; // do nothing
            }
        } catch (InvalidProcessException ipe) {
            QName faultQName = null;
            if (constants != null) {
                Document document = DOMUtils.newDocument();
                Element faultElement = document.createElementNS(Namespaces.SOAP_ENV_NS, "Fault");
                Element faultDetail = document.createElementNS(Namespaces.ODE_EXTENSION_NS, "fault");
                faultElement.appendChild(faultDetail);
                switch (ipe.getCauseCode()) {
                    case InvalidProcessException.DUPLICATE_CAUSE_CODE:
                        faultQName = constants.getDuplicateInstance();
                        faultDetail.setTextContent("Found a duplicate instance with the same message key");
                        break;
                    case InvalidProcessException.RETIRED_CAUSE_CODE:
                        faultQName = constants.getRetiredProcess();
                        faultDetail.setTextContent("The process you're trying to instantiate has been retired");
                        break;
                    case InvalidProcessException.DEFAULT_CAUSE_CODE:
                    default:
                        faultQName = constants.getUnknownFault();
                        break;
                }
                MexDaoUtil.setFaulted(mexdao, faultQName, faultElement);
            }
        } finally {
            _hydrationLatch.release(1);

            // If we did not get an ACK during this method, then mark this MEX as needing an ASYNC wake-up
            if (mexdao.getStatus() != MessageExchange.Status.ACK) mexdao.setStatus(MessageExchange.Status.ASYNC);

            assert mexdao.getStatus() == MessageExchange.Status.ACK || mexdao.getStatus() == MessageExchange.Status.ASYNC;
        }

    }


    MyRoleMessageExchange createNewMyRoleMex(final InvocationStyle istyle, final QName targetService, final String operation) {
        final String mexId = new GUID().toString();
        latch(1);
        try {
            final PartnerLinkMyRoleImpl target = getPartnerLinkForService(targetService);
            if (target == null)
                throw new BpelEngineException("NoSuchService: " + targetService);
            final Operation op = target._plinkDef.getMyRoleOperation(operation);
            if (op == null)
                throw new BpelEngineException("NoSuchOperation: " + operation);

            return newMyRoleMex(istyle, mexId, target._endpoint.serviceName, target._plinkDef, op);
        } finally {
            releaseLatch(1);
        }
    }

    private MyRoleMessageExchangeImpl newMyRoleMex(InvocationStyle istyle, String mexId, QName target,
                                                   PartnerLinkModel mplink, Operation operation) {
        MyRoleMessageExchangeImpl mex;
        switch (istyle) {
        case RELIABLE:
            mex = new ReliableMyRoleMessageExchangeImpl(this, mexId, mplink, operation, target);
            break;
        case TRANSACTED:
            mex = new TransactedMyRoleMessageExchangeImpl(this, mexId, mplink, operation, target);
            break;
        case UNRELIABLE:
            mex = new UnreliableMyRoleMessageExchangeImpl(this, mexId, mplink, operation, target);
            break;
        default:
            throw new AssertionError("Unexpected invocation style: " + istyle);
        }

        _incomingMexCache.put(mex);
        return mex;
    }

    /**
     * Lookup a {@link MyRoleMessageExchangeImpl} object in the cache, re-creating it if not found.
     *
     * @param mexdao
     *            DB representation of the mex.
     * @return client representation
     */
    MyRoleMessageExchangeImpl lookupMyRoleMex(MessageExchangeDAO mexdao) {
        return (MyRoleMessageExchangeImpl) _incomingMexCache.get(mexdao, this); // this will re-create if necessary
    }

    /**
     * Create (or recreate) a {@link MyRoleMessageExchangeImpl} object from data in the db. This method is used by the
     * {@link IncomingMessageExchangeCache} to re-create objects when they are not found in the cache.
     *
     * @param mexdao
     * @return
     */
    MyRoleMessageExchangeImpl recreateIncomingMex(MessageExchangeDAO mexdao) {
        InvocationStyle istyle = mexdao.getInvocationStyle();

        latch(1);
        try {
            PartnerLinkModel plink = _processModel.getPartnerLink(mexdao.getPartnerLinkModelId());
            if (plink == null) {
                String errmsg = __msgs.msgDbConsistencyError("MexDao #" + mexdao.getMessageExchangeId()
                        + " referenced unknown pLinkModelId " + mexdao.getPartnerLinkModelId());
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }

            Operation op = plink.getMyRoleOperation(mexdao.getOperation());
            if (op == null) {
                String errmsg = __msgs.msgDbConsistencyError("MexDao #" + mexdao.getMessageExchangeId()
                        + " referenced unknown operation " + mexdao.getOperation());
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }

            PartnerLinkMyRoleImpl myRole = _myRoles.get(plink);
            if (myRole == null) {
                String errmsg = __msgs.msgDbConsistencyError("MexDao #" + mexdao.getMessageExchangeId()
                        + " referenced non-existant myrole");
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }

            MyRoleMessageExchangeImpl mex = newMyRoleMex(istyle, mexdao.getMessageExchangeId(), myRole._endpoint.serviceName,
                    plink, op);
            mex.load(mexdao);
            return mex;
        } finally {
            releaseLatch(1);
        }
    }

    PartnerRoleMessageExchangeImpl createPartnerRoleMex(MessageExchangeDAO mexdao) {
        latch(1);
        try {
            PartnerLinkModel plink = _processModel.getPartnerLink(mexdao.getPartnerLinkModelId());
            PartnerLinkPartnerRoleImpl prole = _partnerRoles.get(plink);
            return prole.createPartnerRoleMex(mexdao);
        } finally {
            releaseLatch(1);
        }
    }

    void onMyRoleMexAck(MessageExchangeDAO mexdao, MessageExchange.Status old) {
        if (mexdao.getPipedMessageExchangeId() != null) /* p2p */{
            p2pCall(mexdao, old);
        } else /* not p2p */{
            // Do an Async wakeup if we are in the ASYNC state. If we're not, we'll pick up the ACK when we unwind
            // the stack.
            if (old == MessageExchange.Status.ASYNC) {
                MyRoleMessageExchangeImpl mymex = (MyRoleMessageExchangeImpl) _incomingMexCache.get(mexdao, this);
                mymex.onAsyncAck(mexdao);
                try {
                    _contexts.mexContext.onMyRoleMessageExchangeStateChanged(mymex);
                } catch (Throwable t) {
                    __log.error("Integration layer threw an unexepcted exception.", t);
                }
            }
        }
    }

    void invokePartner(MessageExchangeDAO mexdao) {
        PartnerLinkModel oplink = _processModel.getPartnerLink(mexdao.getPartnerLinkModelId());
        PartnerLinkPartnerRoleImpl partnerRole = _partnerRoles.get(oplink);
        Endpoint partnerEndpoint = getInitialPartnerRoleEndpoint(oplink);
        List<ODEProcess> p2pProcesses = null;
        if (partnerEndpoint != null)
            p2pProcesses = _server.route(partnerEndpoint.serviceName, new DbBackedMessageImpl(mexdao.getRequest()));

        Operation operation = oplink.getPartnerRoleOperation(mexdao.getOperation());

        if (!processInterceptors(mexdao, InterceptorInvoker.__onPartnerInvoked)) {
            __log.debug("Partner invocation intercepted.");
            return;
        }

        mexdao.setStatus(MessageExchange.Status.REQ);
        try {
            if (p2pProcesses != null && p2pProcesses.size() != 0) {
                /* P2P (process-to-process) invocation, special logic */
                // First, make a copy of the original request message
                MessageDAO request = mexdao.getRequest();
                // Then, iterate over each subscribing process
                for (ODEProcess p2pProcess : p2pProcesses) {
                    // Clone the request message for this subscriber
                    MessageDAO clone = mexdao.createMessage(request.getType());
                    clone.setData((Element) request.getData().cloneNode(true));
                    clone.setHeader((Element) request.getHeader().cloneNode(true));
                    // Set the request on the MEX to the clone
                    mexdao.setRequest(clone);
                    // Send the cloned message to the subscribing process
                    invokeP2P(p2pProcess, partnerEndpoint.serviceName, operation, mexdao);
                }
            } else {
                partnerRole.invokeIL(mexdao);
                // Scheduling a verification to see if the invoke has really been processed. Otherwise
                // we put it in activity recovery mode (case of a server crash during invocation).
                scheduleInvokeCheck(mexdao);
            }
        } finally {
            if (mexdao.getStatus() != MessageExchange.Status.ACK)
                mexdao.setStatus(MessageExchange.Status.ASYNC);

        }

        assert mexdao.getStatus() == MessageExchange.Status.ACK || mexdao.getStatus() == MessageExchange.Status.ASYNC;
    }

    /**
     * Invoke a partner process directly (via the engine), bypassing the Integration Layer. Obviously this can only be used when an
     * process is partners with another process hosted on the same engine.
     */
    protected void invokeP2P(ODEProcess target, QName serviceName, Operation operation, MessageExchangeDAO partnerRoleMex) {
        if (ODEProcess.__log.isDebugEnabled())
            __log.debug("Invoking in a p2p interaction, partnerrole " + partnerRoleMex.getMessageExchangeId()
                    + " target=" + target);

        partnerRoleMex.setInvocationStyle(
                Boolean.parseBoolean(
                        partnerRoleMex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_TRANSACTED))
                    ? InvocationStyle.P2P_TRANSACTED
                    : InvocationStyle.P2P);

        // Plumbing
        MessageExchangeDAO myRoleMex = target.createMessageExchange(new GUID().toString(),
                MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE);
        myRoleMex.setStatus(MessageExchange.Status.REQ);
        myRoleMex.setCallee(serviceName);

        myRoleMex.setOperation(partnerRoleMex.getOperation());
        myRoleMex.setPattern(partnerRoleMex.getPattern());
        myRoleMex.setTimeout(partnerRoleMex.getTimeout());
        myRoleMex.setRequest(partnerRoleMex.getRequest());
        myRoleMex.setInvocationStyle(partnerRoleMex.getInvocationStyle());

        // Piped cross-references.
        myRoleMex.setPipedMessageExchangeId(partnerRoleMex.getMessageExchangeId());
        myRoleMex.setPipedPID(getPID());
        partnerRoleMex.setPipedPID(target.getPID());
        partnerRoleMex.setPipedMessageExchangeId(myRoleMex.getMessageExchangeId());

        setStatefulEPRs(partnerRoleMex, myRoleMex);

        // A classic P2P interaction is considered reliable. The invocation should take place
        // in the local transaction but the invoked process is not supposed to hold our thread
        // and the reply should come in a separate transaction.
        target.invokeProcess(myRoleMex);
    }

    protected void scheduleInvokeCheck(MessageExchangeDAO mex) {
        boolean isTwoWay = mex.getPattern() ==
                org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
        if (!isInMemory() && isTwoWay) {
            if (__log.isDebugEnabled()) __log.debug("Creating invocation check event for mexid " + mex.getMessageExchangeId());
            JobDetails job = new JobDetails();
            job.setMexId(mex.getMessageExchangeId());
            job.setProcessId(getPID());
            job.setType(JobType.INVOKE_CHECK);
            // use a greater timeout to make sure the check job does not get executed while the service invocation is still waiting for a response
            PartnerLinkModel model = _processModel.getPartnerLink(mex.getPartnerLinkModelId());
            long timeout = (long) (getTimeout(model)*1.5);
            Date future = new Date(System.currentTimeMillis() + timeout);
            String jobId = scheduleJob(job, future);
            mex.setProperty("invokeCheckJobId", jobId);
        }
    }
    
    public long getTimeout(PartnerLinkModel partnerLink) {
        // OPartnerLink, PartnerLinkPartnerRoleImpl
        final PartnerLinkPartnerRoleImpl linkPartnerRole = _partnerRoles.get(partnerLink);
        long timeout = org.apache.ode.utils.Properties.DEFAULT_MEX_TIMEOUT;
        String timeout_property = _pconf.getEndpointProperties(linkPartnerRole._initialEPR).get(org.apache.ode.utils.Properties.PROP_MEX_TIMEOUT);
        if (timeout_property != null) {
            try {
                timeout = Long.parseLong(timeout_property);
            } catch (NumberFormatException e) {
                if (__log.isWarnEnabled())
                    __log.warn("Mal-formatted Property: [" + org.apache.ode.utils.Properties.PROP_MEX_TIMEOUT + "=" + timeout_property + "] Default value (" + timeout + ") will be used");
            }
        }
        return timeout;
    }

    /**
     * Ask the process to dehydrate.
     */
    void dehydrate() {
        _hydrationLatch.latch(0);
        try {
            // We don't actually need to do anything, the latch will run the doDehydrate method
            // when necessary..
        } finally {
            _hydrationLatch.release(0);
        }
    }

    void hydrate() {
        _hydrationLatch.latch(1);
        try {
            // We don't actually need to do anything, the latch will run the doHydrate method
            // when necessary..
        } finally {
            _hydrationLatch.release(1);
        }
    }

}
