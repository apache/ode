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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.ScopeDAO;

@Entity
@Table(name="ODE_CORRELATION_SET")
public class CorrelationSetDAOImpl implements CorrelationSetDAO {

	@Id @Column(name="CORRELATION_SET_ID") private Long _correlationSetId;
	@Basic @Column(name="NAME") private String _name;
	@Basic @Column(name="PROPERTIES") private HashMap<QName,String> _props = new HashMap<QName,String>();
	@Basic @Column(name="CORRELATION_KEY") private CorrelationKey _correlationKey;
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST})
	@Column(name="SCOPE_ID")
	private ScopeDAOImpl _scope;
	@Version @Column(name="VERSION") private long _version;
	
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
		return _props;
	}

	public ScopeDAO getScope() {
		return _scope;
	}

	public CorrelationKey getValue() {
		return _correlationKey;
	}

	public void setValue(QName[] names, CorrelationKey values) {
		_correlationKey = values;
	    for (int m = 0; m < names.length; m++) {
	    	_props.put(names[m], values.getValues()[m]);
        }
	}

}
