package com.fs.pxe.ql.eval.skel;

public interface CommandEvaluator<R, PARAMC> {
    public R evaluate(PARAMC paramValue);
}

