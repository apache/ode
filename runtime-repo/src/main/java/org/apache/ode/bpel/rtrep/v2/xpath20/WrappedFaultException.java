package org.apache.ode.bpel.rtrep.v2.xpath20;

import org.apache.ode.bpel.common.FaultException;

public class WrappedFaultException extends RuntimeException {
    private static final long serialVersionUID = -2677245631724501573L;

    public FaultException _fault;

    public WrappedFaultException(String message) {
        super(message);
    }

    public WrappedFaultException(FaultException message) {
        super(message);
        _fault = message;
    }

    public WrappedFaultException(String message, FaultException cause) {
        super(message, cause);
        _fault = cause;
    }

}
