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

import java.sql.Timestamp;

/**
 * Row representation of a BPEL event.
 * 
 * @hibernate.class table="BPEL_EVENT"
 * @hibernate.query name="SELECT_EVENT_IDS_BY_INSTANCES" query="select id from HBpelEvent as e where e.instance in (:instances)"
 */
public class HBpelEvent extends HObject {
    public static final String SELECT_EVENT_IDS_BY_INSTANCES = "SELECT_EVENT_IDS_BY_INSTANCES";

    private Timestamp _tstamp;
    private String _type;
    private String _detail;

    private HProcess _process;
    private HProcessInstance _instance;
    private HLargeData _data;

    /** Scope identifier, possibly null. */
    private Long _scopeId;

    /**
     * @hibernate.many-to-one column="IID" foreign-key="none"
     */
    public HProcessInstance getInstance() {
        return _instance;
    }

    public void setInstance(HProcessInstance instance) {
        _instance = instance;
    }

    /**
     * @hibernate.many-to-one column="PID" foreign-key="none"
     */
    public HProcess getProcess() {
        return _process;
    }

    public void setProcess(HProcess process) {
        _process = process;
    }

    /**
     * @hibernate.property column="TSTAMP"
     */
    public Timestamp getTstamp() {
        return _tstamp;
    }

    public void setTstamp(Timestamp tstamp) {
        _tstamp = tstamp;
    }

    /**
     * @hibernate.property column="TYPE"
     */
    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    /**
     * TODO Check 32000 is enough for details
     */
    /**
     * @hibernate.property column="DETAIL" type="text" length="32000"
     */
    public String getDetail() {
        return _detail;
    }

    public void setDetail(String detail) {
        _detail = detail;
    }

    /**
     * @hibernate.many-to-one column="LDATA_ID" cascade="delete" foreign-key="none"
     */
    public HLargeData getData() {
        return _data;
    }

    public void setData(HLargeData data) {
        _data = data;
    }

    /**
     * Get the scope identifier of the scope associated with this event. Note,
     * that this is not implemented as a many-to-one relationship because when
     * scopes are deleted from the database we do not want their events to
     * suffer the same fate.
     * 
     * @hibernate.property column="SID"
     */
    public Long getScopeId() {
        return _scopeId;
    }

    public void setScopeId(Long scopeId) {
        _scopeId = scopeId;
    }
}
