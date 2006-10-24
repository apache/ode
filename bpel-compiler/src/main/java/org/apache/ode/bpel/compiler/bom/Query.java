package org.apache.ode.bpel.compiler.bom;

import org.w3c.dom.Element;

/**
 * A Query is nothing more than an expression with the expression-language in the "queryLanguage" attribute
 * rather than the "expressionLanguage" attribute.
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class Query extends Expression {

    public Query(Element el) {
        super(el);
    }

    @Override
    public String getExpressionLanguage() {
        return getAttribute("queryLanguage", null);
    }
}
