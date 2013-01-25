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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.ode.scheduler.simple.SchedulerThread;
import org.apache.ode.scheduler.simple.Task;
import org.apache.ode.scheduler.simple.TaskRunner;


import junit.framework.TestCase;

/**
 * Test of SchedulerThread. 
 * 
 * @author Maciej Szefler  ( m s z e f l e r @ g m a i l . c o m ) 
 */
public class SchedulerThreadTest extends TestCase implements TaskRunner {

    static final long SCHED_TOLERANCE = 100;
    SchedulerThread _st;
    
    List<TR> _tasks = new ArrayList<TR>(100); 
    
    public void setUp() throws Exception {
        _st = new SchedulerThread(this);
    }
    
    public void testSchedulingResolution() throws Exception {
        _st.start();
        long schedtime = System.currentTimeMillis() + 300;
        _st.enqueue(new Task(schedtime));
        Thread.sleep(1000);
        assertEquals(1,_tasks.size());
        assertTrue(_tasks.get(0).time < schedtime + SCHED_TOLERANCE / 2);
        assertTrue(_tasks.get(0).time > schedtime - SCHED_TOLERANCE / 2);
    }

    public void testStartStop() throws Exception {
        _st.start();
        long schedtime = System.currentTimeMillis() + 500;
        _st.enqueue(new Task(schedtime));
        _st.stop();
        Thread.sleep(600);
        assertEquals(0,_tasks.size());
        _st.start();
        Thread.sleep(SCHED_TOLERANCE);
        assertEquals(1,_tasks.size());
    }
    
    public void testParallelEnqueue() throws Exception {
        _st.start();
        final long startTime = System.currentTimeMillis() + 100;
        final AtomicInteger ai = new AtomicInteger(300);
        // enque in reverse order
        Runnable run = new Runnable() {
            public void run() {
                Task tsk = new Task(startTime + ai.getAndDecrement() * 5);
                _st.enqueue(tsk);
            }
        };
        
        ExecutorService es = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 300; ++i) 
            es.execute(run);
        
        Thread.sleep(300 + 300 * 5);
        assertEquals(300,_tasks.size());
        // Make sure they got scheduled in the right order

        for (int i = 0; i < 299; ++i) 
            assertTrue(_tasks.get(i).task.schedDate < _tasks.get(i+1).task.schedDate);

        // Check scheduling tolerance
        for (TR tr : _tasks) {
            assertTrue(tr.time < tr.task.schedDate + SCHED_TOLERANCE / 2);
            assertTrue(tr.time > tr.task.schedDate - SCHED_TOLERANCE / 2);
        }
    }

    public void testTaskDequeueOrderWithSameExecTime() throws Exception {
        final long startTime = System.currentTimeMillis();

        // enqueue jobs with same execution time and incremental jobId
        for (int i = 0; i < 300; i++) {
            Job tsk = new Job(startTime, String.valueOf(i), true, null);
            _st.enqueue(tsk);
        }

        _st.start();
        Thread.sleep(300 + 300 * 5);
        assertEquals(300, _tasks.size());

        // jobs need to be dequeued in the same order of insertion.
        for (int i = 0; i < 299; ++i) {
            Job currJob = (Job) _tasks.get(i).task;
            Job nextJob = (Job) _tasks.get(i + 1).task;
            assertTrue(Integer.parseInt(currJob.jobId) < Integer.parseInt(nextJob.jobId));
        }
        _st.stop();
    }


    public void runTask(Task task) {
        synchronized(_tasks) {
            _tasks.add(new TR(System.currentTimeMillis(),task));
        }
    }
    
    
    class TR {
        long time;
        Task task;
        TR(long time, Task task) {
            this.time = time;
            this.task = task;
        }
    }
}
