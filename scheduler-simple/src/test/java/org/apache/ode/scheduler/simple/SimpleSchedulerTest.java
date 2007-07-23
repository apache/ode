package org.apache.ode.scheduler.simple;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import org.apache.ode.bpel.iapi.Scheduler.JobInfo;
import org.apache.ode.bpel.iapi.Scheduler.JobProcessor;
import org.apache.ode.bpel.iapi.Scheduler.JobProcessorException;
import org.objectweb.jotm.Jotm;

public class SimpleSchedulerTest extends TestCase implements JobProcessor {

    DelegateSupport _ds;

    SimpleScheduler _scheduler;

    ArrayList<JobInfo> _jobs;
    ArrayList<JobInfo> _commit;

    TransactionManager _txm;

    
    Jotm _jotm;

    public void setUp() throws Exception {
        _jotm = new Jotm(true, false);
        _txm = _jotm.getTransactionManager();
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
        _txm.begin();
        String jobId;
        try {
            jobId = _scheduler.schedulePersistedJob(newDetail("123"), new Date(System.currentTimeMillis() + 200));
            Thread.sleep(100);
            // Make sure we don't schedule until commit.
            assertEquals(0, _jobs.size());
        } finally {
            _txm.commit();
        }
        // Delete from DB
        assertEquals(true,_ds.delegate().deleteJob(jobId, "n1"));
        // Wait for the job to be execed.
        Thread.sleep(250);
        // Should execute job,
        assertEquals(1, _jobs.size());
        // But should not commit.
        assertEquals(0, _commit.size());
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
        _scheduler.setNearFutureInterval(10000);
        _scheduler.setImmediateInterval(5000);
        _scheduler.start();

        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("123"), new Date(System.currentTimeMillis() + 7500));
        } finally {
            _txm.commit();
        }

        Thread.sleep(7500);
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

        Thread.sleep(7500);
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
            _scheduler.schedulePersistedJob(newDetail("far"), new Date(System.currentTimeMillis() + 2500));
        } finally {
            _txm.commit();
        }

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
