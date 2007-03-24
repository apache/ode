package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.RepeatUntilActivity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.ORepeatUntil;

/**
 * Generates code for <code>&lt;while&gt;</code> activities.
 */
class RepeatUntilGenerator extends DefaultActivityGenerator {
    public OActivity newInstance(Activity src) {
        return new ORepeatUntil(_context.getOProcess(), _context.getCurrent());
    }

    public void compile(OActivity output, Activity srcx)  {
        ORepeatUntil oru = (ORepeatUntil) output;
        RepeatUntilActivity src = (RepeatUntilActivity)srcx;
        oru.untilCondition = _context.compileExpr(src.getCondition());
        oru.activity = _context.compile(src.getActivity());
    }
}

