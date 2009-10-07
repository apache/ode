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
 * Used to store large data sets into a single table. When an HObject instance
 * needs to store as part of its state large binary or text data, a reference to
 * an instance of this class must be created.
 * 
 * @hibernate.class table="LARGE_DATA"
 * @hibernate.query name="SELECT_ACTIVITY_RECOVERY_LDATA_IDS_BY_INSTANCES" query="select a.details.id from HActivityRecovery as a where a.instance in (:instances) and a.details is not null"
 * @hibernate.query name="SELECT_JACOB_LDATA_IDS_BY_INSTANCES" query="select i.jacobState.id from HProcessInstance as i where i in (:instances) and i.jacobState is not null"
 * @hibernate.query name="SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_1" query="select mex.request.messageData.id from HMessageExchange mex where mex.instance in (:instances) and mex.request.messageData is not null"
 * @hibernate.query name="SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_2" query="select mex.response.messageData.id from HMessageExchange mex where mex.instance in (:instances) and mex.response.messageData is not null"
 * @hibernate.query name="SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_3" query="select mex.request.header.id from HMessageExchange mex where mex.instance in (:instances) and mex.request.header is not null"
 * @hibernate.query name="SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_4" query="select mex.response.header.id from HMessageExchange mex where mex.instance in (:instances) and mex.response.header is not null"
 * @hibernate.query name="SELECT_MEX_LDATA_IDS_BY_INSTANCES_1" query="select e.endpoint.id from HMessageExchange as e where e.instance in (:instances) and e.endpoint is not null"
 * @hibernate.query name="SELECT_MEX_LDATA_IDS_BY_INSTANCES_2" query="select e.callbackEndpoint.id from HMessageExchange as e where e.instance in (:instances) and e.callbackEndpoint is not null"
 *
 * @hibernate.query name="SELECT_EVENT_LDATA_IDS_BY_INSTANCES" query="select e.data.id from HBpelEvent as e where e.instance in (:instances) and e.data is not null"
 * @hibernate.query name="SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_1" query="select mex.request.messageData.id from HMessageExchange mex, HCorrelatorMessage cm where mex = cm.messageExchange and mex.instance in (:instances) and mex.request.messageData is not null"
 * @hibernate.query name="SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_2" query="select mex.response.messageData.id from HMessageExchange mex, HCorrelatorMessage cm where mex = cm.messageExchange and mex.instance in (:instances) and mex.response.messageData is not null"
 * @hibernate.query name="SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_3" query="select mex.request.header.id from HMessageExchange mex, HCorrelatorMessage cm where mex = cm.messageExchange and mex.instance in (:instances) and mex.request.header is not null"
 * @hibernate.query name="SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_4" query="select mex.response.header.id from HMessageExchange mex, HCorrelatorMessage cm where mex = cm.messageExchange and mex.instance in (:instances) and mex.response.header is not null"
 * @hibernate.query name="SELECT_XMLDATA_LDATA_IDS_BY_INSTANCES" query="select x.data.id from HXmlData as x where x.instance in (:instances) and x.data is not null"
 * @hibernate.query name="SELECT_PARTNER_LINK_LDATA_IDS_BY_INSTANCES_1" query="select l.myEPR.id from HPartnerLink as l where l.scope.instance in (:instances) and l.myEPR is not null"
 * @hibernate.query name="SELECT_PARTNER_LINK_LDATA_IDS_BY_INSTANCES_2" query="select l.partnerEPR.id from HPartnerLink as l where l.scope.instance in (:instances) and l.partnerEPR is not null"
 * @hibernate.query name="SELECT_FAULT_LDATA_IDS_BY_INSTANCE_IDS" query="select f.data.id from HFaultData as f, HProcessInstance as i where f.id = i.fault and i.id in (:instanceIds) and f.data is not null"

 * @hibernate.query name="SELECT_MESSAGE_LDATA_IDS_BY_MEX_1" query="select mex.request.messageData.id from HMessageExchange mex where mex = :mex and mex.request.messageData is not null"
 * @hibernate.query name="SELECT_MESSAGE_LDATA_IDS_BY_MEX_2" query="select mex.response.messageData.id from HMessageExchange mex where mex = :mex and mex.response.messageData is not null"
 * @hibernate.query name="SELECT_MESSAGE_LDATA_IDS_BY_MEX_3" query="select mex.request.header.id from HMessageExchange mex where mex = :mex and mex.request.header is not null"
 * @hibernate.query name="SELECT_MESSAGE_LDATA_IDS_BY_MEX_4" query="select mex.response.header.id from HMessageExchange mex where mex = :mex and mex.response.header is not null"
 */
