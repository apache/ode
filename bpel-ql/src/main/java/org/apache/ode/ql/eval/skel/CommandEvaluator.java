package org.apache.ode.ql.eval.skel;

public interface CommandEvaluator<R, PARAMC> {
    public R evaluate(PARAMC paramValue);
}

