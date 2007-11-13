package org.apache.ode.bpel.evar.jdbc;

/**
 * Generator type enumaration.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
enum GenType {
    /** plain old column */
    none,
    
    /** sequence column */
    sequence,
    
    /** SQL expression column */
    expression,
    
    /** server-generated uuid column */
    uuid, 
    
    /** process-id column */
    pid, 
    
    /** instance-id column */
    iid, 
    
    /** create timestamp */
    ctimestamp, 
    
    /** update timestamp */
    utimestamp
}