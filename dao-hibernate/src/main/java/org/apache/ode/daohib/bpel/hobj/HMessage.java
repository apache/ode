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
 * Hibernate-managed table for keeping track of messages.
 *
 * @hibernate.class table="BPEL_MESSAGE"
 * @hibernate.query name="SELECT_MESSAGE_IDS_BY_MEX" query="select id from HMessage as m WHERE m.messageExchange = :messageExchange"
 * @hibernate.query name="SELECT_MESSAGE_IDS_BY_INSTANCES" query="select m.id from HMessage m, HMessageExchange mex WHERE m.messageExchange = mex and mex.instance in (:instances)"
 * @hibernate.query name="SELECT_MESSAGES_BY_INSTANCES" query="select m from HMessage m, HMessageExchange mex WHERE m.messageExchange = :mex"
 */
public class HMessage extends HObject {
    public final static String SELECT_MESSAGE_IDS_BY_MEX = "SELECT_MESSAGE_IDS_BY_MEX";
    public final static String SELECT_MESSAGE_IDS_BY_INSTANCES = "SELECT_MESSAGE_IDS_BY_INSTANCES";
    public final static String SELECT_MESSAGES_BY_INSTANCES = "SELECT_MESSAGES_BY_INSTANCES";

    private HMessageExchange _mex;
    private String _type;
    private byte[] _data;
    private byte[] _header;
    
    public void setMessageExchange(HMessageExchange mex) {
        _mex = mex;
    }
    
    /** @hibernate.many-to-one column="MEX" foreign-key="none"*/
    public HMessageExchange getMessageExchange() {
        return _mex;
    }

    public void setType(String type) {
        _type = type;
    }

    /** @hibernate.property column="TYPE" */
    public String getType() {
        return _type;
    }

    /**
     * @hibernate.property type="org.apache.ode.daohib.bpel.hobj.GZipDataType"
     *
     * @hibernate.column name="MESSAGE_DATA" sql-type="blob(2G)"
     */
    public byte[] getMessageData() {
        return _data;
    }

    public void setMessageData(byte[] data) {
        _data = data;
    }

    /**
     * @hibernate.property type="org.apache.ode.daohib.bpel.hobj.GZipDataType"
     *
     * @hibernate.column name="MESSAGE_HEADER" sql-type="blob(2G)"
     */
    public byte[] getHeader() {
        return _header;
    }

    public void setHeader(byte[] header) {
        _header = header;
    }

}
