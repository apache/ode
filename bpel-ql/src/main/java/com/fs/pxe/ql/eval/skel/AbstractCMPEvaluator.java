package com.fs.pxe.ql.eval.skel;

public abstract class AbstractCMPEvaluator<ID, R, PARAMC> implements CMPEvaluator<ID, R, PARAMC> {
    protected final ID identifier;
    
    protected AbstractCMPEvaluator(ID identifier) {
        this.identifier = identifier;
    }
    /**
     * @see com.fs.pxe.ql.eval.skel.Identified#getIdentifier()
     */
    public ID getIdentifier() {
        return identifier;
    }
}
