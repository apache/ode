package org.apache.ode.bpel.rtrep.v2;

/**
 * RESTful read activity. Provides an idempotent GET method on a given resource.
 */
public class OCollect extends OActivity {

    private OResource resource;
    private OScope.Variable variable;

    public OCollect(OProcess owner, OActivity parent) {
        super(owner, parent);
    }

    public OResource getResource() {
        return resource;
    }

    public void setResource(OResource resource) {
        this.resource = resource;
    }

    public OScope.Variable getVariable() {
        return variable;
    }

    public void setVariable(OScope.Variable variable) {
        this.variable = variable;
    }
}
