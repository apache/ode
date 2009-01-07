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

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;

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
import javax.persistence.Table;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name="ODE_CORRELATION_SET")
@NamedQueries({
    @NamedQuery(name=CorrelationSetDAOImpl.DELETE_CORRELATION_SETS_BY_IDS, query="delete from CorrelationSetDAOImpl as c where c._correlationSetId in (:ids)"),
    @NamedQuery(name=CorrelationSetDAOImpl.SELECT_CORRELATION_SETS_BY_INSTANCES, query="select c from CorrelationSetDAOImpl as c left join fetch c._scope left join fetch c._props where c._scope._processInstance._instanceId in (:instances)"),
    @NamedQuery(name=CorrelationSetDAOImpl.SELECT_CORRELATION_SET_IDS_BY_PROCESS, query="select c._correlationSetId from CorrelationSetDAOImpl as c where c._scope._processInstance._process = :process"),
    @NamedQuery(name=CorrelationSetDAOImpl.SELECT_CORRELATION_SET_IDS_BY_INSTANCE, query="select c._correlationSetId from CorrelationSetDAOImpl as c where c._scope._processInstance = :instance"),
    @NamedQuery(name=CorrelationSetDAOImpl.SELECT_ACTIVE_SETS, query="select c from CorrelationSetDAOImpl as c left join fetch c._scope where c._scope._processInstance._state = (:state)")
})
public class CorrelationSetDAOImpl implements CorrelationSetDAO {
	public final static String DELETE_CORRELATION_SETS_BY_IDS = "DELETE_CORRELATION_SETS_BY_IDS";
    public final static String SELECT_CORRELATION_SETS_BY_INSTANCES = "SELECT_CORRELATION_SETS_BY_INSTANCES";
    public final static String SELECT_CORRELATION_SET_IDS_BY_PROCESS = "SELECT_CORRELATION_SET_IDS_BY_PROCESS";
    public final static String SELECT_CORRELATION_SET_IDS_BY_INSTANCE = "SELECT_CORRELATION_SET_IDS_BY_INSTANCE";
    public final static String SELECT_ACTIVE_SETS = "SELECT_ACTIVE_SETS";
	
	@Id @Column(name="CORRELATION_SET_ID") 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long _correlationSetId;
	@Basic @Column(name="NAME")
    private String _name;
	@Basic @Column(name="CORRELATION_KEY")
    private String _correlationKey;

    @OneToMany(targetEntity=CorrSetProperty.class,mappedBy="_corrSet",fetch=FetchType.LAZY,cascade={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Collection<CorrSetProperty> _props = new ArrayList<CorrSetProperty>();
    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="SCOPE_ID")
    private ScopeDAOImpl _scope;

    public CorrelationSetDAOImpl() {}
	public CorrelationSetDAOImpl(ScopeDAOImpl scope, String name) {
		_name = name;
		_scope = scope;
	}
	
	public Long getCorrelationSetId() {
		return _correlationSetId;
	}

	public String getName() {
		return _name;
	}

	public Map<QName, String> getProperties() {
        HashMap<QName, String> map = new HashMap<QName, String>();
        for (CorrSetProperty prop : _props) {
            map.put(QName.valueOf(prop.getPropertyKey()), prop.getPropertyValue());
        }
        return map;
	}

	public ScopeDAO getScope() {
		return _scope;
	}

	public CorrelationKey getValue() {
        if (_correlationKey == null) return null;
        return new CorrelationKey(_correlationKey);
	}

	public void setValue(QName[] names, CorrelationKey values) {
		_correlationKey = values.toCanonicalString();
        if (names != null)
            for (int m = 0; m < names.length; m++) {
                CorrSetProperty prop = new CorrSetProperty(names[m].toString(), values.getValues()[m]);
                _props.add(prop);
                prop.setCorrSet(this);
            }
	}

    public ProcessDAO getProcess() {
        return _scope.getProcessInstance().getProcess();
    }
    public ProcessInstanceDAO getInstance() {
        return _scope.getProcessInstance();
    }
}
