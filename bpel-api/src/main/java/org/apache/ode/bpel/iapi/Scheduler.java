/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.bpel.iapi;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.CorrelationKeySet;

/**
 * The BPEL scheduler.
 */
public interface Scheduler {
    void setJobProcessor(JobProcessor processor) throws ContextException;
    
    void setPolledRunnableProcesser(JobProcessor polledRunnableProcessor);
    
    /**
     * Schedule a persisted job. Persisted jobs MUST survive system failure.
     * They also must not be scheduled unless the transaction associated with
     * the calling thread commits.
     * @param jobDetail information about the job
     * @param when when the job should run (<code>null</code> means now)
     * @return unique job identifier
     */
    String schedulePersistedJob(JobDetails jobDetail,Date when)
            throws ContextException ;


    /**
     * Schedule a Runnable that will be executed on a dedicated thread pool.
     * @param runnable
     * @param when
     * @return
     * @throws ContextException
     */
    String scheduleMapSerializableRunnable(MapSerializableRunnable runnable, Date when) throws ContextException;

    /**
     * Schedule a volatile (non-persisted) job. Volatile jobs should not be
     * saved in the database and should not survive system crash. Volatile
     * jobs scheduled from a transactional context should be scheduled
     * regardless of whether the transaction commits.
     *
     * @param transacted should the job be executed in a transaction?
     * @param jobDetail information about the job
     * @param when does the job should be executed?
     * @return unique (as far as the scheduler is concerned) job identifier
     */
    String scheduleVolatileJob(boolean transacted, JobDetails jobDetail, Date when) throws ContextException;

    /**
     * Schedule a volatile job for right now
     * @see #scheduleVolatileJob(boolean, java.util.Map, java.util.Date)
     */
    String scheduleVolatileJob(boolean transacted, JobDetails jobDetail) throws ContextException;

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

    /**
     * Execute a {@link Callable} in a transactional context. If the callable
     * throws an exception, then the transaction will be rolled back, otherwise
     * the transaction will commit. Also, modify the value of the timeout value 
     * that is associated with the transactions started by the current thread. 
     *
     * @param <T> return type
     * @param transaction transaction to execute
     * @param timeout, The value of the timeout in seconds. If the value is zero, the transaction service uses the default value.
     * @return result
     * @throws Exception
     */
    <T> T execTransaction(Callable<T> transaction, int timeout)
            throws Exception, ContextException;
    
    void setRollbackOnly() throws Exception;

    /**
     * Same as execTransaction but executes in a different thread to guarantee
     * isolation from the main execution thread.
     * @param transaction
     * @return
     * @throws Exception
     * @throws ContextException
     */
    <T> Future<T> execIsolatedTransaction(final Callable<T> transaction)
            throws Exception, ContextException;

    /**
     * @return true if the current thread is associated with a transaction.
     */
    boolean isTransacted();

    /**
     * Register a transaction synchronizer.
     * @param synch synchronizer
     * @throws ContextException
     */
    void registerSynchronizer(Synchronizer synch) throws ContextException;

    void start();

    void stop();

    void shutdown();
    
    void acquireTransactionLocks();

    public interface Synchronizer {
        /**
         * Called after the transaction is completed.
         * @param success indicates whether the transaction was comitted
         */
        void afterCompletion(boolean success);

        /**
         * Called before the transaction is completed.
         */
        void beforeCompletion();
    }

    /**
     * Interface implemented by the object responsible for job execution.
     * @author mszefler
     */
    public interface JobProcessor {
         /**
          * Implements execution of the job.
          * @param jobInfo the job information
          * @throws JobProcessorException
          */
        void onScheduledJob(JobInfo jobInfo) throws JobProcessorException;
    }

    public enum JobType {
        /** 
         * is used for scheduled timer tasks like in pick's/evenhandler's onAlarm. 
         */
        TIMER, 
        /** 
         * is used for resuming process instances if the time slice has been exeeded 
         * or when the debugger lets the process instance resume.
         */
        RESUME, 
        
        /**
         * is used to let the runtime process an incoming message after it has been
         * received and stored by the IL. It will try to correlate the message if
         * a route to an IMA can be found.
         */
        INVOKE_INTERNAL, 
        
