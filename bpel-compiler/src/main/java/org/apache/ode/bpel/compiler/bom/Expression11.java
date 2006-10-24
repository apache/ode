package org.apache.ode.bpel.compiler.bom;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * BPEL-1.1 overrides for the expression object. In BPEL 1.1 we had these things appear in an 
 * attribute, so we'll return the attribute node for the expression node. 
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class Expression11 extends Expression {

    private Node _expression;

    public Expression11(Element el, Node expression) {
        super(el);
        _expression = expression;
    }

    public Node getExpression() {
        return _expression;
    }

    public String getExpressionLanguage() {
        return getAttribute("queryLanguage", null);
    }
    

}
