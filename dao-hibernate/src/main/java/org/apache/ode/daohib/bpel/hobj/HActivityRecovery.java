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

package org.apache.ode.daohib.bpel.hobj;

import java.util.Date;

/**
 * Persistent representation of activity recovery information.
 * 
 * @hibernate.class table="BPEL_ACTIVITY_RECOVERY"
 * @hibernate.query name="SELECT_ACTIVITY_RECOVERY_IDS_BY_INSTANCES" query="select id from HActivityRecovery as a where a.instance in (:instances)" 
 */
public class HActivityRecovery extends HObject {
    public final static String SELECT_ACTIVITY_RECOVERY_IDS_BY_INSTANCES = "SELECT_ACTIVITY_RECOVERY_IDS_BY_INSTANCES";

    /** Process instance to which this scope belongs. */
    private HProcessInstance _instance;
    private long _activityId;
    private String _channel;
    private String _reason;
    private Date _dateTime;
    private HLargeData _details;
    private String _actions;
    private int _retries;

    /**
     * Get the {@link HProcessInstance} to which this scope object belongs.
     * 
     * @hibernate.many-to-one column="PIID" foreign-key="none"
     */
    public HProcessInstance getInstance() {
        return _instance;
    }

    /** @see #getInstance() */
    public void setInstance(HProcessInstance instance) {
        _instance = instance;
    }

    /**
     * @hibernate.property column="AID"
     */
    public long getActivityId() {
        return _activityId;
    }

    public void setActivityId(long activityId) {
        _activityId = activityId;
    }

    /**
     * @hibernate.property column="CHANNEL"
     */
    public String getChannel() {
        return _channel;
    }

    public void setChannel(String channel) {
        _channel = channel;
    }

    /**
     * @hibernate.property column="REASON"
     */
    public String getReason() {
        return _reason;
    }

    public void setReason(String reason) {
        _reason = reason;
    }

    /**
     * @hibernate.property column="DATE_TIME"
     */
    public Date getDateTime() {
        return _dateTime;
    }

    public void setDateTime(Date dateTime) {
        _dateTime = dateTime;
    }

    /**
     * @hibernate.many-to-one column="LDATA_ID" cascade="delete" foreign-key="none"
     */
    public HLargeData getDetails() {
        return _details;
    }

    public void setDetails(HLargeData details) {
        _details = details;
    }

    /**
     * @hibernate.property column="ACTIONS"
     */
    public String getActions() {
        return _actions;
    }

    public void setActions(String actions) {
        _actions = actions;
    }

    /**
     * @hibernate.property column="RETRIES"
     */
    public int getRetries() {
        return _retries;
    }

    public void setRetries(int retries) {
        _retries = retries;
    }
}
