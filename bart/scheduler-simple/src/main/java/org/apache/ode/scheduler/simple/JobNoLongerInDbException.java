package org.apache.ode.scheduler.simple;

/**
 * Exception thrown if an attempt has been made to commit a job that is no longer in the 
 * database. This can happen if multiple nodes through some bizarre bad luck happen to 
 * execute the same job. In any case, the second node will receive this exception which
 * will cause a roll-back of the transaction running the job.  
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
public class JobNoLongerInDbException extends Exception  {

    private static final long serialVersionUID = 1L;

    public JobNoLongerInDbException(String jobId, String nodeId) {
        super("Job no longer in db: "+ jobId + " nodeId=" + nodeId);
    }

}
