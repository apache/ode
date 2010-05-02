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
package org.apache.ode.dao.jpa.scheduler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.dao.scheduler.JobDAO;

/**
 */
@Entity
@Table(name = "ODE_JOB")
@NamedQueries({
  @NamedQuery(name = "deleteJobs", query = "DELETE FROM JobDAOImpl AS j WHERE j.jobId = :job AND j.nodeId = :node"),
  @NamedQuery(name = "nodeIds", query = "SELECT DISTINCT j.nodeId FROM JobDAOImpl AS j WHERE j.nodeId IS NOT NULL"),
  @NamedQuery(name = "dequeueImmediate", query = "SELECT j FROM JobDAOImpl AS j WHERE j.nodeId = :node AND j.scheduled = false AND j.timestamp < :time ORDER BY j.timestamp"),
  @NamedQuery(name = "updateScheduled", query = "UPDATE JobDAOImpl AS j SET j.scheduled = true WHERE j.jobId in (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)"),
  @NamedQuery(name = "updateAssignToNode", query = "UPDATE JobDAOImpl AS j SET j.nodeId = :node WHERE j.nodeId IS NULL AND j.scheduled = false AND mod(j.timestamp,:numNode) = :i AND j.timestamp < :maxTime"),
  @NamedQuery(name = "updateReassign", query = "UPDATE JobDAOImpl AS j SET j.nodeId = :newNode, j.scheduled = false WHERE j.nodeId = :oldNode")
})
public class JobDAOImpl implements JobDAO, Serializable {

  private static final Log __log = LogFactory.getLog(JobDAOImpl.class);
  private String _jobId;
  private long _ts;
  private String _nodeId;
  private boolean _scheduled;
  private boolean _transacted;
  private boolean _persisted = true;
  JobDetails _details = new Scheduler.JobDetails();

  //JobDAO methods
  @Id
  @Column(name = "JOB_ID", length = 64)
  public String getJobId() {
    return _jobId;
  }

  @Column(name = "TRANSACTED", nullable = false)
  public boolean isTransacted() {
    return _transacted;
  }

  @Transient
  public JobDetails getDetails() {
    return _details;
  }

  @Transient
  public boolean isPersisted() {
    return _persisted;
  }

  @Transient
  public long getScheduledDate() {
    return getTimestamp();
  }

  //JPA Job getters/setters
  public void setJobId(String jobId) {
    _jobId = jobId;
  }

  @Column(name = "TS", nullable = false)
  public long getTimestamp() {
    return _ts;
  }

  public void setTimestamp(long ts) {
    _ts = ts;
  }

  @Column(name = "NODE_ID", length = 64)
  public String getNodeId() {
    return _nodeId;
  }

  public void setNodeId(String nodeId) {
    _nodeId = nodeId;
  }

  @Column(name = "SCHEDULED", nullable = false)
  public boolean isScheduled() {
    return _scheduled;
  }

  public void setScheduled(boolean scheduled) {
    this._scheduled = scheduled;
  }

  public void setTransacted(boolean transacted) {
    this._transacted = transacted;
  }

  public void setPersisted(boolean persisted) {
    this._persisted = persisted;
  }

  public void setDetails(JobDetails details) {
    _details = details;
  }

  //JPA JobDetails getters/setters
  @Column(name = "INSTANCE_ID")
  public long getInstanceId() {
    return _details.instanceId == null ? 0L : _details.instanceId.longValue();
  }

  public void setInstanceId(long instanceId) {
    _details.instanceId = instanceId;
  }

  @Column(name = "MEX_ID")
  public String getMexId() {
    return _details.mexId;
  }

  public void setMexId(String mexId) {
    _details.mexId = mexId;
  }

  @Column(name = "PROCESS_ID")
  public String getProcessId() {
    return _details.processId;
  }

  public void setProcessId(String processId) {
    _details.processId = processId;
  }

  @Column(name = "TYPE")
  public String getType() {
    return _details.type;
  }

  public void setType(String type) {
    _details.type = type!=null?type:"";
  }

  @Column(name = "CHANNEL")
  public String getChannel() {
    return _details.channel;
  }

  public void setChannel(String channel) {
    _details.channel = channel;
  }

  @Column(name = "CORRELATOR_ID")
  public String getCorrelatorId() {
    return _details.correlatorId;
  }

  public void setCorrelatorId(String correlatorId) {
    _details.correlatorId = correlatorId;
  }

  @Column(name = "CORRELATION_KEY")
  public String getCorrelationKey() {
    return _details.correlationKey; 
  }

  public void setCorrelationKey(String correlationKey) {
    _details.correlationKey = correlationKey;
  }

  @Column(name = "RETRY_COUNT")
  public int getRetryCount() {
    return _details.retryCount == null ? 0 : _details.retryCount.intValue();
  }

  public void setRetryCount(int retryCount) {
    _details.retryCount = retryCount;
  }

  @Column(name = "IN_MEM")
  public boolean isInMem() {
    return _details.inMem == null ? false : _details.inMem.booleanValue();
  }

  public void setInMem(boolean inMem) {
    _details.inMem = inMem;
  }

  //should not lazy load, it is possible getDetails() called before this
  @Lob
  @Column(name = "DETAILS_EXT")
  public byte[] getDetailsExt() {
    if (_details.detailsExt != null) {
      try {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(_details.detailsExt);
        os.close();
        return bos.toByteArray();
      } catch (Exception e) {
        __log.error("Error in getDetailsExt ", e);
      }
    }
    return null;
  }

  public void setDetailsExt(byte[] detailsExt) {
    if (detailsExt != null) {
      try {
        ByteArrayInputStream bis = new ByteArrayInputStream(detailsExt);
        ObjectInputStream is = new ObjectInputStream(bis);
        _details.detailsExt = (Map<String, Object>) is.readObject();
        is.close();
      } catch (Exception e) {
        __log.error("Error in setDetailsExt ", e);
      }
    }
  }
}
