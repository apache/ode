package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.utils.msg.MessageBundle;

public class BpelCompilerFactory {

    private static final CommonCompilationMessages __cmsgs =
            MessageBundle.getMessages(CommonCompilationMessages.class);

    // Ugly hack to facilitate testing (at least for now)
    public static int forceVersion = -1;

    /**
     * Creates a new compiler for the latest version of the internal BPEL model and the provided version
     * of the BPEL specification.
     */
    public static BpelCompiler latestCompiler(BpelCompiler.Version bpelVersion) throws Exception {
        if (forceVersion > 0) return versionCompiler(bpelVersion, forceVersion);
        else return versionCompiler(bpelVersion, 2);
    }

    /**
     * Creates a new compiler for the provided version of the internal BPEL model and the provided version
     * of the BPEL specification.
     */
    public static BpelCompiler versionCompiler(BpelCompiler.Version bpelVersion, int version) throws Exception {
        BpelCompiler compiler = null;
        switch (version) {
            case 1:
                switch (bpelVersion) {
                    case BPEL20:
                        compiler = new org.apache.ode.bpel.compiler.v1.BpelCompiler20();
                        break;
                    case BPEL20_DRAFT:
                        compiler = new org.apache.ode.bpel.compiler.v1.BpelCompiler20Draft();
                        break;
                    case BPEL11:
                        compiler = new org.apache.ode.bpel.compiler.v1.BpelCompiler11();
                        break;
                    default:
                        CompilationMessage cmsg = __cmsgs.errUnrecognizedBpelVersion();
                        throw new CompilationException(cmsg);
                }
                break;
            case 2:
                switch (bpelVersion) {
                    case BPEL20:
                        compiler = new org.apache.ode.bpel.compiler.v2.BpelCompiler20();
                        break;
                    case BPEL20_DRAFT:
                        compiler = new org.apache.ode.bpel.compiler.v2.BpelCompiler20Draft();
                        break;
                    case BPEL11:
                        compiler = new org.apache.ode.bpel.compiler.v2.BpelCompiler11();
                        break;
                    default:
                        CompilationMessage cmsg = __cmsgs.errUnrecognizedBpelVersion();
                        throw new CompilationException(cmsg);
                }
                break;
            default:
                throw new RuntimeException("Non existant version: " + version);
        }
        return compiler;
    }
}
