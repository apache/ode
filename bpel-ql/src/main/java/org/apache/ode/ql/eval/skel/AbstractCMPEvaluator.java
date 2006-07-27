package org.apache.ode.ql.eval.skel;

public abstract class AbstractCMPEvaluator<ID, R, PARAMC> implements CMPEvaluator<ID, R, PARAMC> {
    protected final ID identifier;
    
    protected AbstractCMPEvaluator(ID identifier) {
        this.identifier = identifier;
    }
    /**
     * @see org.apache.ode.ql.eval.skel.Identified#getIdentifier()
     */
    public ID getIdentifier() {
        return identifier;
    }
}
