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

package org.apache.ode.il;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.utils.GUID;

/**
 * 
 * @author Matthieu Riou <mriou at apache dot org>
 * 
 * - BART refactor: Removed transaction management logic. 
 * @author Maciej Szefler <mszefler at gmail dot com> 
 */
public class MockScheduler implements Scheduler {

    private static final Log __log = LogFactory.getLog(MockScheduler.class);

    private JobProcessor _processor;
    private Timer _timer = new Timer(false);

    private ScheduledExecutorService _exec;

    private TransactionManager _txm;

    public MockScheduler(TransactionManager txm) {
        _txm = txm;
        _exec = Executors.newSingleThreadScheduledExecutor();
    }

    ThreadLocal<List<Synchronization>> _synchros = new ThreadLocal<List<Synchronization>>() {
        @Override
        protected List<Synchronization> initialValue() {
            return new ArrayList<Synchronization>();
        }
    };

    public String schedulePersistedJob(final Map<String, Object> detail, Date dt) throws ContextException {
        final Date date = dt == null ? new Date() : dt;
        registerSynchronizer(new Synchronization() {
            public void afterCompletion(int status) {
                long delay = Math.max(0, date.getTime() - System.currentTimeMillis());
                _exec.schedule(new Callable<Void>() {
                    public Void call() throws Exception {
                        JobInfo ji = new JobInfo("job" + System.currentTimeMillis(), detail, 0);
                        doExecute(ji);
                        return null;
                    }
                }, delay, TimeUnit.MILLISECONDS);
            }

            public void beforeCompletion() {
            }
        });
        return new GUID().toString();
    }

    public void cancelJob(String arg0) throws ContextException {
        
    }

    public void start() {
    }

    public void stop() {
    }

    public void shutdown() {
    }

    private void registerSynchronizer(final Synchronization synch) throws ContextException {
        try {
            _txm.getTransaction().registerSynchronization(synch);
        } catch (Exception e) {
            __log.error("Exception in mock scheduler sync registration.", e);
            throw new RuntimeException(e);
        }
    }


    private void doExecute(JobInfo ji) {
        JobProcessor processor = _processor;
        if (processor == null)
            throw new RuntimeException("No processor.");
        try {
            processor.onScheduledJob(ji);
        } catch (Exception jpe) {
            throw new RuntimeException("Scheduled transaction failed unexpectedly: transaction will not be retried!.", jpe);
        }
    }

    public void setJobProcessor(JobProcessor processor) throws ContextException {
        _processor = processor;
    }

    public void jobCompleted(String jobId) {

    }
}
