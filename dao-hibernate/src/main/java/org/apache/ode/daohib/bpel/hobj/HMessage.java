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
 * @hibernate.query name="SELECT_MESSAGE_IDS_BY_MEX_1" query="select mex.request.id from HMessageExchange mex WHERE mex = :messageExchange and mex.request is not null"
 * @hibernate.query name="SELECT_MESSAGE_IDS_BY_MEX_2" query="select mex.response.id from HMessageExchange mex WHERE mex = :messageExchange and mex.response is not null"
 * @hibernate.query name="SELECT_MESSAGE_IDS_BY_INSTANCES_1" query="select mex.request.id from HMessageExchange mex WHERE mex.instance in (:instances) and mex.request.id is not null"
 * @hibernate.query name="SELECT_MESSAGE_IDS_BY_INSTANCES_2" query="select mex.response.id from HMessageExchange mex WHERE mex.instance in (:instances) and mex.response.id is not null"
 */
public class HMessage extends HObject {
    public final static String SELECT_MESSAGE_IDS_BY_MEX_1 = "SELECT_MESSAGE_IDS_BY_MEX_1";
    public final static String SELECT_MESSAGE_IDS_BY_MEX_2 = "SELECT_MESSAGE_IDS_BY_MEX_2";
    public final static String SELECT_MESSAGE_IDS_BY_INSTANCES_1 = "SELECT_MESSAGE_IDS_BY_INSTANCES_1";
    public final static String SELECT_MESSAGE_IDS_BY_INSTANCES_2 = "SELECT_MESSAGE_IDS_BY_INSTANCES_2";

    private String _type;
    private HLargeData _data;
    private HLargeData _header;

    public void setType(String type) {
        _type = type;
    }

    /** @hibernate.property column="TYPE" */
    public String getType() {
        return _type;
    }

    /** @hibernate.many-to-one column="DATA" lazy="false" outer-join="true" foreign-key="none" */
    public HLargeData getMessageData() {
        return _data;
    }

    public void setMessageData(HLargeData data) {
        _data = data;
    }

    /** @hibernate.many-to-one column="HEADER" foreign-key="none" */
    public HLargeData getHeader() {
        return _header;
    }

    public void setHeader(HLargeData header) {
        _header = header;
    }
}
