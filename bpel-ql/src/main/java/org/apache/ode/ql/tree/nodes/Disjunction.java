package org.apache.ode.ql.tree.nodes;

import java.util.Collection;

public class Disjunction extends AbstractLogicExpr {

    /**
     * @param childs
     */
    public Disjunction(Collection<LogicNode> childs) {
        super(childs);
    }
    
}
