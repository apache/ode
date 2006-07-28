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

package org.apache.ode.bpel.scheduler.quartz;

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
