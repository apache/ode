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
 * Hibernate table representing a BPEL scope instance.
 * 
 * @hibernate.class table="BPEL_SCOPE"
 * @hibernate.query name="SELECT_SCOPE_IDS_BY_INSTANCES" query="select id from HScope as s where s.instance in (:instances)"
 */
public class HScope extends HObject{
    public final static String SELECT_SCOPE_IDS_BY_INSTANCES = "SELECT_SCOPE_IDS_BY_INSTANCES";

    /** Process instance to which this scope belongs. */
    private HProcessInstance _instance;

    /** Correlation set values for csets declared in this scope. */
    private Set<HCorrelationSet> _correlationSets = new HashSet<HCorrelationSet>();

    /** Variable values for variables declared in this scope. */
    private Set<HXmlData> _variables = new HashSet<HXmlData>();

    /** Enpoint References for partner links declared in this scope */
    private Set<HPartnerLink> _partnerLinks = new HashSet<HPartnerLink>();

    /** Parent scope for this scope. */
    private HScope _parentScope;

    /** State of the scope. */
    private String _state;

    /** Scope type / name. */
    private String _name;

    private int _scopeModelId;

    public HScope() {
    }

    /**
     * @hibernate.set lazy="true" inverse="true" cascade="delete"
     * @hibernate.collection-key column="SCOPE_ID" foreign-key="none"
     * @hibernate.collection-one-to-many class="org.apache.ode.daohib.bpel.hobj.HCorrelationSet"
     */
    public Set<HCorrelationSet> getCorrelationSets() {
        return _correlationSets;
    }

    public void setCorrelationSets(Set<HCorrelationSet> correlationSets) {
        _correlationSets = correlationSets;
    }

    /**
     * Get the {@link HProcessInstance} to which this scope object belongs.
     * 
     * @hibernate.many-to-one column="PIID" foreign-key="none"
     */
    public HProcessInstance getInstance() {
        return _instance;
    }

    /** @see #getInstance() */
    public void setInstance(HProcessInstance instance) {
        _instance = instance;
    }

    /**
     * Get the "parent" {@link HScope} of this scope.
     * 
     * @hibernate.many-to-one column="PARENT_SCOPE_ID" foreign-key="none"
     */
    public HScope getParentScope() {
        return _parentScope;
    }

    /** @see #getParentScope() */
    public void setParentScope(HScope parentScope) {
        _parentScope = parentScope;
    }

    /**
     * @hibernate.property column="STATE" not-null="true"
     */
    public String getState() {
        return _state;
    }

    /** @see #getState() */
    public void setState(String state) {
        _state = state;
    }

    /**
     * Get the type (i.e. the name) of the scope.
     * 
     * @hibernate.property column="NAME"
     */
    public String getName() {
        return _name;
    }

    /** @see #getName() */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Get the variable values associated with this scope.
     * 
     * @return {@link Set}&lt;{@link HXmlData}&gt; with variable values
     * @hibernate.set lazy="true" inverse="true" cascade="delete"
     * @hibernate.collection-key column="SCOPE_ID" foreign-key="none"
     * @hibernate.collection-one-to-many class="org.apache.ode.daohib.bpel.hobj.HXmlData"
     */
    public Set<HXmlData> getVariables() {
        return _variables;
    }

    public void setVariables(Set<HXmlData> variables) {
        _variables = variables;
    }

    /**
     * Get the endpoint references for partner links roles associated with this
     * scope.
     * 
     * @return {@link Set}&lt;{@link HPartnerLink}&gt; with variable values
     * @hibernate.set lazy="true" inverse="true" cascade="delete"
     * @hibernate.collection-key column="SCOPE" foreign-key="none"
     * @hibernate.collection-one-to-many class="org.apache.ode.daohib.bpel.hobj.HPartnerLink"
     */
    public Set<HPartnerLink> getPartnerLinks() {
        return _partnerLinks;
    }

    public void setPartnerLinks(Set<HPartnerLink> eprs) {
        _partnerLinks = eprs;
    }

    /**
     * @hibernate.property column="MODELID"
     */
    public int getScopeModelId() {
        return _scopeModelId;
    }

    public void setScopeModelId(int scopeModelId) {
        _scopeModelId = scopeModelId;
    }

    public String toString() {
        return "HScope{id=" + getId() + ",name=" + _name + "}";
    }
}
