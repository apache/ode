package org.apache.ode.bpel.rtrep.v2;

/**
 * RESTful resource representation at the process level
 */
public class OResource extends OBase {

    private String name;
    private OExpression subpath;
    private OResource reference;

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

}
