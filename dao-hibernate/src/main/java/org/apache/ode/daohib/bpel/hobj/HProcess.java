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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Hibernate table representing a BPEL process (<em>not</em> a process instance).
 * @hibernate.class table="BPEL_PROCESS" lazy="false"
 *
 */
public class HProcess extends HObject{

    /** {@link HCorrelator}s for this process. */
    private Set<HCorrelator> _correlators = new HashSet<HCorrelator>();

    /** Instances of this BPEL process. */
    private Collection<HProcessInstance> _instances = new HashSet<HProcessInstance>();

    /** Events belonging to this BPEL process. */
    private Collection<HBpelEvent> _events = new HashSet<HBpelEvent>();

    /** Partnerlinks defined on this process */
    private Set<HPartnerLink> _plinks = new HashSet<HPartnerLink>();

    /** Message exchanges associated with this process. */
    private Set<HMessageExchange> _messageExchanges = new HashSet<HMessageExchange>();

    /** Simple name of the process. */
    private String _processId;

    /** User that deployed the process. */
    private String _deployer;

    /** Date of last deployment. */
    private Date _deployDate;

    /** Process name. */
    private String _typeName;

    /** Process namespace. */
    private String _typeNamespace;

    /** Process version. */
    private long _version;

    /** Whether process is retired */
    private boolean _retired;

    private boolean _active;

    private String _guid;

    /**
     * @hibernate.set
     *  lazy="true"
     *  inverse="true"
     * @hibernate.collection-key
     *  column="PROCESS" foreign-key="none"
     * @hibernate.collection-one-to-many
     *   class="org.apache.ode.daohib.bpel.hobj.HMessageExchange"
     */
    public Set<HMessageExchange> getMessageExchanges() {
        return _messageExchanges;
    }

    public void setMessageExchanges(Set<HMessageExchange> exchanges) {
        _messageExchanges = exchanges;
    }

    /**
     * @hibernate.set
     *  lazy="true"
     *  inverse="true"
     *  cascade="delete"
     * @hibernate.collection-key
     *  column="PROCESS_ID" foreign-key="none"
     * @hibernate.collection-one-to-many
     *   class="org.apache.ode.daohib.bpel.hobj.HCorrelator"
     */
    public Set<HCorrelator> getCorrelators() {
        return _correlators;
    }

    public void setCorrelators(Set<HCorrelator> correlators) {
        _correlators = correlators;
    }

    /**
     * @hibernate.bag
     *  lazy="true"
     *  inverse="true"
     * @hibernate.collection-key
     *  column="PROCESS_ID" foreign-key="none"
     * @hibernate.collection-one-to-many
     *  class="org.apache.ode.daohib.bpel.hobj.HProcessInstance"
     */
    public Collection<HProcessInstance> getInstances() {
        return _instances;
    }

    public void setInstances(Collection<HProcessInstance> instances) {
        _instances = instances;
    }

    /**
     * @hibernate.bag
     *  lazy="true"
     *  inverse="true"
     * @hibernate.collection-key
     *  column="PID" foreign-key="none"
     * @hibernate.collection-one-to-many
     *  class="org.apache.ode.daohib.bpel.hobj.HBpelEvent"
     */
    public Collection<HBpelEvent> getEvents() {
        return _events;
    }

    public void setEvents(Collection<HBpelEvent> events) {
        _events = events;
    }

    /**
     * Get the partner links values as deployed.
     *
     * @return {@link Set}&lt;{@link HPartnerLink}&gt; with variable values
     * @hibernate.set lazy="true" inverse="true"
     * @hibernate.collection-key column="PROCESS" foreign-key="none"
     * @hibernate.collection-one-to-many class="org.apache.ode.daohib.bpel.hobj.HPartnerLink"
     */
    public Set<HPartnerLink> getDeploymentPartnerLinks() {
        return _plinks;
    }

    /**
     * Set the partner links as deployed.
     * @param partnerlinks
     */
    public void setDeploymentPartnerLinks(Set<HPartnerLink> partnerlinks) {
        _plinks = partnerlinks;
    }


    /**
     *
     * @hibernate.property
     * @hibernate.column
     *  name="PROCID"
     *  not-null="true"
     *  unique="true"
     */
    public String getProcessId() {
        return _processId;
    }

    public void setProcessId(String processId) {
        _processId = processId;
    }

    /**
     * The user that deployed the process.
     * @hibernate.property
     *    column="deployer"
     */
    public String getDeployer() {
        return _deployer;
    }

    public void setDeployer(String deployer) {
        _deployer = deployer;
    }



    /**
     * The date the process was deployed.
     * @hibernate.property
     *    column="deploydate"
     */
    public Date getDeployDate() {
        return _deployDate;
    }

    public void setDeployDate(Date deployDate) {
        _deployDate = deployDate;
    }

    /**
     * The type of the process (BPEL process definition name).
     * @hibernate.property
     *     column="type_name"
     */
    public String getTypeName() {
        return _typeName;
    }

    public void setTypeName(String processName) {
        _typeName = processName;
    }

    /**
     * The type of the process (BPEL process definition name).
     * @hibernate.property
     *     column="type_ns"
     */
    public String getTypeNamespace() {
        return _typeNamespace;
    }

    public void setTypeNamespace(String processName) {
        _typeNamespace = processName;
    }

    /**
     * The process version.
     * @hibernate.property
     *    column="version"
     */
    public long getVersion() {
        return _version;
    }

    public void setVersion(long version) {
        _version = version;
    }

    /**
     * The process status.
     * @hibernate.property
     *    column="ACTIVE_"
     */
    public boolean isActive() {
        return _active;
    }

    public void setActive(boolean active) {
        _active = active;
    }

    /**
     * @hibernate.property
     */
    public String getGuid() {
        return _guid;
    }

    public void setGuid(String guid) {
        _guid = guid;
    }
}
