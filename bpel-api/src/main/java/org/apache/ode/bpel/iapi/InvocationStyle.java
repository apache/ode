package org.apache.ode.bpel.iapi;

/**
 * Style of invocation supported by the integration layer for a given operation.
 * 
 * @author Maciej Szefler
 */
public enum InvocationStyle {
    /**
     * Reliable style -- the IL/engine will queue the invocation using the current transaction. The response will be
     * delivered when available using a separate transaction. 
     */
    RELIABLE,
    
    
    /**
     * Transacted style -- the IL/engine will enroll the operation with the current transaction. The IL/engine will
     * block until the operation completes. 
     */
    TRANSACTED, 
    
    /**
     * Unreliable style -- the "default"
     */
    UNRELIABLE, 
    
    /**
     * Process-2-Process, used when "including" one process in another. 
     */
    P2P,
    
    /**
     * Process-2-Process, used when "including" one process in another.
     * Transacted style -- the IL/engine will enroll the operation with the current transaction. The IL/engine will
     * block until the operation completes. 
     */
    P2P_TRANSACTED
    
}