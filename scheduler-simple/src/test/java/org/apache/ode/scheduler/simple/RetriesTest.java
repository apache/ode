package org.apache.ode.scheduler.simple;

import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;

import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

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

        Thread.sleep(5000);
        assertEquals(3, _tried);
    }


    public void onScheduledJob(Scheduler.JobInfo jobInfo) throws Scheduler.JobProcessorException {
        _tried++;
        throw new Scheduler.JobProcessorException(jobInfo.retryCount < 2);
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
