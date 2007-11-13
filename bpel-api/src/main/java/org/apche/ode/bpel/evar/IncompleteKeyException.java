package org.apche.ode.bpel.evar;

import java.util.Collection;

/**
 * Exception used to indicate that an attempt was made to access a variable using an incomplete key.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
public class IncompleteKeyException extends ExternalVariableModuleException {
    
    private static final long serialVersionUID = 1L;
    private Collection<String>_missing;

    public IncompleteKeyException(Collection<String> missing) {
        super("Attempt to read external variable with an incomplete compoung key. " +
                "The following components were missing: " + missing);
        
        _missing = missing;
    }
    
    public Collection<String> getMissing() {
        return _missing;
    }
}
