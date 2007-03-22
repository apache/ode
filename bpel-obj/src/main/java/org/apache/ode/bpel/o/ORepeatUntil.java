package org.apache.ode.bpel.o;


/**
 * Compiled representation of a <code>&lt;repeatUntil&gt;</code> activity.
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
public class ORepeatUntil extends OActivity {

    static final long serialVersionUID = -1L  ;
    
    /** The repeat until condition. */
    public OExpression untilCondition;

    public OActivity activity;

    public ORepeatUntil(OProcess owner, OActivity parent) {
        super(owner, parent);
    }
}

