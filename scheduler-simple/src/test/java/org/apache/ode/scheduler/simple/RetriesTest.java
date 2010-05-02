package org.apache.ode.scheduler.simple;

import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

import org.apache.ode.dao.scheduler.SchedulerDAOConnection;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public class RetriesTest extends SchedulerTestBase implements Scheduler.JobProcessor {
    private static final Log __log = LogFactory.getLog(RetriesTest.class);
    

    SimpleScheduler _scheduler;
    ArrayList<Scheduler.JobInfo> _jobs;
    ArrayList<Scheduler.JobInfo> _commit;
    
    int _tried = 0;

    public void setUp() throws Exception {
        super.setUp();

        _scheduler = newScheduler("n1");
        _jobs = new ArrayList<Scheduler.JobInfo>(100);
        _commit = new ArrayList<Scheduler.JobInfo>(100);
    }

    public void tearDown() throws Exception {
        _scheduler.shutdown();
        super.tearDown();
    }
    
    public void testRetries() throws Exception {
        // speed things up a bit to hit the right code paths
        _scheduler.setNearFutureInterval(5000);
        _scheduler.setImmediateInterval(1000);
        _scheduler.start();

        SchedulerDAOConnection conn = _factory.getConnection();
        _txm.begin();
        try {
            _scheduler.schedulePersistedJob(newDetail("123"), new Date());
        } finally {
            _txm.commit();
            conn.close();
        }

        Thread.sleep(10000);
        assertEquals(3, _tried);
    }


    public void onScheduledJob(Scheduler.JobInfo jobInfo) throws Scheduler.JobProcessorException {
        _tried++;
         __log.debug("onScheduledJob " + jobInfo.jobName);
        if (jobInfo.retryCount < 2) {
            __log.debug("retrying " + _tried);
            throw new Scheduler.JobProcessorException(true);
        } else {
            __log.debug("completing " + _tried);
        }
    }

    Scheduler.JobDetails newDetail(String x) {
        Scheduler.JobDetails jd = new Scheduler.JobDetails();
        jd.getDetailsExt().put("foo", x);
        return jd;
    }

    private SimpleScheduler newScheduler(String nodeId) {
        SimpleScheduler scheduler = new SimpleScheduler(nodeId, _factory, _txm, new Properties());
        scheduler.setJobProcessor(this);
        return scheduler;
    }

}
