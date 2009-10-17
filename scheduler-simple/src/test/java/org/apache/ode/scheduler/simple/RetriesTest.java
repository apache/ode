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

import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;

import javax.transaction.TransactionManager;
import java.util.*;
import java.util.concurrent.Callable;

import junit.framework.TestCase;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public class RetriesTest extends TestCase implements Scheduler.JobProcessor {
    DelegateSupport _ds;
    SimpleScheduler _scheduler;
    ArrayList<Scheduler.JobInfo> _jobs;
    ArrayList<Scheduler.JobInfo> _commit;
    TransactionManager _txm;
    int _tried = 0;
    Scheduler.JobInfo _jobInfo = null;
    
    public void setUp() throws Exception {
        _txm = new GeronimoTransactionManager();
        _ds = new GeronimoDelegateSupport(_txm);

        _scheduler = newScheduler("n1");
        _jobs = new ArrayList<Scheduler.JobInfo>(100);
        _commit = new ArrayList<Scheduler.JobInfo>(100);
    }

    public void tearDown() throws Exception {
        _scheduler.shutdown();
    }
    
    public void testRetries() throws Exception {
        // speed things up a bit to hit the right code paths
        _scheduler.setNearFutureInterval(5000);
        _scheduler.setImmediateInterval(1000);
        _scheduler.start();
        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("123"), new Date());
        } finally {
            _txm.commit();
        }

        Thread.sleep(10000);
        assertEquals(8, _tried);
    }

    public void testExecTransaction() throws Exception {
        final int[] tryCount = new int[1];
        tryCount[0] = 0;
        
        Callable<Void> transaction = new Callable<Void>() {
            public Void call() throws Exception {
                tryCount[0]++;
                if( tryCount[0] < 3 ) {
                    throw new Exception("any");
                } else {
                    return null;
                }
            }            
        };

        _scheduler.execTransaction(transaction);
        assertEquals(3, tryCount[0]);
    }

    public void onScheduledJob(Scheduler.JobInfo jobInfo) throws Scheduler.JobProcessorException {
        _jobInfo = jobInfo;
        
        _tried++;
        throw new Scheduler.JobProcessorException(jobInfo.retryCount < 1);
    }

    Map<String, Object> newDetail(String x) {
        HashMap<String, Object> det = new HashMap<String, Object>();
        det.put("foo", x);
        return det;
    }

    private SimpleScheduler newScheduler(String nodeId) {
        SimpleScheduler scheduler = new SimpleScheduler(nodeId, _ds.delegate(), new Properties());
        scheduler.setJobProcessor(this);
        scheduler.setTransactionManager(_txm);
        return scheduler;
    }
}
