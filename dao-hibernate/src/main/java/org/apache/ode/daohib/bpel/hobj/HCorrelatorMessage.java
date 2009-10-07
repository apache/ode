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
 * @hibernate.class table="BPEL_UNMATCHED" lazy="true"
 * @hibernate.query name="SELECT_CORMESSAGE_IDS_BY_INSTANCES" query="select id from HCorrelatorMessage as m where m.messageExchange in(select mex from HMessageExchange as mex where mex.instance in (:instances))"
 * @hibernate.query name="SELECT_CORMESSAGE_IDS_BY_MEX" query="select id from HCorrelatorMessage as m where m.messageExchange = :mex"
 */
public class HCorrelatorMessage extends HObject {
    public final static String SELECT_CORMESSAGE_IDS_BY_MEX = "SELECT_CORMESSAGE_IDS_BY_MEX";
    public final static String SELECT_CORMESSAGE_IDS_BY_INSTANCES = "SELECT_CORMESSAGE_IDS_BY_INSTANCES";

    private HMessageExchange _messageExchange;
    private HCorrelator _correlator;
    private String _correlationKey;

    /**
     * @hibernate.many-to-one column="MEX" foreign-key="none"
     */
    public HMessageExchange getMessageExchange() {
        return _messageExchange;
    }

    public void setMessageExchange(HMessageExchange data) {
        _messageExchange = data;
    }

    @Override
    public String toString() {
        return "{HCorrelatorMessage correlator=" + this.getCorrelator() + ", ckey=" + getCorrelationKey() + ", mex="
                + _messageExchange.getId() + "}";
    }

    /**
     * @hibernate.property column="CORRELATION_KEY"
     * @hibernate.column name="CORRELATION_KEY"
     *                   index="IDX_UNMATCHED_CKEY"
     *                   
     */
    public String getCorrelationKey() {
        return _correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        _correlationKey = correlationKey;
    }

    /**
     * @hibernate.many-to-one foreign-key="none"
     * @hibernate.column name="CORRELATOR" index="IDX_UNMATCHED_CORRELATOR" not-null="true"
     */
    public HCorrelator getCorrelator() {
        return _correlator;
    }

    public void setCorrelator(HCorrelator correlator) {
        _correlator = correlator;
    }
}
