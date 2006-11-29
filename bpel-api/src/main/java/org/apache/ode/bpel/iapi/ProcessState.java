package org.apache.ode.bpel.iapi;

/**
 * The user-selectable states of a process in the configuration store.
 * @author mszefler
 */
public enum ProcessState {
    /** Process can create new instances and execute old instances. */
    ACTIVE,
    
    /** Process can execute old instances, but cannot create new instances. */
    RETIRED,
    
    /** Process cannot exeucte old nor create new instances. */
    DISABLED
}
