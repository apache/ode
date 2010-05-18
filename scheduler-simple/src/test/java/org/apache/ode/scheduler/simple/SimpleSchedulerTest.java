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

package org.apache.ode.scheduler.simple;

import java.util.*;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.Scheduler.JobInfo;
import org.apache.ode.bpel.iapi.Scheduler.JobProcessor;
import org.apache.ode.bpel.iapi.Scheduler.JobProcessorException;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;

public class SimpleSchedulerTest extends TestCase implements JobProcessor {

    DelegateSupport _ds;
    SimpleScheduler _scheduler;
    ArrayList<JobInfo> _jobs;
    ArrayList<JobInfo> _commit;
    TransactionManager _txm;

    public void setUp() throws Exception {
        _txm = new GeronimoTransactionManager();
        _ds = new DelegateSupport();

        _scheduler = newScheduler("n1");
        _jobs = new ArrayList<JobInfo>(100);
        _commit = new ArrayList<JobInfo>(100);
    }

    public void tearDown() throws Exception {
        _scheduler.shutdown();
    }

    public void testConcurrentExec() throws Exception  {
        _scheduler.start();
        for (int i=0; i<10; i++) {
            _txm.begin();
            String jobId;
            try {
                int jobs = _jobs.size();
                jobId = _scheduler.schedulePersistedJob(newDetail("123"), new Date(System.currentTimeMillis() + 200));
                Thread.sleep(100);
                // we're using transacted jobs which means it will commit at the end
                // if the job is scheduled, the following assert is not valid @seanahn
                // assertEquals(jobs, _jobs.size());            
            } finally {
                _txm.commit();
            }
            // Delete from DB
            assertEquals(true,_ds.delegate().deleteJob(jobId, "n1"));
            // Wait for the job to be execed.
            Thread.sleep(250);
            // We should always have same number of jobs/commits
            assertEquals(_jobs.size(), _commit.size());
        }
    }
    
    public void testImmediateScheduling() throws Exception {
        _scheduler.start();
        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("123"), new Date());
            Thread.sleep(100);
            // we're using transacted jobs which means it will commit at the end
            // if the job is scheduled, the following assert is not valid @seanahn
            // assertEquals(jobs, _jobs.size());        
        } finally {
            _txm.commit();
        }
        Thread.sleep(100);
        assertEquals(1, _jobs.size());
    }

    public void testStartStop() throws Exception {
        _scheduler.start();
        _txm.begin();
        try {
            for (int i = 0; i < 10; ++i)
                _scheduler.schedulePersistedJob(newDetail("123"), new Date(System.currentTimeMillis() + (i * 100)));
        } finally {
            _txm.commit();
        }
        Thread.sleep(100);
        _scheduler.stop();
        int jobs = _jobs.size();
        assertTrue(jobs > 0);
        assertTrue(jobs < 10);
        Thread.sleep(200);
        assertEquals(jobs, _jobs.size());
        _scheduler.start();
        Thread.sleep(1000);
        assertEquals(10, _jobs.size());
    }

    public void testNearFutureScheduling() throws Exception {
        // speed things up a bit to hit the right code paths
        _scheduler.setNearFutureInterval(10000);
        _scheduler.setImmediateInterval(5000);
        _scheduler.start();

        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("123"), new Date(System.currentTimeMillis() + 7500));
        } finally {
            _txm.commit();
        }

        Thread.sleep(8500);
        assertEquals(1, _jobs.size());
    }

    public void testFarFutureScheduling() throws Exception {
        // speed things up a bit to hit the right code paths
        _scheduler.setNearFutureInterval(7000);
        _scheduler.setImmediateInterval(3000);
        _scheduler.start();

        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("123"), new Date(System.currentTimeMillis() + 7500));
        } finally {
            _txm.commit();
        }

        Thread.sleep(8500);
        assertEquals(1, _jobs.size());
    }

    public void testRecovery() throws Exception {
        // speed things up a bit to hit the right code paths
        _scheduler.setNearFutureInterval(2000);
        _scheduler.setImmediateInterval(1000);
        _scheduler.setStaleInterval(500);

        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("immediate"), new Date(System.currentTimeMillis()));
            _scheduler.schedulePersistedJob(newDetail("near"), new Date(System.currentTimeMillis() + 1100));
            _scheduler.schedulePersistedJob(newDetail("far"), new Date(System.currentTimeMillis() + 2500));
        } finally {
            _txm.commit();
        }

        _scheduler = newScheduler("n3");
        _scheduler.setNearFutureInterval(2000);
        _scheduler.setImmediateInterval(1000);
        _scheduler.setStaleInterval(1000);
        _scheduler.start();
        Thread.sleep(4000);
        assertEquals(3, _jobs.size());
    }

    public void testRecoverySuppressed() throws Exception {
        // speed things up a bit to hit the right code paths
        _scheduler.setNearFutureInterval(2000);
        _scheduler.setImmediateInterval(1000);
        _scheduler.setStaleInterval(500);

        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("immediate"), new Date(System.currentTimeMillis()));
            _scheduler.schedulePersistedJob(newDetail("near"), new Date(System.currentTimeMillis() + 1100));
            _scheduler.schedulePersistedJob(newDetail("far"), new Date(System.currentTimeMillis() + 15000));
        } finally {
            _txm.commit();
        }
        _scheduler.stop();

        _scheduler = newScheduler("n3");
        _scheduler.setNearFutureInterval(2000);
        _scheduler.setImmediateInterval(1000);
        _scheduler.setStaleInterval(1000);
        _scheduler.start();
        for (int i = 0; i < 40; ++i) {
            _scheduler.updateHeartBeat("n1");
            Thread.sleep(100);
        }

        _scheduler.stop();
        Thread.sleep(1000);

        assertEquals(0, _jobs.size());
    }

    public void onScheduledJob(final JobInfo jobInfo) throws JobProcessorException {
        synchronized (_jobs) {
            _jobs.add(jobInfo);
        }
        
        try {
            _txm.getTransaction().registerSynchronization(new Synchronization() {

                public void afterCompletion(int arg0) {
                    if (arg0 == Status.STATUS_COMMITTED) 
                        _commit.add(jobInfo);
                }

                public void beforeCompletion() {
                    // TODO Auto-generated method stub
                    
                }
                
            });
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RollbackException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    Scheduler.JobDetails newDetail(String x) {
        Scheduler.JobDetails jd = new Scheduler.JobDetails();
        jd.getDetailsExt().put("foo", x);
        return jd;
    }

    private SimpleScheduler newScheduler(String nodeId) {
        SimpleScheduler scheduler = new SimpleScheduler(nodeId, _ds.delegate(), new Properties());
        scheduler.setJobProcessor(this);
        scheduler.setTransactionManager(_txm);
        return scheduler;
    }
    
}
