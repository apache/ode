package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.v2.BpelCompiler20;
import org.apache.ode.bpel.compiler.v2.BpelCompiler20Draft;
import org.apache.ode.bpel.compiler.v2.BpelCompiler11;
import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.utils.msg.MessageBundle;

import java.net.URI;

public class BpelCompilerFactory {

    private static final CommonCompilationMessages __cmsgs =
            MessageBundle.getMessages(CommonCompilationMessages.class);

    /**
     * Creates a new compiler for the latest version of the internal BPEL model and the provided version
     * of the BPEL specification.
     */
    public static BpelCompiler latestCompiler(BpelCompiler.Version bpelVersion) {
        BpelCompiler compiler;
        switch (bpelVersion) {
            case BPEL20:
                compiler = new BpelCompiler20();
                break;
            case BPEL20_DRAFT:
                compiler = new BpelCompiler20Draft();
                break;
            case BPEL11:
                compiler = new BpelCompiler11();
                break;
            default:
                CompilationMessage cmsg = __cmsgs.errUnrecognizedBpelVersion();
                throw new CompilationException(cmsg);
        }

        return compiler;
    }

    /**
     * Creates a new compiler for the provided version of the internal BPEL model and the provided version
     * of the BPEL specification.
     */
    public static BpelCompiler versionCompiler(BpelCompiler.Version bpelVersion, int version) {
        // TODO switch on the version when we'll have more than one
        return latestCompiler(bpelVersion);
    }
}
