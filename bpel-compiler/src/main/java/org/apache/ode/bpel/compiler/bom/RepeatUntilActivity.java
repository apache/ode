package org.apache.ode.bpel.compiler.bom;

import org.w3c.dom.Element;

/**
 * Representation of the <code>repeatUntil</code> BPEL 2.0 activity.
 * @author Maciej Szefler (m s z e f l e r @ g m a i l . c o m)
 */
public class RepeatUntilActivity extends Activity {
    
    public RepeatUntilActivity(Element el) {
        super(el);
    }

    /**
     * Get the child (repeated) activity.
     * 
     * @return repeated activity
     */
    public Activity getActivity() {
        return getFirstChild(Activity.class);
    }

    /**
     * Get the repeat-until condition.
     * 
     * @return the repeat-until condition
     */
    public Expression getCondition() {
        return getFirstChild(Expression.class);
    }
}
