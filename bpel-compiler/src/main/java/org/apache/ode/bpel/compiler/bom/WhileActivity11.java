package org.apache.ode.bpel.compiler.bom;

import org.w3c.dom.Element;

/**
 * BPEL 1.1 version of the /while/ activity.
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class WhileActivity11 extends WhileActivity {

    public WhileActivity11(Element el) {
        super(el);
    }

    @Override
    public Expression getCondition() {
        // BPEL 1.1 has the condition in an attribute, not an element.
        return isAttributeSet("condition") ? 
                new Expression11(getElement(), getElement().getAttributeNode("condition")) : null;
    }

    
}
