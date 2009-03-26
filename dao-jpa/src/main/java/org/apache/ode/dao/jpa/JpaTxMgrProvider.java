package org.apache.ode.dao.jpa;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.util.GeneralException;

public class JpaTxMgrProvider implements ManagedRuntime {
	private TransactionManager _txMgr;
	
    public JpaTxMgrProvider(TransactionManager txMgr) {
    	_txMgr = txMgr;
    }
    
    public TransactionManager getTransactionManager() throws Exception {
        return _txMgr;
    }
    
    public void setRollbackOnly(Throwable cause) throws Exception {
        // there is no generic support for setting the rollback cause
        getTransactionManager().getTransaction().setRollbackOnly();
    }
    
    public Throwable getRollbackCause() throws Exception {
        // there is no generic support for setting the rollback cause
        return null;
    }
    
    public Object getTransactionKey() throws Exception, SystemException {
        return _txMgr.getTransaction();
    }
    
    public void doNonTransactionalWork(java.lang.Runnable runnable) throws NotSupportedException {
        TransactionManager tm = null;
        Transaction transaction = null;
        
        try { 
            tm = getTransactionManager(); 
            transaction = tm.suspend();
        } catch (Exception e) {
            NotSupportedException nse =
                new NotSupportedException(e.getMessage());
            nse.initCause(e);
            throw nse;
        }
        
        runnable.run();
        
        try {
            tm.resume(transaction);
        } catch (Exception e) {
            try {
                transaction.setRollbackOnly();
            }
            catch(SystemException se2) {
                throw new GeneralException(se2);
            }
            NotSupportedException nse =
                new NotSupportedException(e.getMessage());
            nse.initCause(e);
            throw nse;
        } 
    }
}