package org.apache.ode.dao.bpel;

import java.io.Serializable;

/**
 * Instances and associated data for a ProcessDAO implementation that implements this 
 * interface can be deleted in a deferred fashion.
 * The framework guarantees that the deleteInstances() call is repeatedly called
 * until the call returns a number smaller than the given transaction size, even
 * when the system restarts at some time.
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
