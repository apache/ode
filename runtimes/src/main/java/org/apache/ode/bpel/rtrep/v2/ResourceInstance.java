package org.apache.ode.bpel.rtrep.v2;

import org.apache.ode.bpel.rapi.ResourceModel;
import org.apache.ode.bpel.rapi.Resource;
import org.apache.ode.utils.ObjectPrinter;

import java.io.Serializable;

/**
 * Serializable reference to a resource instance.
 */
public class ResourceInstance implements Serializable, Resource {

    private Long scopeInstanceId;
    private OResource resource;

    public ResourceInstance(Long scopeInstanceId, OResource resource) {
        this.scopeInstanceId = scopeInstanceId;
        this.resource = resource;
    }

    public String getName() {
        return resource.getName();
    }

    public Long getScopeInstanceId() {
        return scopeInstanceId;
    }

    public ResourceModel getModel() {
        return resource;
    }

    @Override
    public int hashCode() {
        return this.scopeInstanceId.hashCode() ^ this.resource.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        ResourceInstance other = (ResourceInstance) obj;
        return scopeInstanceId.equals(other.getScopeInstanceId()) && resource.equals(other.getModel());
    }

    @Override
    public String toString() {
        return ObjectPrinter.toString(this, new Object[] { "resourceDecl", resource, "scopeInstanceId",
                scopeInstanceId });
    }
}
