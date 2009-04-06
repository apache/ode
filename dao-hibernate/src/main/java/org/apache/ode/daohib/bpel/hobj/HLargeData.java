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
 * @hibernate.query name="DELETE_EVENT_LDATA_BY_PROCESS" query="delete from HLargeData as d where d in(select e.data from HBpelEvent as e where e.process = :process)"
 * @hibernate.query name="DELETE_XMLDATA_LDATA_BY_PROCESS" query="delete from HLargeData as d where d in(select x.data from HXmlData as x where x.instance.process = :process)"
 * @hibernate.query name="DELETE_ACTIVITY_RECOVERY_LDATA_BY_PROCESS" query="delete from HLargeData as d where d in(select a.details from HActivityRecovery as a where a.instance.process = :process)"
 * @hibernate.query name="DELETE_FAULT_LDATA_BY_PROCESS" query="delete from HLargeData as d where d in(select f.data from HFaultData as f, HProcessInstance as i where f.id = i.fault and i.process = :process)"
 * @hibernate.query name="DELETE_JACOB_LDATA_BY_PROCESS" query="delete from HLargeData as d where d in(select i.jacobState from HProcessInstance as i where i.process = :process)"
 * @hibernate.query name="DELETE_PARTNER_LINK_LDATA_BY_PROCESS" query="delete from HLargeData as d where d in(select l.myEPR from HPartnerLink as l where l.scope.instance.process = :process) or d IN(select l.partnerEPR from HPartnerLink as l where l.scope.instance.process = :process2)"
 * @hibernate.query name="DELETE_MESSAGE_LDATA_BY_PROCESS" query="delete from HLargeData as d where d in(select m.messageData from HMessage m where m in(select request from HMessageExchange x where x.process = :process)) or d in(select m.header from HMessage m where m in(select request from HMessageExchange x where x.process = :process)) or d in(select m.messageData from HMessage m where m in(select response from HMessageExchange x where x.process = :process)) or d in(select m.header from HMessage m where m in(select response from HMessageExchange x where x.process = :process))"
 * @hibernate.query name="DELETE_MEX_LDATA_BY_PROCESS" query="delete from HLargeData as d where d in(select e.endpoint from HMessageExchange as e where e.process = :process) or d IN(select e.callbackEndpoint from HMessageExchange as e where e.process = :process2)"
 * 
 * @hibernate.query name="DELETE_EVENT_LDATA_BY_INSTANCE" query="delete from HLargeData as d where d in(select e.data from HBpelEvent as e where e.instance = :instance)"
 * @hibernate.query name="DELETE_MESSAGE_LDATA_BY_MEX" query="delete from HLargeData as d where d in(select m.messageData from HMessage m where m in(select request from HMessageExchange x where x = :mex)) or d in(select m.header from HMessage m where m in(select request from HMessageExchange x where x = :mex)) or d in(select m.messageData from HMessage m where m in(select response from HMessageExchange x where x = :mex)) or d in(select m.header from HMessage m where m in(select response from HMessageExchange x where x = :mex))"
 * @hibernate.query name="DELETE_UNMATCHED_MESSAGE_LDATA_BY_INSTANCE" query="delete from HLargeData as d where d in(select m.messageData from HMessage m, HMessageExchange x, HCorrelatorMessage cm where (m = x.request or m = x.response) and x = cm.messageExchange and x.instance = :instance) or d in(select m.header from HMessage m, HMessageExchange x, HCorrelatorMessage cm where (m = x.request or m = x.response) and x = cm.messageExchange and x.instance = :instance)"
 * @hibernate.query name="DELETE_XMLDATA_LDATA_BY_INSTANCE" query="delete from HLargeData as d where d in(select x.data from HXmlData as x where x.instance = :instance)"
 * @hibernate.query name="DELETE_PARTNER_LINK_LDATA_BY_INSTANCE" query="delete from HLargeData as d where d in(select l.myEPR from HPartnerLink as l where l.scope.instance = :instance) or d IN(select l.partnerEPR from HPartnerLink as l where l.scope.instance = :instance2)"
 * @hibernate.query name="DELETE_FAULT_LDATA_BY_INSTANCE_ID" query="delete from HLargeData as d where d in(select f.data from HFaultData as f, HProcessInstance as i where f.id = i.fault and i.id = :instanceId)"
 */
public class HLargeData extends HObject {
    public final static String DELETE_EVENT_LDATA_BY_PROCESS = "DELETE_EVENT_LDATA_BY_PROCESS";
    public final static String DELETE_XMLDATA_LDATA_BY_PROCESS = "DELETE_XMLDATA_LDATA_BY_PROCESS";
    public final static String DELETE_ACTIVITY_RECOVERY_LDATA_BY_PROCESS = "DELETE_ACTIVITY_RECOVERY_LDATA_BY_PROCESS";
    public final static String DELETE_FAULT_LDATA_BY_PROCESS = "DELETE_FAULT_LDATA_BY_PROCESS";
    public final static String DELETE_JACOB_LDATA_BY_PROCESS = "DELETE_JACOB_LDATA_BY_PROCESS";
    public final static String DELETE_PARTNER_LINK_LDATA_BY_PROCESS = "DELETE_PARTNER_LINK_LDATA_BY_PROCESS";
    public final static String DELETE_MESSAGE_LDATA_BY_PROCESS = "DELETE_MESSAGE_LDATA_BY_PROCESS";
    public final static String DELETE_MEX_LDATA_BY_PROCESS = "DELETE_MEX_LDATA_BY_PROCESS";

    public final static String DELETE_EVENT_LDATA_BY_INSTANCE = "DELETE_EVENT_LDATA_BY_INSTANCE";
    public final static String DELETE_MESSAGE_LDATA_BY_MEX = "DELETE_MESSAGE_LDATA_BY_MEX";
    public final static String DELETE_UNMATCHED_MESSAGE_LDATA_BY_INSTANCE = "DELETE_UNMATCHED_MESSAGE_LDATA_BY_INSTANCE";
    public final static String DELETE_XMLDATA_LDATA_BY_INSTANCE = "DELETE_XMLDATA_LDATA_BY_INSTANCE";
    public final static String DELETE_PARTNER_LINK_LDATA_BY_INSTANCE = "DELETE_PARTNER_LINK_LDATA_BY_INSTANCE";
    public final static String DELETE_FAULT_LDATA_BY_INSTANCE_ID = "DELETE_FAULT_LDATA_BY_INSTANCE_ID";

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
