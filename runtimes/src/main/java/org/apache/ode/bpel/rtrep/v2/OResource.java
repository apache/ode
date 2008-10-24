package org.apache.ode.bpel.rtrep.v2;

import org.apache.ode.bpel.rapi.ResourceModel;

/**
 * RESTful resource representation at the process level
 */
public class OResource extends OBase implements ResourceModel {

    private String name;
    private OExpression subpath;
    private OResource reference;
    private String method;
    private boolean instantiateResource;

    public OResource(OProcess owner) {
        super(owner);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OExpression getSubpath() {
        return subpath;
    }

    public void setSubpath(OExpression subpath) {
        this.subpath = subpath;
    }

    public OResource getReference() {
        return reference;
    }

    public void setReference(OResource reference) {
        this.reference = reference;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isInstantiateResource() {
        return instantiateResource;
    }

    public void setInstantiateResource(boolean instantiateResource) {
        this.instantiateResource = instantiateResource;
    }
}
