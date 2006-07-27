package org.apache.ode.ql;

import org.apache.ode.ql.eval.skel.CommandEvaluator;
import org.apache.ode.ql.tree.nodes.Query;

public abstract class Compiler<R, PARAMC> {
    public abstract CommandEvaluator<R, PARAMC> compile(Query query);
}
