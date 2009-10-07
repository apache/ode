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

/**
 * @hibernate.class table="BPEL_SELECTORS" lazy="true"
 * @hibernate.query name="SELECT_MESSAGE_ROUTE_IDS_BY_PROCESS" query="select id from HCorrelatorSelector as m where m.correlator in(select c from HCorrelator c where c.process = :process)"
 * @hibernate.query name="SELECT_MESSAGE_ROUTE_IDS_BY_INSTANCES" query="select id from HCorrelatorSelector as m where m.instance in (:instances))"
 */
public class HCorrelatorSelector extends HObject {
    public static final String SELECT_MESSAGE_ROUTE_IDS_BY_PROCESS = "SELECT_MESSAGE_ROUTE_IDS_BY_PROCESS";
    public static final String SELECT_MESSAGE_ROUTE_IDS_BY_INSTANCES = "SELECT_MESSAGE_ROUTE_IDS_BY_INSTANCES";

    private HProcessInstance _instance;
    private String _groupId;
    private int _idx;
    private HCorrelator _correlator;
    private String _correlationKey;
    private String _processType;
    
    /**
     * @hibernate.many-to-one column="PIID" not-null="true" foreign-key="none"
     */
    public HProcessInstance getInstance() {
        return _instance;
    }

    public void setInstance(HProcessInstance instance) {
        _instance = instance;
    }

    /** after c
     * @hibernate.property column="SELGRPID"
     * @hibernate.column name="SELGRPID" index="IDX_SELECTOR_SELGRPID" not-null="true"
     */
    public String getGroupId() {
        return _groupId;
    }

    public void setGroupId(String groupId) {
        _groupId = groupId;
    }

    /**
     * @hibernate.property column="IDX" not-null="true"
     */
    public int getIndex() {
        return _idx;
    }

    public void setIndex(int idx) {
        _idx = idx;
    }
    
    
    @Override
    public String toString() {
        return "{HCorrelatorSelector correlator=" + this.getCorrelator() + ", ckey=" + getCorrelationKey() + 
        ", groupId=" + getGroupId() + ", idx=" + getIndex() + ", iid=" + getInstance().getId() + "}";
    }

    /**
     * @hibernate.property column="CORRELATION_KEY" not-null="true"
     * @hibernate.column name="CORRELATION_KEY"
     *                   index="IDX_SELECTOR_CKEY"
     *                   not-null="true"
     *                   unique-key="UNIQ_SELECTOR"
     *                   
     */
    public String getCorrelationKey() {
        return _correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        _correlationKey = correlationKey;
    }

    /**
     * @hibernate.property column="PROC_TYPE" not-null="true"
     */
    public String getProcessType() {
        return _processType;
    }

    public void setProcessType(String _processType) {
        this._processType = _processType;
    }

    /**
     * @hibernate.many-to-one not-null="true" foreign-key="none"
     * @hibernate.column name="CORRELATOR" not-null="true" 
     *          index="IDX_SELECTOR_CORRELATOR" unique-key="UNIQ_SELECTOR"
     */
    public HCorrelator getCorrelator() {
        return _correlator;
    }

    public void setCorrelator(HCorrelator correlator) {
        _correlator = correlator;
    }

}
