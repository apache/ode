package org.apache.ode.bpel.evar.jdbc;

/**
 * Enumeration of methods in which a new external variable row initialization is handled. 
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
public enum InitType {
    /** Just try to update the row, if does not already  exist, fails. */
    update,
    
    /** Just insert the row, if already exist fails. */
    insert,
    
    /** Try updating the row, if no exist, then try inserting. */
    update_insert,
    
    /** First delete the row, then insert a new one. */
    delete_insert

}
