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

/**
 * The BPEL scheduler.
 */
public interface Scheduler {

    void setJobProcessor(JobProcessor processor) throws ContextException;

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


    void jobCompleted(String jobId);

    /**
     * Make a good effort to cancel the job. If its already running no big
     * deal.
     * @param jobId job identifier of the job
     */
    void cancelJob(String jobId) throws ContextException;

    void start();

    void stop();

    void shutdown();

    /**
     * Interface implemented by the object responsible for job execution.
     * @author mszefler
     */
    public interface JobProcessor {
        void onScheduledJob(JobInfo jobInfo) throws JobProcessorException;
    }

    /**
     * Wrapper containing information about a scheduled job.
     * @author mszefler
     */
    public static class JobInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String jobName;
        public final int retryCount;
        public final Map<String,Object> jobDetail;

        public JobInfo(String jobName, Map<String,Object>jobDetail, int retryCount) {
            this.jobName = jobName;
            this.jobDetail = jobDetail;
            this.retryCount = retryCount;
        }

        public String toString() {
            // Wrap in hashmap in case the underlying object has no toString method.
            return jobName + "["+retryCount +"]: " + new HashMap<Object, Object>(jobDetail);
        }
    }

    /**
     * Exception thrown by the {@link JobProcessor} to indicate failure in job
     * processing.
     * @author mszefler
     */
    public class JobProcessorException extends Exception {
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



}
