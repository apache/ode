package org.apache.ode.bpel.dao;

import java.io.Serializable;

/**
 * Instances and associated data for a ProcessDAO implementation that implements this 
 * interface can be deleted in a deferred fashion.
 * 
 * @author sean
 *
 */
public interface DeferredProcessInstanceCleanable {
    /**
     * Returns the database id.
     * 
     * @return database id
     */
    Serializable getId();
    
    /**
     * Deletes instances and data for this process, the number of rows gets deletes is limited
     * by the transaction size.
     * 
     * @param transactionSize the number of rows to delete
     * @return the number of rows actually deleted
     */
    int deleteInstances(int transactionSize);
}
