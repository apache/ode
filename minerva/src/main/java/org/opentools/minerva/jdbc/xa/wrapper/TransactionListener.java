/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.jdbc.xa.wrapper;


/**
 * Callback for notification when a transaction is finished.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public interface TransactionListener {
    /**
     * Indicates that the transaction this instance was part of has finished.
     */
    public void transactionFinished(XAConnectionImpl con);

    /**
     * Indicates that the transaction this instance was part of has finished,
     * and there was a fatal error.  Any pooled resources should be recycled.
     */
    public void transactionFailed(XAConnectionImpl con);
}