public class HLargeData extends HObject {
    public final static String SELECT_ACTIVITY_RECOVERY_LDATA_IDS_BY_INSTANCES = "SELECT_ACTIVITY_RECOVERY_LDATA_IDS_BY_INSTANCES";
    public final static String SELECT_JACOB_LDATA_IDS_BY_INSTANCES = "SELECT_JACOB_LDATA_IDS_BY_INSTANCES";
    public final static String SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_1 = "SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_1";
    public final static String SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_2 = "SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_2";
    public final static String SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_3 = "SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_3";
    public final static String SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_4 = "SELECT_MESSAGE_LDATA_IDS_BY_INSTANCES_4";
    public final static String SELECT_MEX_LDATA_IDS_BY_INSTANCES_1 = "SELECT_MEX_LDATA_IDS_BY_INSTANCES_1";
    public final static String SELECT_MEX_LDATA_IDS_BY_INSTANCES_2 = "SELECT_MEX_LDATA_IDS_BY_INSTANCES_2";

    public final static String SELECT_EVENT_LDATA_IDS_BY_INSTANCES = "SELECT_EVENT_LDATA_IDS_BY_INSTANCES";
    public final static String SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_1 = "SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_1";
    public final static String SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_2 = "SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_2";
    public final static String SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_3 = "SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_3";
    public final static String SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_4 = "SELECT_UNMATCHED_MESSAGE_LDATA_IDS_BY_INSTANCES_4";
    public final static String SELECT_XMLDATA_LDATA_IDS_BY_INSTANCES = "SELECT_XMLDATA_LDATA_IDS_BY_INSTANCES";
    public final static String SELECT_PARTNER_LINK_LDATA_IDS_BY_INSTANCES_1 = "SELECT_PARTNER_LINK_LDATA_IDS_BY_INSTANCES_1";
    public final static String SELECT_PARTNER_LINK_LDATA_IDS_BY_INSTANCES_2 = "SELECT_PARTNER_LINK_LDATA_IDS_BY_INSTANCES_2";
    public final static String SELECT_FAULT_LDATA_IDS_BY_INSTANCE_IDS = "SELECT_FAULT_LDATA_IDS_BY_INSTANCE_IDS";
    
    public final static String SELECT_MESSAGE_LDATA_IDS_BY_MEX_1 = "SELECT_MESSAGE_LDATA_IDS_BY_MEX_1";
    public final static String SELECT_MESSAGE_LDATA_IDS_BY_MEX_2 = "SELECT_MESSAGE_LDATA_IDS_BY_MEX_2";
    public final static String SELECT_MESSAGE_LDATA_IDS_BY_MEX_3 = "SELECT_MESSAGE_LDATA_IDS_BY_MEX_3";
    public final static String SELECT_MESSAGE_LDATA_IDS_BY_MEX_4 = "SELECT_MESSAGE_LDATA_IDS_BY_MEX_4";
	
    private byte[] binary = null;

    public HLargeData() {
        super();
    }

    public HLargeData(byte[] binary) {
        super();
        this.binary = binary;
    }

    public HLargeData(String text) {
        super();
        this.binary = text.getBytes();
    }

    /**
     * @hibernate.property type="binary" length="2G"
     * 
     * @hibernate.column name="BIN_DATA" sql-type="blob(2G)"
     */
    public byte[] getBinary() {
        return binary;
    }

    public void setBinary(byte[] binary) {
        this.binary = binary;
    }

    public String getText() {
        return new String(binary);
    }
}
