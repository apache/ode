package org.apache.ode.scheduler.simple;

import java.util.Map;

import org.apache.ode.utils.GUID;

/**
 * Like a task, but a little bit better.
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
class Job extends Task {
    String jobId;
    boolean transacted;
    Map<String,Object> detail;
    boolean persisted = true;

    public Job(long when, boolean transacted, Map<String, Object> jobDetail) {
        this(when, new GUID().toString(),transacted,jobDetail);
    }
    
    public Job(long when, String jobId, boolean transacted,Map<String, Object> jobDetail) {
        super(when);
        this.jobId = jobId;
        this.detail = jobDetail;
        this.transacted = transacted;
    }

    
}
