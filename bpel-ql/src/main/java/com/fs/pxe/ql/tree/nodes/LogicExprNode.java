package com.fs.pxe.ql.tree.nodes;

import java.util.Collection;

public interface LogicExprNode extends Node {
    public Collection<LogicNode> getChilds();
}
