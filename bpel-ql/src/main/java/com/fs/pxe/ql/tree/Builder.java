package com.fs.pxe.ql.tree;

public abstract class Builder<ET> {
    public abstract com.fs.pxe.ql.tree.nodes.Node build(ET expr);
}
