package com.fs.pxe.ql.tree.nodes;

import java.util.Collection;

public class Conjunction extends AbstractLogicExpr {

    /**
     * @param childs
     */
    public Conjunction(final Collection<LogicNode> childs) {
        super(childs);
    }


    
}
