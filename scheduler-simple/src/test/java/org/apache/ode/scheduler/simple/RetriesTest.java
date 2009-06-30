package org.apache.ode.scheduler.simple;

import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;

import javax.transaction.TransactionManager;
import java.util.*;

import junit.framework.TestCase;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public class RetriesTest extends TestCase implements Scheduler.JobProcessor {
    private static final Log __log = LogFactory.getLog(RetriesTest.class);
    
    DelegateSupport _ds;
    SimpleScheduler _scheduler;
    ArrayList<Scheduler.JobInfo> _jobs;
    ArrayList<Scheduler.JobInfo> _commit;
    TransactionManager _txm;
    int _tried = 0;

    public void setUp() throws Exception {
        _txm = new GeronimoTransactionManager();
        _ds = new DelegateSupport();

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
        assertEquals(3, _tried);
    }


    public void onScheduledJob(Scheduler.JobInfo jobInfo) throws Scheduler.JobProcessorException {
        _tried++;
        
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
        SimpleScheduler scheduler = new SimpleScheduler(nodeId, _ds.delegate(), new Properties());
        scheduler.setJobProcessor(this);
        scheduler.setTransactionManager(_txm);
        return scheduler;
    }

}
