package org.apache.ode.bpel.scheduler.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz {@link Job} implementation that calls back into the BPEL engine.
 */
public class JobImpl implements Job {

  public void execute(JobExecutionContext jobcontext) throws JobExecutionException {
    QuartzSchedulerImpl.execute(jobcontext);
  }

}
