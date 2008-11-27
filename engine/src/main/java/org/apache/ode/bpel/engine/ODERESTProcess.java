package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.rapi.ResourceModel;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ResourceRouteDAO;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.runtime.InvalidProcessException;
import org.apache.ode.bpel.evt.NewProcessInstanceEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Callable;

public class ODERESTProcess extends ODEProcess {

    private ConcurrentHashMap<ResourceModel,String> _staticResources = new ConcurrentHashMap<ResourceModel,String>();

    private ArrayList<Resource> _resources = new ArrayList<Resource>();

    public ODERESTProcess(BpelServerImpl server, ProcessConf conf, BpelEventListener debugger, IncomingMessageExchangeCache mexCache) {
        super(server, conf, debugger, mexCache);
        _processModel = conf.getProcessModel();
        _runtime = buildRuntime(_processModel.getModelVersion());
        _runtime.init(_pconf, _processModel);
    }

    public Collection<String> getInitialResourceUrls() {
        if (_staticResources.size() > 0 ) return _staticResources.values();

        // Caching instantiating resource urls as those can be expressions
        ArrayList<String> addresses = new ArrayList<String>();
        for (ResourceModel resourceModel : _processModel.getProvidedResources()) {
            if (resourceModel.isInstantiateResource()) {
                try {
                    String addr = _runtime.extractAddress(resourceModel);
                    addresses.add(addr);
                    _staticResources.put(resourceModel, addr);
                } catch (FaultException e) {
                    throw new BpelEngineException(e);
                }
            }
        }
        return addresses;
    }

    void activate() {
        bounceProcessDAO();

        // Activating instantiating resources
        for (ResourceModel resourceModel : _staticResources.keySet()) {
            Resource resource = new Resource(_staticResources.get(resourceModel),
                    "application/xml", resourceModel.getMethod());
            _contexts.bindingContext.activateProvidedResource(resource);
            _resources.add(resource);
        }
    }

    void deactivate() {
        for (ResourceModel resourceModel : _staticResources.keySet()) {
            Resource resource = new Resource(_staticResources.get(resourceModel),
                    "application/xml", resourceModel.getMethod());
            _contexts.bindingContext.deactivateProvidedResource(resource);
        }
    }

    void invokeProcess(final MessageExchangeDAO mexdao) {
        if (_pconf.getState() == ProcessState.RETIRED) {
            throw new InvalidProcessException("Process is retired.", InvalidProcessException.RETIRED_CAUSE_CODE);
        }
        mexdao.setProcess(getProcessDAO());

        Resource instantiatingResource = getResource(mexdao.getResource());
        InvocationStyle istyle = mexdao.getInvocationStyle();

        if (instantiatingResource != null) {
            ProcessInstanceDAO newInstance = getProcessDAO().createInstance(null);
            newInstance.setInstantiatingUrl(mexdao.getResource());

            // send process instance event
            NewProcessInstanceEvent evt = new NewProcessInstanceEvent(getProcessModel().getQName(),
                    getProcessDAO().getProcessId(), newInstance.getInstanceId());
            evt.setMexId(mexdao.getMessageExchangeId());
            saveEvent(evt, newInstance);

            mexdao.setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.CREATE_INSTANCE.toString());
            mexdao.setInstance(newInstance);

            doInstanceWork(mexdao.getInstance().getInstanceId(), new Callable<Void>() {
                public Void call() {
                    executeCreateInstance(mexdao);
                    return null;
                }
            });
        } else {
            // TODO avoid reloading the resource routing, it's just been loaded by the server on mex creation
            String[] urlMeth = mexdao.getResource().split("~");
            ResourceRouteDAO rr = _contexts.dao.getConnection().getResourceRoute(urlMeth[0], urlMeth[1]);
            // This really should have been caught by the server
            if (rr == null) throw new BpelEngineException("NoSuchResource: " + mexdao.getResource());
            mexdao.setInstance(rr.getInstance());
            mexdao.setChannel(rr.getPickResponseChannel() + "&" + rr.getSelectorIdx());

            if (istyle == InvocationStyle.TRANSACTED) {
                doInstanceWork(mexdao.getInstance().getInstanceId(), new Callable<Void>() {
                    public Void call() {
                        executeContinueInstanceMyRoleRequestReceived(mexdao);
                        return null;
                    }
                });
            } else /* non-transacted style */ {
                WorkEvent we = new WorkEvent();
                we.setType(WorkEvent.Type.MYROLE_INVOKE);
                we.setIID(mexdao.getInstance().getInstanceId());
                we.setMexId(mexdao.getMessageExchangeId());
                // Could be different to this pid when routing to an older version
                we.setProcessId(mexdao.getInstance().getProcess().getProcessId());

                scheduleWorkEvent(we, null);
            }

        }
    }

    void onRestMexAck(MessageExchangeDAO mexdao, MessageExchange.Status old, String url) {
        if (mexdao.getPipedMessageExchangeId() != null) /* p2p */{
            p2pCall(mexdao, old);
        } else /* not p2p */{
            RESTMessageExchangeImpl mymex = (RESTMessageExchangeImpl) _incomingMexCache.get(mexdao, this);
            mymex.getResource().setUrl(url);
            if (old == MessageExchange.Status.ASYNC) {
                // Updating url for instantiating mexs so that the created resource url can be returned to the caller
                mymex.onAsyncAck(mexdao);
            }
        }
    }

    // Restful processes don't lazy load their OModel, they need it right away to access the instantiating resource
    protected void latch(int s) { }
    protected void releaseLatch(int s) { }
    protected boolean isLatched(int s) { return false; }
    protected void hydrate() { }
    protected void dehydrate() { }

    public RESTMessageExchange createRESTMessageExchange(Resource resource, String clientKey) {
        // TODO check the resource matches a provided one
        RESTMessageExchangeImpl mex = new RESTMessageExchangeImpl(this, clientKey, resource);
        _incomingMexCache.put(mex);
        return mex;
    }

    MessageExchangeImpl recreateIncomingMex(MessageExchangeDAO mexdao) {
        Resource resource = getResource(mexdao.getResource());
        return new RESTMessageExchangeImpl(this, mexdao.getMessageExchangeId(), resource);
    }

    public Resource getResource(String url, String method) {
        for (Resource resource : _resources) {
            if (resource.getUrl().equals(url) && resource.getMethod().equals(method)) return resource;
        }
        return null;
    }

    public Resource getResource(String serializedForm) {
        int sep = serializedForm.indexOf("~");
        String url = serializedForm.substring(0, sep);
        String method = serializedForm.substring(sep + 1);

        for (Resource resource : _resources) {
            if (resource.getUrl().equals(url) && resource.getMethod().equals(method)) return resource;
        }
        return null;
    }

    public Resource getInstantiatingUrl(ProcessInstanceDAO instanceDao) {
        return getResource(instanceDao.getInstantiatingUrl());
    }

    protected boolean isInstantiating(Resource res) {
        for (Map.Entry<ResourceModel, String> resourceModel : _staticResources.entrySet()) {
            if (resourceModel.getValue().equals(res.getUrl())
                    && resourceModel.getKey().getMethod().equals(res.getMethod())
                    && resourceModel.getKey().isInstantiateResource()) return true;
        }
        return false;
    }
}
