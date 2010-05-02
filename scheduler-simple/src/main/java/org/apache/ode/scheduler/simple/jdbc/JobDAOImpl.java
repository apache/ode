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

package org.apache.ode.scheduler.simple.jdbc;


import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.dao.scheduler.JobDAO;
import org.apache.ode.utils.GUID;

/**
 * Like a task, but a little bit better.
 *
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
class JobDAOImpl implements JobDAO {

    long scheduledDate;
    String jobId;
    boolean transacted;
    JobDetails detail;
    boolean persisted = true;

    public JobDAOImpl(long when, String jobId, boolean transacted, JobDetails jobDetail) {
        this.scheduledDate = when;
        this.jobId = jobId;
        this.detail = jobDetail;
        this.transacted = transacted;
    }

  public String getJobId() {
    return jobId;
  }

  public boolean isTransacted() {
    return transacted;
  }

  public JobDetails getDetails() {
    return detail;
  }

  public boolean isPersisted() {
    return persisted;
  }

  public long getScheduledDate() {
    return scheduledDate;
  }

}
