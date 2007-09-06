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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.ode.bpel.iapi.Scheduler.JobInfo;
import org.apache.ode.bpel.iapi.Scheduler.JobProcessor;
import org.apache.ode.bpel.iapi.Scheduler.JobProcessorException;

public class SimpleSchedulerTest extends TestCase implements JobProcessor {

    DelegateSupport _ds;
    SimpleScheduler _scheduler;
    ArrayList<JobInfo> _jobs;
    TransactionManager _txm;


    public void setUp() throws Exception {
        _txm = new GeronimoTransactionManager();
        _ds = new DelegateSupport();

        _scheduler = newScheduler("n1");
        _jobs = new ArrayList<JobInfo>(100);
    }

    public void tearDown() throws Exception {
        _scheduler.shutdown();
    }

    public void testConcurrentExec() throws Exception  {
        _scheduler.start();
        _txm.begin();
        String jobId;
        try {
            jobId = _scheduler.schedulePersistedJob(newDetail("123"), new Date(System.currentTimeMillis() + 100));
            Thread.sleep(200);
            // Make sure we don't schedule until commit.
            assertEquals(0, _jobs.size());
        } finally {
            _txm.commit();
        }
        // Wait for the job to be execed.
        Thread.sleep(100);
        // Should execute job,
        assertEquals(1, _jobs.size());

    }
    
    public void testImmediateScheduling() throws Exception {
        _scheduler.start();
        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("123"), new Date());
            Thread.sleep(100);
            // Make sure we don't schedule until commit.
            assertEquals(0, _jobs.size());
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
        _scheduler.setNearFutureInterval(1000);
        _scheduler.setImmediateInterval(500);
        _scheduler.start();

        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("123"), new Date(System.currentTimeMillis() + 750));
        } finally {
            _txm.commit();
        }

        Thread.sleep(850);
        assertEquals(1, _jobs.size());
    }

    public void testFarFutureScheduling() throws Exception {
        // speed things up a bit to hit the right code paths
        _scheduler.setNearFutureInterval(700);
        _scheduler.setImmediateInterval(300);
        _scheduler.start();

        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("123"), new Date(System.currentTimeMillis() + 750));
        } finally {
            _txm.commit();
        }

        Thread.sleep(850);
        assertEquals(1, _jobs.size());
    }

    public void testRecovery() throws Exception {
        // speed things up a bit to hit the right code paths
        _scheduler.setNearFutureInterval(200);
        _scheduler.setImmediateInterval(100);
        _scheduler.setStaleInterval(50);

        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("immediate"), new Date(System.currentTimeMillis()));
            _scheduler.schedulePersistedJob(newDetail("near"), new Date(System.currentTimeMillis() + 110));
            _scheduler.schedulePersistedJob(newDetail("far"), new Date(System.currentTimeMillis() + 250));
        } finally {
            _txm.commit();
        }

        _scheduler = newScheduler("n3");
        _scheduler.setNearFutureInterval(200);
        _scheduler.setImmediateInterval(100);
        _scheduler.setStaleInterval(50);
        _scheduler.start();
        Thread.sleep(400);
        assertEquals(3, _jobs.size());
    }

    public void testRecoverySuppressed() throws Exception {
        // speed things up a bit to hit the right code paths
        _scheduler.setNearFutureInterval(200);
        _scheduler.setImmediateInterval(100);
        _scheduler.setStaleInterval(50);

        // schedule some jobs ...
        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("immediate"), new Date(System.currentTimeMillis()));
            _scheduler.schedulePersistedJob(newDetail("near"), new Date(System.currentTimeMillis() + 150));
            _scheduler.schedulePersistedJob(newDetail("far"), new Date(System.currentTimeMillis() + 250));
        } finally {
            _txm.commit();
        } 

        // but don't start the scheduler.... 
        
        // create a second node for the scheduler. 
        SimpleScheduler scheduler = newScheduler("n3");
        scheduler.setNearFutureInterval(200);
        scheduler.setImmediateInterval(100);
        scheduler.setStaleInterval(50);
        scheduler.start();
        for (int i = 0; i < 40; ++i) {
            scheduler.updateHeartBeat("n1");
            Thread.sleep(10);
        }

        scheduler.stop();

        assertTrue(_jobs.size() <= 1);
        if (_jobs.size() == 1)
            assertEquals("far", _jobs.get(0).jobDetail.get("foo"));
    }

    public void onScheduledJob(final JobInfo jobInfo) throws JobProcessorException {
        synchronized (_jobs) {
            _jobs.add(jobInfo);
        }
    }

    Map<String, Object> newDetail(String x) {
        HashMap<String, Object> det = new HashMap<String, Object>();
        det.put("foo", x);
        return det;
    }

    private SimpleScheduler newScheduler(String nodeId) {
        SimpleScheduler scheduler = new SimpleScheduler(nodeId, _ds.delegate());
        scheduler.setJobProcessor(this);
        scheduler.setTransactionManager(_txm);
        return scheduler;
    }
    
}
