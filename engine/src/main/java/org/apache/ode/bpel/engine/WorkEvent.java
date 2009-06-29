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

package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.bpel.iapi.Scheduler.JobDetailsImpl;
import org.apache.ode.bpel.iapi.Scheduler.JobType;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper around job detail map.
 * 
 */
public class WorkEvent {

    private JobDetails _jobDetail;

    WorkEvent(JobDetails jobDetail) {
        _jobDetail = jobDetail;
    }

    WorkEvent() {
        _jobDetail = new JobDetailsImpl();
    }

    JobDetails getDetails() {
        return _jobDetail;
    }

    public String toString() {
        return "WorkEvent" + _jobDetail;
    }

    public String getChannel() {
        return _jobDetail.getChannel();
    }

    public CorrelationKey getCorrelationKey() {
        return _jobDetail.getCorrelationKey();
    }

    public String getCorrelatorId() {
        return _jobDetail.getCorrelatorId();
    }

    public Map<String, Object> getDetailsExt() {
        return _jobDetail.getDetailsExt();
    }

    public Boolean getInMem() {
        return _jobDetail.getInMem();
    }

    public Long getInstanceId() {
        return _jobDetail.getInstanceId();
    }

    public String getMexId() {
        return _jobDetail.getMexId();
    }

    public QName getProcessId() {
        return _jobDetail.getProcessId();
    }

    public Integer getRetryCount() {
        return _jobDetail.getRetryCount();
    }

    public JobType getType() {
        return _jobDetail.getType();
    }

    public void setChannel(String channel) {
        _jobDetail.setChannel(channel);
    }

    public void setCorrelationKey(CorrelationKey correlationKey) {
        _jobDetail.setCorrelationKey(correlationKey);
    }

    public void setCorrelatorId(String correlatorId) {
        _jobDetail.setCorrelatorId(correlatorId);
    }

    public void setDetailsExt(Map<String, Object> detailsExt) {
        _jobDetail.setDetailsExt(detailsExt);
    }

    public void setInMem(Boolean inMem) {
        _jobDetail.setInMem(inMem);
    }

    public void setInstanceId(Long iid) {
        _jobDetail.setInstanceId(iid);
    }

    public void setMexId(String mexId) {
        _jobDetail.setMexId(mexId);
    }

    public void setProcessId(QName processId) {
        _jobDetail.setProcessId(processId);
    }

    public void setRetryCount(Integer retryCount) {
        _jobDetail.setRetryCount(retryCount);
    }

    public void setType(JobType type) {
        _jobDetail.setType(type);
    }
    
    
}

