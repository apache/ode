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

package org.apache.ode.dao.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ScopeStateEnum;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.bpel.evt.BpelEvent;

@Entity
@Table(name="ODE_SCOPE")
public class ScopeDAOImpl implements ScopeDAO {
	
	@Id @Column(name="SCOPE_ID") private Long _scopeInstanceId;
	@Basic @Column(name="MODEL_ID") private int _modelId;
	@Basic @Column(name="SCOPE_NAME") private String _name;
	@Basic @Column(name="SCOPE_STATE") private String _scopeState;
	@Version @Column(name="VERSION") private long _version;
	
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST})
	@Column(name="PARENT_SCOPE_ID")
	private ScopeDAOImpl _parentScope;
	
	@OneToMany(targetEntity=ScopeDAOImpl.class,mappedBy="_parentScope",fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	private Collection<ScopeDAO> _childScopes = new ArrayList<ScopeDAO>();
	@OneToMany(targetEntity=CorrelationSetDAOImpl.class,mappedBy="_scope",fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	private Collection<CorrelationSetDAO> _correlationSets = new ArrayList<CorrelationSetDAO>();
	@OneToMany(targetEntity=PartnerLinkDAOImpl.class,fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	private Collection<PartnerLinkDAO> _partnerLinks = new ArrayList<PartnerLinkDAO>();
	@OneToMany(targetEntity=XmlDataDAOImpl.class,mappedBy="_scope",fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	private Collection<XmlDataDAO> _variables = new ArrayList<XmlDataDAO>();
	
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST})
	@Column(name="PROCESS_INSTANCE_ID")
	private ProcessInstanceDAOImpl _processInstance;
	
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST})
	@Column(name="CONNECTION_ID")
	private BPELDAOConnectionImpl _connection;

	public ScopeDAOImpl() {}
	public ScopeDAOImpl(ScopeDAOImpl parentScope, String name, int scopeModelId, BPELDAOConnectionImpl connection) {
		_parentScope = parentScope;
		_name = name;
		_modelId = scopeModelId;
		_connection = connection;
		_connection.addScope(this);
	}
	
	public PartnerLinkDAO createPartnerLink(int plinkModelId, String pLinkName,
			String myRole, String partnerRole) {
		PartnerLinkDAO pl = new PartnerLinkDAOImpl(plinkModelId, pLinkName, myRole, partnerRole);
		
		_partnerLinks.add(pl);
		return pl;
	}

	public Collection<ScopeDAO> getChildScopes() {
		return _childScopes;
	}

	public CorrelationSetDAO getCorrelationSet(String corrSetName) {
		CorrelationSetDAO ret = null;
		for (CorrelationSetDAO csElement : _correlationSets) {
			if ( csElement.getName().equals(corrSetName)) ret = csElement;
		}
		
		if ( ret == null ) {
			// Apparently the caller knows there should be a correlation set
			// in here. Create a new set if one does not exist.
			// Not sure I understand this implied object creation and why
			// an explicit create pattern isn't used ( i.e. similar to
			// PartnerLink creation )
			ret = new CorrelationSetDAOImpl(this,corrSetName);
			_correlationSets.add(ret);
		}
		
		return ret;
	}

	public Collection<CorrelationSetDAO> getCorrelationSets() {
		return _correlationSets;
	}

	public int getModelId() {
		return _modelId;
	}

	public String getName() {
		return _name;
	}

	public ScopeDAO getParentScope() {
		return _parentScope;
	}

	public PartnerLinkDAO getPartnerLink(int plinkModelId) {
		for (PartnerLinkDAO plElement : _partnerLinks) {
			if ( plElement.getPartnerLinkModelId() == plinkModelId) return plElement;
		}
		return null;
	}

	public Collection<PartnerLinkDAO> getPartnerLinks() {
		return _partnerLinks;
	}

	public ProcessInstanceDAO getProcessInstance() {
		return _processInstance;
	}

	public Long getScopeInstanceId() {
		return _scopeInstanceId;
	}

	public ScopeStateEnum getState() {
		return new ScopeStateEnum(_scopeState);
	}

	public XmlDataDAO getVariable(String varName) {
		for (XmlDataDAO xmlElement : _variables) {
			if ( xmlElement.getName().equals(varName)) return xmlElement;
		}
		return null;
	}

	public Collection<XmlDataDAO> getVariables() {
		return _variables;
	}

	public List<BpelEvent> listEvents(BpelEventFilter efilter) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setState(ScopeStateEnum state) {
		_scopeState = state.toString();
	}

}
