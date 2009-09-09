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
package org.apache.ode.bpel.engine.replayer;

import java.util.Date;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Scheduler;

/**
 * Manages events invocation in sorted order during replaying. 
 *  
 * @author Rafal Rusin
 *
 */
public class ReplayerScheduler implements Scheduler {
    private static final Log __log = LogFactory.getLog(ReplayerScheduler.class);

    private PriorityQueue<TaskElement> taskQueue = new PriorityQueue<TaskElement>();

    private static class TaskElement implements Comparable<TaskElement> {
        public final Date when;
        public final Callable<Object> action;
        public final ReplayerBpelRuntimeContextImpl runtimeContext;

        public TaskElement(Date when, Callable<Object> action, ReplayerBpelRuntimeContextImpl runtimeContext) {
            super();
            this.when = when;
            this.action = action;
            this.runtimeContext = runtimeContext;
        }

        public int compareTo(TaskElement o) {
            return when.compareTo(o.when);
        }
    }

    public void scheduleReplayerJob(Callable action, Date when, ReplayerBpelRuntimeContextImpl runtimeContext) {
        taskQueue.add(new TaskElement(when, action, runtimeContext));
    }

    public void cancelJob(String jobId) throws ContextException {
        throw new IllegalStateException();
    }

    public <T> T execTransaction(Callable<T> transaction) throws Exception, ContextException {
        throw new IllegalStateException();
    }

    public boolean isTransacted() {
        return true;
    }

    public void registerSynchronizer(Synchronizer synch) throws ContextException {
        throw new IllegalStateException();
    }

    public void setJobProcessor(JobProcessor processor) throws ContextException {
        throw new IllegalStateException();
    }

    public void setRollbackOnly() throws Exception {
        throw new IllegalStateException();
    }

    public void shutdown() {
    }

    public void startReplaying() throws Exception {
        while (!taskQueue.isEmpty()) {
            TaskElement taskElement = taskQueue.remove();
            __log.debug("executing action at time " + taskElement.when);
            if (taskElement.runtimeContext != null) {
                taskElement.runtimeContext.setCurrentEventDateTime(taskElement.when);
            }
            taskElement.action.call();
        }
    }

    public void stop() {
    }

    public void start() {
    }

    public <T> Future<T> execIsolatedTransaction(Callable<T> transaction) throws Exception, ContextException {
        return null;
    }

    public String scheduleMapSerializableRunnable(MapSerializableRunnable runnable, Date when) throws ContextException {
        return null;
    }

    public String schedulePersistedJob(Map<String, Object> jobDetail, Date when) throws ContextException {
        return null;
    }

    public String scheduleVolatileJob(boolean transacted, Map<String, Object> jobDetail) throws ContextException {
        return null;
    }

    public void setPolledRunnableProcesser(JobProcessor polledRunnableProcessor) {
    }
}
