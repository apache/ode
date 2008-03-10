package org.apche.ode.bpel.evar;

/**
 * Exception thrown by external variable engines.
 *  
 * @author Maciej Szefler <mszefler at gmail dot com>
 */
public class ExternalVariableModuleException extends Exception {

    private static final long serialVersionUID = 1L;

    
    public ExternalVariableModuleException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public ExternalVariableModuleException(String msg) {
        super(msg);
    }
}
