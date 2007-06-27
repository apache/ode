package org.apache.ode.bpel.iapi;

/**
 * Style of invocation supported by the integration layer for a given operation.
 * 
 * @author Maciej Szefler
 */
public enum InvocationStyle {
    /** 
     * The very ordinary blocking IO style --- the IL will block until the operation is complete, or until
     * a timeout is reached. 
     */
    BLOCKING, 
    
    /**
     * Asynchrnous style -- the IL will "queue" the invocation, and call-back asynchrnously when the response
     * is available. 
     */
    ASYNC, 
    
    /**
     * Reliable style -- the IL will queue the invocation using the current transaction. The response will be
     * delivered when available using a separate transaction. 
     */
    RELIABLE,
    
    
    /**
     * Transacted style -- the IL will enroll the operation with the current transaction. The IL will block until the
     * operation completes. 
     */
    TRANSACTED
}