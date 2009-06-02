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
 * @hibernate.query name="DELETE_ACTIVITY_RECOVERY_LDATA_BY_INSTANCES" query="delete from HLargeData as d where d in(select a.details from HActivityRecovery as a where a.instance in (:instances))"
 * @hibernate.query name="DELETE_JACOB_LDATA_BY_INSTANCES" query="delete from HLargeData as d where d in(select i.jacobState from HProcessInstance as i where i in (:instances))"
 * @hibernate.query name="DELETE_MESSAGE_LDATA_BY_INSTANCES" query="delete from HLargeData as d where d in(select x.request.messageData from HMessageExchange x where x.instance in(:instances)) or d in(select x.response.messageData from HMessageExchange x where x.instance in(:instances)) or d in(select x.request.header from HMessageExchange x where x.instance in (:instances)) or d in(select x.response.header from HMessageExchange x where x.instance in (:instances))"
 * @hibernate.query name="DELETE_MEX_LDATA_BY_INSTANCES" query="delete from HLargeData as d where d in(select e.endpoint from HMessageExchange as e where e.instance in (:instances)) or d IN(select e.callbackEndpoint from HMessageExchange as e where e.instance in (:instances))"
 *
 * @hibernate.query name="DELETE_EVENT_LDATA_BY_INSTANCES" query="delete from HLargeData as d where d in(select e.data from HBpelEvent as e where e.instance in (:instances))"
 * @hibernate.query name="DELETE_UNMATCHED_MESSAGE_LDATA_BY_INSTANCES" query="delete from HLargeData as d where d in(select cm.messageExchange.request.messageData from HCorrelatorMessage cm where cm.messageExchange.instance in (:instances)) or d in(select cm.messageExchange.response.messageData from HCorrelatorMessage cm where cm.messageExchange.instance in (:instances)) or d in(select cm.messageExchange.request.header from HCorrelatorMessage cm where cm.messageExchange.instance in (:instances)) or d in(select cm.messageExchange.response.header from HCorrelatorMessage cm where cm.messageExchange.instance in (:instances))"
 * @hibernate.query name="DELETE_XMLDATA_LDATA_BY_INSTANCES" query="delete from HLargeData as d where d in(select x.data from HXmlData as x where x.instance in (:instances))"
 * @hibernate.query name="DELETE_PARTNER_LINK_LDATA_BY_INSTANCES" query="delete from HLargeData as d where d in(select l.myEPR from HPartnerLink as l where l.scope.instance in (:instances)) or d IN(select l.partnerEPR from HPartnerLink as l where l.scope.instance in (:instances))"
 * @hibernate.query name="DELETE_FAULT_LDATA_BY_INSTANCE_IDS" query="delete from HLargeData as d where d in(select f.data from HFaultData as f, HProcessInstance as i where f.id = i.fault and i.id in (:instanceIds))"

 * @hibernate.query name="DELETE_MESSAGE_LDATA_BY_MEX" query="delete from HLargeData as d where d in(select m.messageData from HMessage m, HMessageExchange x where (m = x.request or m = x.response) and x = :mex) or d in(select m.header from HMessage m, HMessageExchange x where (m = x.request or m = x.response) and x = :mex)"
 */
public class HLargeData extends HObject {
//@hibernate.query name="DELETE_MESSAGE_LDATA_BY_INSTANCES" query="delete from HLargeData as d where d in(select m.messageData from HMessage m, HMessageExchange x where (m = x.request or m = x.response) and x.instance in(:instances)) or d in(select m.header from HMessage m, HMessageExchange x where (m = x.request or m = x.response) and x.instance in (:instances))"
    
    public final static String DELETE_ACTIVITY_RECOVERY_LDATA_BY_INSTANCES = "DELETE_ACTIVITY_RECOVERY_LDATA_BY_INSTANCES";
    public final static String DELETE_JACOB_LDATA_BY_INSTANCES = "DELETE_JACOB_LDATA_BY_INSTANCES";
    public final static String DELETE_MESSAGE_LDATA_BY_INSTANCES = "DELETE_MESSAGE_LDATA_BY_INSTANCES";
    public final static String DELETE_MEX_LDATA_BY_INSTANCES = "DELETE_MEX_LDATA_BY_INSTANCES";

    public final static String DELETE_EVENT_LDATA_BY_INSTANCES = "DELETE_EVENT_LDATA_BY_INSTANCES";
    public final static String DELETE_MESSAGE_LDATA_BY_MEX = "DELETE_MESSAGE_LDATA_BY_MEX";
    public final static String DELETE_UNMATCHED_MESSAGE_LDATA_BY_INSTANCES = "DELETE_UNMATCHED_MESSAGE_LDATA_BY_INSTANCES";
    public final static String DELETE_XMLDATA_LDATA_BY_INSTANCES = "DELETE_XMLDATA_LDATA_BY_INSTANCES";
    public final static String DELETE_PARTNER_LINK_LDATA_BY_INSTANCES = "DELETE_PARTNER_LINK_LDATA_BY_INSTANCES";
    public final static String DELETE_FAULT_LDATA_BY_INSTANCE_IDS = "DELETE_FAULT_LDATA_BY_INSTANCE_IDS";

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
