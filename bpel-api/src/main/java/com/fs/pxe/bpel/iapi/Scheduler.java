package com.fs.pxe.bpel.iapi;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * The BPEL scheduler.
 */
public interface Scheduler {

  /**
   * Schedule a persisted job. Persisted jobs MUST survive system failure.
   * They also must not be scheduled unless the transaction associated with 
   * the calling thread commits. 
   * @param jobDetail information about the job
   * @param when when the job should run (<code>null</code> means now)
   * @return unique job identifier
   */
  String schedulePersistedJob(Map<String,Object>jobDetail,Date when) 
    throws ContextException ;
  
  
  /**
   * Schedule a volatile (non-persisted) job. Volatile jobs should not be 
   * saved in the database and should not survive system crash. Volatile 
   * jobs scheduled from a transactional context should be scheduled 
   * regardless of whether the tansaction commits. 
   * 
   * @param jobDetail information about the job
   * @param when when the job should run (<code>null</code> means now)
   * @return unique (as far as the scheduler is concerned) job identifier
   */
  String scheduleVolatileJob(boolean transacted, Map<String,Object> jobDetail, 
      Date when) throws ContextException;
  
  /**
   * Make a good effort to cancel the job. If its already running no big
   * deal. 
   * @param jobId job identifier of the job 
   */
  void cancelJob(String jobId) throws ContextException;
  
  /**
   * Execute a {@link Callable} in a transactional context. If the callable 
   * throws an exception, then the transaction will be rolled back, otherwise
   * the transaction will commit. 
   * 
   * @param <T> return type
   * @param transaction transaction to execute
   * @return result
   * @throws Exception
   */
  <T> T execTransaction(Callable<T> transaction) 
    throws Exception, ContextException;
  
  void start();
  
  void stop();
}
