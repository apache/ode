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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name="ODE_CORRELATION_SET")
public class CorrelationSetDAOImpl implements CorrelationSetDAO {

	@Id @Column(name="CORRELATION_SET_ID") 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long _correlationSetId;
	@Basic @Column(name="NAME")
    private String _name;
	@Basic @Column(name="CORRELATION_KEY")
    private String _correlationKey;

    @OneToMany(targetEntity=CorrSetProperty.class,mappedBy="_corrSet",fetch=FetchType.LAZY,cascade={CascadeType.ALL})
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
	    for (int m = 0; m < names.length; m++) {
            CorrSetProperty prop = new CorrSetProperty(names[m].toString(), values.getValues()[m]);
            _props.add(prop);
            prop.setCorrSet(this);
        }
	}
}
