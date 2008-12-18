package org.apache.ode.bpel.rtrep.v2.xpath10.jaxp;

import org.apache.ode.bpel.common.FaultException;

/**
 * wrapper for ODE exceptions thrown during XPath evaluation.
 */
public class WrappedFaultException extends RuntimeException {
    private static final long serialVersionUID = -3575585514576583418L;

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
