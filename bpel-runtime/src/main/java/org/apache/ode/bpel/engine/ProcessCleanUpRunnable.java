package org.apache.ode.bpel.engine;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.DeferredProcessInstanceCleanable;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.engine.BpelServerImpl.ContextsAware;
import org.apache.ode.bpel.iapi.Scheduler.MapSerializableRunnable;

public class ProcessCleanUpRunnable implements MapSerializableRunnable, ContextsAware {
    private static final long serialVersionUID = 1L;

    private static final Log __log = LogFactory.getLog(ProcessCleanUpRunnable.class);

    public final static int PROCESS_CLEANUP_TRANSACTION_SIZE = Integer.getInteger("org.apache.ode.processInstanceDeletion.transactionSize", 10);
    
    private transient Contexts _contexts;
    private transient Serializable _pid;

    public ProcessCleanUpRunnable() {
    }
    
    public ProcessCleanUpRunnable(Serializable pid) {
        _pid = pid;
    }

    public void storeToDetailsMap(Map<String, Object> details) {
        details.put("pid", _pid);
    }
    
    public void restoreFromDetailsMap(Map<String, Object> details) {
        _pid = (Serializable)details.get("pid");
    }

    public void setContexts(Contexts contexts) {
        _contexts = contexts;
    }
    
    public void run() {
        if(__log.isDebugEnabled()) __log.debug("Deleting runtime data for old process: " + _pid + "...");
        try {
            // deleting of a process may involve hours' of database transaction, 
            // we need to break it down to smaller transactions
            int transactionResultSize = 0;
            do {
                transactionResultSize = _contexts.scheduler.execTransaction(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        ProcessDAO process = _contexts.dao.getConnection().createTransientProcess(_pid);
                        if( !(process instanceof DeferredProcessInstanceCleanable) ) {
                            throw new IllegalArgumentException("ProcessDAO does not implement DeferredProcessInstanceCleanable!!!");
                        }
                        return ((DeferredProcessInstanceCleanable)process).deleteInstances(PROCESS_CLEANUP_TRANSACTION_SIZE);
                    }
                });
                if(__log.isDebugEnabled()) __log.debug("Deleted " + transactionResultSize + "instances for old process: " + _pid + ".");
            } while( transactionResultSize == PROCESS_CLEANUP_TRANSACTION_SIZE );
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if(__log.isInfoEnabled()) __log.info("Deleted runtime data for old process: " + _pid + ".");
    }
}