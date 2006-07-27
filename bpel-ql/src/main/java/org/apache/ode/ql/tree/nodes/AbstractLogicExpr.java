package org.apache.ode.ql.tree.nodes;

import java.util.Collection;

public class AbstractLogicExpr implements LogicExprNode, LogicNode {
    private final Collection<LogicNode> childs;

    /**
     * @param childs
     */
    protected AbstractLogicExpr(final Collection<LogicNode> childs) {
        super();
        this.childs = childs;
    }


    /**
     * @see org.apache.ode.ql.tree.nodes.LogicExprNode#getChilds()
     */
    public Collection<LogicNode> getChilds() {
        return childs;
    }

}
