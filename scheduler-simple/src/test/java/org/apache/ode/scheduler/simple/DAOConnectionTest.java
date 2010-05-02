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

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.Scheduler.JobType;
import org.apache.ode.dao.scheduler.JobDAO;

import org.apache.ode.dao.scheduler.SchedulerDAOConnection;

/**
 * 
 * Test of the JDBC delegate. 
 * 
 * @author Maciej Szefler ( m s z e f l e r  @ g m a i l . c o m )
 */
public class DAOConnectionTest extends SchedulerTestBase {

    
    public void testGetNodeIds() throws Exception {
        SchedulerDAOConnection conn = _factory.getConnection();
        try{          
          // should have no node ids in the db, empty list (not null)
          _txm.begin();;
          List<String> nids = conn.getNodeIds();
          _txm.commit();
          assertNotNull(nids);
          assertEquals(0, nids.size());

          // try for one nodeid
          _txm.begin();;
          conn.insertJob(conn.createJob(true, new Scheduler.JobDetails(), true, 0L),"abc", true);
          _txm.commit();
          _txm.begin();;
          nids = conn.getNodeIds();
          _txm.commit();
          assertEquals(1, nids.size());
          assertTrue(nids.contains("abc"));

          // check that dups are ignored.
          _txm.begin();;
          conn.insertJob(conn.createJob(true, new Scheduler.JobDetails(), true, 0L),"abc", true);
          _txm.commit();
          _txm.begin();;
          nids = conn.getNodeIds();
          _txm.commit();
          assertEquals(1, nids.size());
          assertTrue(nids.contains("abc"));

          // add another nodeid,
          _txm.begin();;
          conn.insertJob(conn.createJob(true, new Scheduler.JobDetails(), true, 0L),"123", true);
          _txm.commit();

          _txm.begin();;
          nids = conn.getNodeIds();
          _txm.commit();
          assertEquals(2, nids.size());
          assertTrue(nids.contains("abc"));
          assertTrue(nids.contains("123"));
        }finally{
          conn.close();
        }
    }

    public void testReassign() throws Exception {
       SchedulerDAOConnection conn = _factory.getConnection();
        try{
          _txm.begin();;
          conn.insertJob(conn.createJob(true, new Scheduler.JobDetails(), true, 100L),"n1", false);
          conn.insertJob(conn.createJob(true, new Scheduler.JobDetails(), true, 200L),"n2", false);
          _txm.commit();

          _txm.begin();;
          int num = conn.updateReassign("n1","n2");
          _txm.commit();

          assertEquals(1,num);

          _txm.begin();;
          List<JobDAO> jobs = conn.dequeueImmediate("n2", 400L, 1000);
          _txm.commit();
           
          assertEquals(2,jobs.size());
        }finally{
          conn.close();
        }
    }

    public void testScheduleImmediateTimeFilter() throws Exception {
       SchedulerDAOConnection conn = _factory.getConnection();
        try{
          _txm.begin();;
          JobDAO job = conn.createJob(true, new Scheduler.JobDetails(), true, 100L);
          String jobId1 = job.getJobId();
          conn.insertJob(job,"n1", false);
          job = conn.createJob(true, new Scheduler.JobDetails(), true, 200L);
          String jobId2 = job.getJobId();
          conn.insertJob(job,"n1", false);
          _txm.commit();

          _txm.begin();;
          List<JobDAO> jobs = conn.dequeueImmediate("n1", 150L, 1000);
          _txm.commit();

          assertNotNull(jobs);
          assertEquals(1, jobs.size());
          assertEquals(jobId1,jobs.get(0).getJobId());

          _txm.begin();;
          jobs = conn.dequeueImmediate("n1", 250L, 1000);
          _txm.commit();

          assertNotNull(jobs);
          assertEquals(1, jobs.size());
          assertEquals(jobId2,jobs.get(0).getJobId());
        }finally{
          conn.close();
        }
    }
    
    public void testScheduleImmediateMaxRows() throws Exception {
        SchedulerDAOConnection conn = _factory.getConnection();
        try{
          _txm.begin();;
          JobDAO job = conn.createJob(true, new Scheduler.JobDetails(), true, 100L);
          String jobId1 = job.getJobId();
          conn.insertJob(job,"n1", false);
          job = conn.createJob(true, new Scheduler.JobDetails(), true, 200L);
          String jobId2 = job.getJobId();
          conn.insertJob(job,"n1", false);
          _txm.commit();

          _txm.begin();;
          List<JobDAO> jobs = conn.dequeueImmediate("n1", 201L, 1);
          _txm.commit();
          assertNotNull(jobs);
          assertEquals(1, jobs.size());
          assertEquals(jobId1,jobs.get(0).getJobId());

          _txm.begin();;
          jobs = conn.dequeueImmediate("n1", 250L, 1000);
          _txm.commit();
          assertNotNull(jobs);
          assertEquals(1, jobs.size());
          assertEquals(jobId2,jobs.get(0).getJobId());
        }finally{
          conn.close();
        }
    }

