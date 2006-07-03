package com.fs.pxe.ql;

import com.fs.pxe.ql.eval.skel.CommandEvaluator;
import com.fs.pxe.ql.tree.nodes.Query;

public abstract class Compiler<R, PARAMC> {
    public abstract CommandEvaluator<R, PARAMC> compile(Query query);
}
