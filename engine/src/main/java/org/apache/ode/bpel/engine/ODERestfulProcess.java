package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.Resource;
import org.apache.ode.bpel.rapi.ResourceModel;
import org.apache.ode.bpel.dao.MessageExchangeDAO;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ODERestfulProcess extends ODEProcess {

    private ConcurrentHashMap<ResourceModel,String> _providedResources = new ConcurrentHashMap<ResourceModel,String>();

    public ODERestfulProcess(BpelServerImpl server, ProcessConf conf, BpelEventListener debugger) {
        super(server, conf, debugger);
        _processModel = conf.getProcessModel();
    }

    public Collection<String> getResourceUrls() {
        if (_providedResources.size() > 0 ) return _providedResources.values();

        ArrayList<String> addresses = new ArrayList<String>();
        for (ResourceModel resourceModel : _processModel.getProvidedResources()) {
            String addr = _runtime.extractAddress(resourceModel);
            addresses.add(addr);
            _providedResources.put(resourceModel, addr);
        }
        return addresses;
    }

    void activate(Contexts contexts) {
        _contexts = contexts;
        for (ResourceModel resourceModel : _providedResources.keySet()) {
            Resource resource = new Resource(_providedResources.get(resourceModel),
                    "application/xml", resourceModel.getMethod());
            contexts.bindingContext.activateProvidedResource(resource);
        }
    }

    void deactivate() {
        for (ResourceModel resourceModel : _providedResources.keySet()) {
            Resource resource = new Resource(_providedResources.get(resourceModel),
                    "application/xml", resourceModel.getMethod());
            _contexts.bindingContext.deactivateProvidedResource(resource);
        }
    }

    void invokeProcess(MessageExchangeDAO mexdao) {
        // Do stuff
    }

    // Restful processes don't lazy load their OModel, they need it right away to access the instantiating resource
    protected void latch(int s) { }
    protected void releaseLatch(int s) { }
    protected boolean isLatched(int s) { return false; }
    protected void hydrate() { }
    protected void dehydrate() { }

}
