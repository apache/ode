package org.apache.ode.ql.tree;

public abstract class Builder<ET> {
    public abstract org.apache.ode.ql.tree.nodes.Node build(ET expr);
}
