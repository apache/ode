package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.rapi.PartnerLinkModel;
import org.apache.ode.bpel.rapi.ProcessModel;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
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

    void activate(Contexts contexts) {
        _contexts = contexts;
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
                _processModel = deserializeCompiledProcess(_pconf.getCBPInputStream());
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

            if (isInMemory()) {
                bounceProcessDAO(_inMemDao.getConnection(), _pid, _pconf.getVersion(), _processModel);
            } else if (_contexts.isTransacted()) {
                // If we have a transaction, we do this in the current transaction.
                bounceProcessDAO(_contexts.dao.getConnection(), _pid, _pconf.getVersion(), _processModel);
            } else {
                // If we do not have a transaction we need to create one.
                try {
                    _contexts.execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            bounceProcessDAO(_contexts.dao.getConnection(), _pid, _pconf.getVersion(), _processModel);
                            return null;
                        }
                    });
                } catch (Exception ex) {
                    String errmsg = "DbError";
                    __log.error(errmsg, ex);
                    throw new BpelEngineException(errmsg, ex);
                }
            }
        }

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