        /**
         * is used when the response from a two-way invocation comes back and shall be 
         * passed to the runtime.
         */
        INVOKE_RESPONSE, 
        
        /**
         * is used to schedule the matchmaking after adding a route to the correlator, i.e.
         * if a IMA is now waiting for a message. If the message is already in the queue,
         * this matcher job will find it.
         */
        MATCHER, 
        
        /**
         * is used to check for failed partner invocations. It runs after a defined time
         * out, checks whether a response has arrived and if not, it marks the MEX as
         * faulted.
         */
        INVOKE_CHECK
    }
    
    public static class JobDetails {
        public Long instanceId;
        public String mexId;
        public String processId;
        public String type;
        public String channel;
        public String correlatorId;
        public String correlationKeySet;
        public Integer retryCount;
        public Boolean inMem;
        public Map<String, Object> detailsExt = new HashMap<String, Object>();
        
        public Boolean getInMem() {
            return inMem == null ? false : inMem;
        }
        public void setInMem(Boolean inMem) {
            this.inMem = inMem;
        }
        public String getMexId() {
            return mexId;
        }
        public void setMexId(String mexId) {
            this.mexId = mexId;
        }
        public QName getProcessId() {
            return processId == null ? null : QName.valueOf(processId);
        }
        public void setProcessId(QName processId) {
            this.processId = "" + processId;
        }
        public JobType getType() {
            return JobType.valueOf(type);
        }
        public void setType(JobType type) {
            this.type = type.toString();
        }
        public String getChannel() {
            return channel;
        }
        public void setChannel(String channel) {
            this.channel = channel;
        }
        public String getCorrelatorId() {
            return correlatorId;
        }
        public void setCorrelatorId(String correlatorId) {
            this.correlatorId = correlatorId;
        }
        public CorrelationKeySet getCorrelationKeySet() {
            return new CorrelationKeySet(correlationKeySet);
        }
        public void setCorrelationKeySet(CorrelationKeySet correlationKeySet) {
            this.correlationKeySet = correlationKeySet == null ? null : correlationKeySet.toCanonicalString();
        }
        public Integer getRetryCount() {
            return retryCount == null ? 0 : retryCount;
        }
        public void setRetryCount(Integer retryCount) {
            this.retryCount = retryCount;
        }
        public Long getInstanceId() {
            return instanceId;
        }
        public void setInstanceId(Long instanceId) {
            this.instanceId = instanceId;
        }
        public Map<String, Object> getDetailsExt() {
            return detailsExt;
        }
        public void setDetailsExt(Map<String, Object> detailsExt) {
            this.detailsExt = detailsExt;
        }
        
        @Override
        public String toString() {
            return "JobDetails("
            + " instanceId: " + instanceId
            + " mexId: " + mexId
            + " processId: " + processId
            + " type: " + type
            + " channel: " + channel
            + " correlatorId: " + correlatorId
            + " correlationKeySet: " + correlationKeySet
            + " retryCount: " + retryCount
            + " inMem: " + inMem
            + " detailsExt: " + detailsExt
            + ")";
        }
    }
    
    /**
     * Wrapper containing information about a scheduled job.
     * @author mszefler
     */
    public static class JobInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String jobName;
        public final int retryCount;
        public final JobDetails jobDetail;

        public JobInfo(String jobName, JobDetails jobDetail, int retryCount) {
            this.jobName = jobName;
            this.jobDetail = jobDetail;
            this.retryCount = retryCount;
        }

        public String toString() {
            return jobName + "["+retryCount +"]: " + jobDetail;
        }
    }

    /**
     * Exception thrown by the {@link JobProcessor} to indicate failure in job
     * processing.
     * @author mszefler
     */
    public class JobProcessorException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public final boolean retry;

        public JobProcessorException(boolean retry) {
            this.retry = retry;
        }

        public JobProcessorException(Throwable cause, boolean retry) {
            super(cause);
            this.retry = retry;
        }
    }

    public interface MapSerializableRunnable extends Runnable, Serializable {
        void storeToDetails(JobDetails details);
        void restoreFromDetails(JobDetails details);
    }
}