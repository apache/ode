package org.apache.ode.bpel.compiler.bom;

import org.w3c.dom.Element;

/**
 * External variable mapping. Creates a link between a key of an external variable and a BPEL expression.  
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
public class ExtVarKeyMapping extends BpelObject {

    public ExtVarKeyMapping(Element el) {
        super(el);
    }

    /**
     * This identifies part of the key.
     * @return
     */
    public String getKey() {
        return getAttribute("key", null);
    }
    
    /**
     * This specifies the expression used to pupulate the key. 
     * @return
     */
    public Expression getExpression() {
        return getFirstChild(Expression.class);
    }
}
