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

import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ScopeStateEnum;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.bpel.evt.BpelEvent;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name="ODE_SCOPE")
@NamedQueries({
    @NamedQuery(name="ScopeEvents", query="SELECT se FROM EventDAOImpl as se WHERE se._scopeId = :sid"),
    @NamedQuery(name=ScopeDAOImpl.SELECT_SCOPE_IDS_BY_PROCESS, query="select s._scopeInstanceId from ScopeDAOImpl as s where s._processInstance._process = :process"),
    @NamedQuery(name=ScopeDAOImpl.SELECT_SCOPE_IDS_BY_INSTANCE, query="select s._scopeInstanceId from ScopeDAOImpl as s where s._processInstance = :instance"),
    @NamedQuery(name=ScopeDAOImpl.DELETE_SCOPES_BY_SCOPE_IDS, query="delete from ScopeDAOImpl as s where s._scopeInstanceId in(:ids)")
})
public class ScopeDAOImpl extends OpenJPADAO implements ScopeDAO {
	public final static String SELECT_SCOPE_IDS_BY_PROCESS = "SELECT_SCOPE_IDS_BY_PROCESS";
	public final static String SELECT_SCOPE_IDS_BY_INSTANCE = "SELECT_SCOPE_IDS_BY_INSTANCE";
	public final static String DELETE_SCOPES_BY_SCOPE_IDS = "DELETE_SCOPES_BY_SCOPE_IDS";
	
    @Id @Column(name="SCOPE_ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
	private Long _scopeInstanceId;
    
	@Basic @Column(name="MODEL_ID")
    private int _modelId;
	@Basic @Column(name="SCOPE_NAME")
    private String _name;
	@Basic @Column(name="SCOPE_STATE")
    private String _scopeState;

	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST})
	@Column(name="PARENT_SCOPE_ID")
	private ScopeDAOImpl _parentScope;
	
	@OneToMany(targetEntity=ScopeDAOImpl.class,mappedBy="_parentScope",fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	private Collection<ScopeDAO> _childScopes = new ArrayList<ScopeDAO>();
	@OneToMany(targetEntity=CorrelationSetDAOImpl.class,mappedBy="_scope",fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	private Collection<CorrelationSetDAO> _correlationSets = new ArrayList<CorrelationSetDAO>();
	@OneToMany(targetEntity=PartnerLinkDAOImpl.class,mappedBy="_scope",fetch= FetchType.LAZY,cascade={CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
    private Collection<PartnerLinkDAO> _partnerLinks = new ArrayList<PartnerLinkDAO>();
	@OneToMany(targetEntity=XmlDataDAOImpl.class,mappedBy="_scope",fetch=FetchType.LAZY,cascade={CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
	private Collection<XmlDataDAO> _variables = new ArrayList<XmlDataDAO>();
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PROCESS_INSTANCE_ID")
	private ProcessInstanceDAOImpl _processInstance;

	public ScopeDAOImpl() {}
	public ScopeDAOImpl(ScopeDAOImpl parentScope, String name, int scopeModelId, ProcessInstanceDAOImpl pi) {
		_parentScope = parentScope;
		_name = name;
		_modelId = scopeModelId;
		_processInstance = pi;
    }
	
	public PartnerLinkDAO createPartnerLink(int plinkModelId, String pLinkName,
			String myRole, String partnerRole) {
		PartnerLinkDAOImpl pl = new PartnerLinkDAOImpl(plinkModelId, pLinkName, myRole, partnerRole);
        pl.setScope(this);
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
			// Persist the new correlation set to generate an ID
			getEM().persist(ret);
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
        for (PartnerLinkDAO pLink : getPartnerLinks()) {
            if (pLink.getPartnerLinkModelId() == plinkModelId) {
                return pLink;
            }
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
		return ScopeStateEnum.valueOf(_scopeState);
	}

	public XmlDataDAO getVariable(String varName) {
		XmlDataDAO ret = null;
		
		for (XmlDataDAO xmlElement : _variables) {
			if ( xmlElement.getName().equals(varName)) return xmlElement;
		}
		
		ret = new XmlDataDAOImpl(this,varName);
		_variables.add(ret);
		
		return ret;
	}

	public Collection<XmlDataDAO> getVariables() {
		return _variables;
	}

	public List<BpelEvent> listEvents() {
        List<BpelEvent> result = new ArrayList<BpelEvent>();
        Query qry = getEM().createNamedQuery("ScopeEvents");
        qry.setParameter("sid", _scopeInstanceId);
        for (Object eventDao : qry.getResultList()) {
            result.add(((EventDAOImpl)eventDao).getEvent());
        }
        return result;
    }

	public void setState(ScopeStateEnum state) {
		_scopeState = state.toString();
	}

}
