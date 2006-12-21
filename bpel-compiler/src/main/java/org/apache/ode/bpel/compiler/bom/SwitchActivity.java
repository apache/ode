package org.apache.ode.bpel.compiler.bom;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Legacy (BPEL 1.1) representation of a <code>&lt;switch&gt;</code> activity.
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class SwitchActivity extends Activity {

    public SwitchActivity(Element el) {
        super(el);
    }

    /**
     * Get the cases for this switch.
     * 
     * @return the cases
     */
    public List<Case> getCases() {
        return getChildren(Case.class);
    }

    /**
     * BPEL object model representation of <code>&lt;case&gt;</code> and 
     * <code>&lt;otherwise&gt;</code> elements. Note that the 
     * <code>&lt;otherwise&gt;</code> elements simply return null for
     * the {@link #getCondition()}.
     */
    public static class Case extends BpelObject {

        public Case(Element el) {
            super(el);
        }

        /**
         * Get the activity for this case.
         * 
         * @return activity enabled when case is satisfied
         */
        public Activity getActivity() {
            return getFirstChild(Activity.class);
        }

        /**
         * Get the condition associated with this case.
         * 
         * @return the condition
         */
        public Expression getCondition() {
            return isAttributeSet("condition") 
                ? new Expression11(getElement(),getElement().getAttributeNode("condition")) 
                    : null;
        }

    }
    
}
