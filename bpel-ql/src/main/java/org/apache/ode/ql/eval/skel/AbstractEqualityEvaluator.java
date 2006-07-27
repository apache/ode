package org.apache.ode.ql.eval.skel;

public abstract class AbstractEqualityEvaluator<ID, R, PARAMC>  extends AbstractCMPEvaluator<ID, R, PARAMC> implements EqualityEvaluator<ID, R, PARAMC> {

    /**
     * @param identifier
     */
    public AbstractEqualityEvaluator(ID identifier) {
        super(identifier);
    }
    

}
