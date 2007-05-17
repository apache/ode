package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.CompensateScopeActivity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OCompensate;
import org.apache.ode.utils.msg.MessageBundle;


/**
 * Generates code for the <code>&lt;compensateScope&gt;</code> activities.
 */
class CompensateScopeGenerator extends DefaultActivityGenerator {
    private static final CompensateGeneratorMessages __cmsgs = MessageBundle.getMessages(CompensateGeneratorMessages.class);
    
    public void compile(OActivity output, Activity src) {
        CompensateScopeActivity compSrc = (CompensateScopeActivity) src;
        if (compSrc.getScopeToCompensate() == null)
            throw new CompilationException(__cmsgs.errScopeToCompensateUnspecfied());
        ((OCompensate)output).compensatedScope = _context.resolveCompensatableScope(compSrc.getScopeToCompensate());
    }

    public OActivity newInstance(Activity src) {
        return new OCompensate(_context.getOProcess(), _context.getCurrent());
    }
}

