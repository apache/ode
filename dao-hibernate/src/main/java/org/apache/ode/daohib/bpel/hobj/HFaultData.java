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
 * Persistent representation of a fault.
 * 
 * @hibernate.class table="BPEL_FAULT"
 * @hibernate.query name="SELECT_FAULT_IDS_BY_INSTANCES" query="select id from HFaultData as f where f in (select i.fault from HProcessInstance as i where i in (:instances))"
 */
public class HFaultData extends HObject {
    public final static String SELECT_FAULT_IDS_BY_INSTANCES = "SELECT_FAULT_IDS_BY_INSTANCES";

    private String _name;
    private String _explanation;
    private HLargeData _data;
    private int _lineNo;
    private int _activityId;

    /**
     * @hibernate.property column="FAULTNAME"
     */
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
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
     * @hibernate.property column="EXPLANATION" length="4000"
     */
    public String getExplanation() {
        return _explanation;
    }

    public void setExplanation(String explanation) {
        // Don't want to make this a blob, truncating to avoid errors
        if (explanation != null && explanation.length() > 4000)
            explanation = explanation.substring(0, 3999);
        _explanation = explanation;
    }

    /**
     * @hibernate.property column="LINE_NUM"
     */
    public int getLineNo() {
        return _lineNo;
    }

    public void setLineNo(int lineNo) {
        _lineNo = lineNo;
    }

    /**
     * @hibernate.property column="AID"
     */
    public int getActivityId() {
        return _activityId;
    }

    public void setActivityId(int activityId) {
        _activityId = activityId;
    }
}