    public void testScheduleImmediateNodeFilter() throws Exception {
        SchedulerDAOConnection conn = _factory.getConnection();
        try{
          _txm.begin();;
          JobDAO job = conn.createJob(true, new Scheduler.JobDetails(), true, 100L);
          String jobId1 = job.getJobId();
          conn.insertJob(job,"n1", false);
          job = conn.createJob(true, new Scheduler.JobDetails(), true, 200L);
          String jobId2 = job.getJobId();
          conn.insertJob(job,"n2", false);
          _txm.commit();

          _txm.begin();;
          List<JobDAO> jobs = conn.dequeueImmediate("n2", 300L, 1000);
          _txm.commit();
          
          assertNotNull(jobs);
          assertEquals(1, jobs.size());
          assertEquals(jobId2,jobs.get(0).getJobId());
        }finally{
          conn.close();
        }
    }

    public void testDeleteJob() throws Exception {

        SchedulerDAOConnection conn = _factory.getConnection();
        try{
          _txm.begin();;
          JobDAO job = conn.createJob(true, new Scheduler.JobDetails(), true, 100L);
          String jobId1 = job.getJobId();
          conn.insertJob(job,"n1", false);
          job = conn.createJob(true, new Scheduler.JobDetails(), true, 200L);
          String jobId2 = job.getJobId();
          conn.insertJob(job,"n2", false);
          _txm.commit();

          // try deleting, wrong jobid -- del should fail
          _txm.begin();;
          assertFalse(conn.deleteJob("j1x", "n1"));
          assertEquals(2,conn.getNodeIds().size());
          _txm.commit();

          // wrong nodeid
          _txm.begin();;
          assertFalse(conn.deleteJob(jobId1, "n1x"));
          assertEquals(2,conn.getNodeIds().size());
          _txm.commit();

          // now do the correct job
          _txm.begin();;
          assertTrue(conn.deleteJob(jobId1, "n1"));
          assertEquals(1,conn.getNodeIds().size());
          _txm.commit();
        }finally{
          conn.close();
        }
    }
    
    public void testUpgrade() throws Exception {

        SchedulerDAOConnection conn = _factory.getConnection();
        try{
          _txm.begin();;
          for (int i = 0; i < 200; ++i)
            conn.insertJob(conn.createJob(true, new Scheduler.JobDetails(), true, i),null, false);
          _txm.commit();

          _txm.begin();;
          int n1 = conn.updateAssignToNode("n1", 0, 3, 100);
          int n2 = conn.updateAssignToNode("n2", 1, 3, 100);
          int n3 = conn.updateAssignToNode("n3", 2, 3, 100);
          _txm.commit();
          // Make sure we got 100 upgraded nodes
          assertEquals(100,n1+n2+n3);

          // now do scheduling.
          _txm.begin();;
          assertEquals(n1,conn.dequeueImmediate("n1", 10000L, 1000).size());
          assertEquals(n2,conn.dequeueImmediate("n2", 10000L, 1000).size());
          assertEquals(n3,conn.dequeueImmediate("n3", 10000L, 1000).size());
          _txm.commit();
        }finally{
          conn.close();
        }
    }
    
    public void testMigration() throws Exception {
        SchedulerDAOConnection conn = _factory.getConnection();
        try{
          Scheduler.JobDetails j1 = new Scheduler.JobDetails();
          j1.getDetailsExt().put("type", "MATCHER");
          j1.getDetailsExt().put("iid", 1234L);
          j1.getDetailsExt().put("pid", new QName("http://test1", "test2").toString());
          j1.getDetailsExt().put("inmem", true);
          j1.getDetailsExt().put("ckey", "123~abcd");
          j1.getDetailsExt().put("channel", "123");
          j1.getDetailsExt().put("mexid", "mexid123");
          j1.getDetailsExt().put("correlatorId", "cid123");
          j1.getDetailsExt().put("retryCount", "15");

          _txm.begin();;
          conn.insertJob(conn.createJob(true, j1, true, 0L), null, false);
          conn.updateAssignToNode("m", 0, 3, 100);
          _txm.commit();

          _txm.begin();;
          Scheduler.JobDetails j2 = conn.dequeueImmediate("m", 10000L, 1000).get(0).getDetails();
          _txm.commit();

          assertEquals(j2.getType(), JobType.MATCHER);
          assertEquals(j2.getInstanceId(), (Object) 1234L);
          assertEquals(j2.getProcessId(), new QName("http://test1", "test2"));
          assertEquals(j2.getInMem(), (Object) true);
          assertEquals(j2.getCorrelationKey().toCanonicalString(), (Object) "123~abcd");
          assertEquals(j2.getChannel(), (Object) "123");
          assertEquals(j2.getMexId(), (Object) "mexid123");
          assertEquals(j2.getCorrelatorId(), (Object) "cid123");
          assertEquals(j2.getRetryCount(), (Object) 15);
        }finally{
          conn.close();
        }
    }
}
