package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.rapi.PartnerLinkModel;
import org.apache.ode.bpel.rapi.ProcessModel;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.utils.GUID;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.wsdl.Operation;
import java.util.*;
import java.util.concurrent.Callable;

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

    private MyRoleMessageExchangeCache _myRoleMexCache;

    ODEWSProcess(BpelServerImpl server, ProcessConf conf, BpelEventListener debugger, MyRoleMessageExchangeCache mexCache) {
        super(server, conf, debugger);
        _hydrationLatch = new HydrationLatch();
        _myRoleMexCache = mexCache;
    }

    private PartnerLinkMyRoleImpl getMyRoleForService(QName serviceName) {
        assert isLatched(1);
        for (Map.Entry<Endpoint, PartnerLinkMyRoleImpl> e : _endpointToMyRoleMap.entrySet()) {
            if (e.getKey().serviceName.equals(serviceName))
                return e.getValue();
        }
        return null;
    }

    void activate() {
        _debugger = new DebuggerSupport(this);

        __log.debug("Activating endpoints for " + _pid);
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
            readModel();
            
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

        private void readModel() {
            try {
                _processModel = _pconf.getProcessModel();
            } catch (Exception e) {
                // Swallow, was just trying
            }
            if (_processModel == null) {
                try {
                    _processModel = deserializeCompiledProcess(_pconf.getCBPInputStream());
                } catch (Exception e) {
                    String errmsg = "Error reloading compiled process " + _pconf.getProcessId() + "; the file appears to be corrupted.";
                    __log.error(errmsg);
                    throw new BpelEngineException(errmsg, e);
                }
            }
        }

    }

    /**
     * Entry point for message exchanges aimed at the my role.
     * @param mexdao
     */
    void invokeProcess(final MessageExchangeDAO mexdao) {
        InvocationStyle istyle = mexdao.getInvocationStyle();

        latch(1);
        try {
            // The following check is mostly for sanity purposes. MexImpls should prevent this from
            // happening.
            PartnerLinkMyRoleImpl target = getMyRoleForService(mexdao.getCallee());
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
                } else /* non-transacted style */{
                    WorkEvent we = new WorkEvent();
                    we.setType(WorkEvent.Type.MYROLE_INVOKE);
                    we.setIID(mexdao.getInstance().getInstanceId());
                    we.setMexId(mexdao.getMessageExchangeId());
                    // Could be different to this pid when routing to an older version
                    we.setProcessId(mexdao.getInstance().getProcess().getProcessId());

                    scheduleWorkEvent(we, null);
                }
            } else if (cstatus == MyRoleMessageExchange.CorrelationStatus.QUEUED) {
                ; // do nothing
            }
        } finally {
            releaseLatch(1);

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

        _myRoleMexCache.put(mex);
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
        return _myRoleMexCache.get(mexdao, this); // this will re-create if necessary
    }

    /**
     * Create (or recreate) a {@link MyRoleMessageExchangeImpl} object from data in the db. This method is used by the
     * {@link MyRoleMessageExchangeCache} to re-create objects when they are not found in the cache.
     *
     * @param mexdao
     * @return
     */
    MyRoleMessageExchangeImpl recreateMyRoleMex(MessageExchangeDAO mexdao) {
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
            ODEProcess caller = _server.getBpelProcess(mexdao.getPipedPID());
            // process no longer deployed....
            if (caller == null) return;

            MessageExchangeDAO pmex = caller.loadMexDao(mexdao.getPipedMessageExchangeId());
            // Mex no longer there.... odd..
            if (pmex == null) return;

            // Need to copy the response and state from myrolemex --> partnerrolemex
            boolean compat = !(caller.isInMemory() ^ isInMemory());
            if (compat) {
                // both processes are in-mem or both are persisted, can share the message
                pmex.setResponse(mexdao.getResponse());
            } else /* one process in-mem, other persisted */{
                MessageDAO presponse = pmex.createMessage(mexdao.getResponse().getType());
                presponse.setData(mexdao.getResponse().getData());
                presponse.setHeader(mexdao.getResponse().getHeader());
                pmex.setResponse(presponse);
            }
            pmex.setStatus(mexdao.getStatus());
            pmex.setAckType(mexdao.getAckType());
            pmex.setFailureType(mexdao.getFailureType());

            if (old == MessageExchange.Status.ASYNC) caller.p2pWakeup(pmex);
        } else /* not p2p */{
            // Do an Async wakeup if we are in the ASYNC state. If we're not, we'll pick up the ACK when we unwind
            // the stack.
            if (old == MessageExchange.Status.ASYNC) {
                MyRoleMessageExchangeImpl mymex = _myRoleMexCache.get(mexdao, this);
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
