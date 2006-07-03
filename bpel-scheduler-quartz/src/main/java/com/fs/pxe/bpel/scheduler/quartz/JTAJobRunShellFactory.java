package com.fs.pxe.bpel.scheduler.quartz;

import javax.transaction.TransactionManager;

import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.core.JobRunShell;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.SchedulingContext;

public class JTAJobRunShellFactory implements JobRunShellFactory {
  private Scheduler _scheduler;
  private SchedulingContext _schedCtxt;
  private TransactionManager _txm;

  /*
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   * 
   * Constructors.
   * 
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   */

  public JTAJobRunShellFactory(TransactionManager txm) {
      _txm = txm;
  }

  public void initialize(Scheduler scheduler, SchedulingContext schedCtxt)
          throws SchedulerConfigException {
      _scheduler = scheduler;
      _schedCtxt = schedCtxt;
  }

  /**
   * <p>
   * Called by the <class>{@link org.quartz.core.QuartzSchedulerThread}
   * </code> to obtain instances of <code>
   * {@link org.quartz.core.JobRunShell}</code>.
   * </p>
   */
  public JobRunShell borrowJobRunShell() {
      return new JTAJobRunShell(this, _scheduler, _schedCtxt, _txm);
  }

  /**
   * <p>
   * Called by the <class>{@link org.quartz.core.QuartzSchedulerThread}
   * </code> to return instances of <code>
   * {@link org.quartz.core.JobRunShell}</code>.
   * </p>
   */
  public void returnJobRunShell(JobRunShell jobRunShell) {
      jobRunShell.passivate();
  }


}
