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


import java.util.HashSet;
import java.util.Set;

/**
 * Hibernate table representing a BPEL correlator, or message-router. This class
 * maintains a set of <em>selectors</em> and <em>messages</em>. The
 * selector set is a database representation of the pick/receive activities that
 * need to be matched up to a message, while the message set is the database
 * representation of the messages that have been received that need to be
 * matched up to a pcik/receive.
 * 
 * @hibernate.class table="BPEL_CORRELATOR"
 * @hibernate.query name="SELECT_CORRELATOR_IDS_BY_PROCESS" query="select id from HCorrelator as c where c.process = :process"
 */
public class HCorrelator extends HObject {
	public final static String SELECT_CORRELATOR_IDS_BY_PROCESS = "SELECT_CORRELATOR_IDS_BY_PROCESS";
	
    private HProcess _process;

    private String _correlatorId;

    /** Receives/picks waiting for message. */
    private Set<HCorrelatorSelector> _selectors = new HashSet<HCorrelatorSelector>();

    /** Messages waiting for picks/receives. */
    private Set<HCorrelatorMessage> _messages = new HashSet<HCorrelatorMessage>();

    /** Constructor. */
    public HCorrelator() {
        super();
    }

    /**
     * Get the set of {@link HCorrelatorMessage} objects representing the
     * messages that need matching to a selector (i.e. pick/receive).
     * 
     * @hibernate.set lazy="true" inverse="true"
     * @hibernate.collection-key column="CORRELATOR" foreign-key="none"
     * @hibernate.collection-one-to-many class="org.apache.ode.daohib.bpel.hobj.HCorrelatorMessage"
     */
    public Set<HCorrelatorMessage> getMessageCorrelations() {
        return _messages;
    }

    /** Hibernate-mandated setter. */
    public void setMessageCorrelations(Set<HCorrelatorMessage> messages) {
        _messages = messages;
    }

    /**
     * @hibernate.property
     * @hibernate.column name="CID" index="IDX_CORRELATOR_CID"
     */
    public String getCorrelatorId() {
        return _correlatorId;
    }

    /** Hibernate-mandated setter. */
    public void setCorrelatorId(String correlatorId) {
        _correlatorId = correlatorId;
    }

    /**
     * @hibernate.many-to-one column="PROCESS_ID" foreign-key="none"
     */
    public HProcess getProcess() {
        return _process;
    }

    /** Hibernate-mandated setter. */
    public void setProcess(HProcess process) {
        _process = process;
    }

    /**
     * Get the set of {@link HCorrelatorSelector} objects representing the
     * selectors (i.e. pick/receive) that need matching to an input message.
     * 
     * @hibernate.set lazy="true" inverse="true"
     * @hibernate.collection-key column="CORRELATOR" foreign-key="none"
     * @hibernate.collection-one-to-many class="org.apache.ode.daohib.bpel.hobj.HCorrelatorSelector"
     */
    public Set<HCorrelatorSelector> getSelectors() {
        return _selectors;
    }

    /** Hibernate-mandated setter. */
    public void setSelectors(Set<HCorrelatorSelector> selectors) {
        _selectors = selectors;
    }

    @Override
    public String toString() {
        return "{HCorrelator process=" + _process.getProcessId() + ", cid=" + _correlatorId + "}";
    }
}
