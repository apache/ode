package com.fs.pxe.bpel.scheduler.quartz;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShell;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.SchedulingContext;

/**
 * A riff on the Quartz JTA implementation that dispenses with the use
 * of the UserTransaction interface, and instead goes direct to the source,
 * the TransactionManager.
 *
 */
public class JTAJobRunShell extends JobRunShell {

  /*
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   * 
   * Data members.
   * 
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   */

  private TransactionManager _txm;

  /**
   * <p>
   * Create a JTAJobRunShell instance with the given settings.
   * </p>
   */
  public JTAJobRunShell(JobRunShellFactory jobRunShellFactory,
          Scheduler scheduler, SchedulingContext schdCtxt,
          TransactionManager tm) {
      super(jobRunShellFactory, scheduler, schdCtxt);
      _txm = tm;
  }

  /*
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   * 
   * Interface.
   * 
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   */

  protected void begin() throws SchedulerException {
      try {
          _txm.begin();
      } catch (Exception nse) {

          throw new SchedulerException(
                  "JTAJobRunShell could not start UserTransaction.", nse);
      }
  }

  protected void complete(boolean successfulExecution)
          throws SchedulerException {

      if (_txm == null) return;

      try {
          if (_txm.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
              log.debug("UserTransaction marked for rollback only.");
              successfulExecution = false;
          }
      } catch (SystemException e) {
          throw new SchedulerException(
                  "JTAJobRunShell could not read UserTransaction status.", e);
      }

      if (successfulExecution) {
          try {
              log.debug("Committing UserTransaction.");
              _txm.commit();
          } catch (Exception nse) {
              throw new SchedulerException(
                      "JTAJobRunShell could not commit UserTransaction.", nse);
          }
      } else {
          try {
              log.debug("Rolling-back UserTransaction.");
              _txm.rollback();
          } catch (Exception nse) {
              throw new SchedulerException(
                      "JTAJobRunShell could not rollback UserTransaction.",
                      nse);
          }
      }

      _txm = null;
  }


}
