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

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.CorrelationKey;

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

    public enum JobType {
        TIMER, 
        
        RESUME, 
        
        /** Response from partner (i.e. the result of a partner-role invoke) has been received. */
        PARTNER_RESPONSE, 
        
        MATCHER, 
        
        /** Invoke a "my role" operation (i.e. implemented by the process). */
        MYROLE_INVOKE, 
        
        MYROLE_INVOKE_ASYNC_RESPONSE,

        INVOKE_CHECK
    }
    
    public interface JobDetails {
        public Long getInstanceId();
        public void setInstanceId(Long iid);
        public String getMexId();
        public void setMexId(String mexId);
        public QName getProcessId();
        public void setProcessId(QName processId);
        public JobType getType();
        public void setType(JobType type);
        public String getChannel();
        public void setChannel(String channel);
        public String getCorrelatorId();
        public void setCorrelatorId(String correlatorId);
        public CorrelationKey getCorrelationKey();
        public void setCorrelationKey(CorrelationKey correlationKey);
        public Integer getRetryCount();
        public void setRetryCount(Integer retryCount);
        public Boolean getInMem();
        public void setInMem(Boolean inMem);
        public Map<String, Object> getDetailsExt();
        public void setDetailsExt(Map<String, Object> detailsExt);
    }

    public static class JobDetailsImpl implements Scheduler.JobDetails {
        public Long instanceId;
        public String mexId;
        public String processId;
        public String type;
        public String channel;
        public String correlatorId;
        public String correlationKey;
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
        public CorrelationKey getCorrelationKey() {
            return new CorrelationKey(correlationKey);
        }
        public void setCorrelationKey(CorrelationKey correlationKey) {
            this.correlationKey = correlationKey == null ? null : correlationKey.toCanonicalString();
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
            return "JobDetailsImpl("
            + " instanceId: " + instanceId
            + " mexId: " + mexId
            + " processId: " + processId
            + " type: " + type
            + " channel: " + channel
            + " correlatorId: " + correlatorId
            + " correlationKey: " + correlationKey
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

    public interface MapSerializableRunnable extends Runnable, Serializable {
        void storeToDetails(JobDetails details);
        void restoreFromDetails(JobDetails details);
    }
}
