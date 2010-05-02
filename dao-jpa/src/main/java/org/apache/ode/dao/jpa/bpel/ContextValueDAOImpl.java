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
package org.apache.ode.dao.jpa.bpel;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.ode.dao.bpel.ContextValueDAO;

@Entity
@Table(name="ODE_CONTEXT_VALUE")
@NamedQueries({
    @NamedQuery(name=ContextValueDAOImpl.DELETE_CONTEXT_VALUES_BY_KEYS, query="delete from ContextValueDAOImpl as l where l._key = :key and l._namespace = :namespace")
})

public class ContextValueDAOImpl extends BpelDAO implements ContextValueDAO, Serializable {
	public static final String DELETE_CONTEXT_VALUES_BY_KEYS = "DELETE_CONTEXT_VALUES_BY_KEYS";
	
    @Id @Column(name="CONTEXT_VALUE_ID") 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @SuppressWarnings("unused")
    private Long _id;

    @Basic @Column(name="NAMESPACE")
    //TODO: can we move this specific annotation into XML property??
    //@Index(name="IDX_CTX_NS", enabled=true, unique=false)
    private String _namespace;

    @Basic @Column(name="KEY_NAME")
    //TODO: can we move this specific annotation into XML property??
    //@Index(name="IDX_CTX_KEY", enabled=true, unique=false)
    private String _key;

    @Lob @Column(name="DATA")
    private String _data;
    
    @Basic @Column(name="VALUE")
    //TODO: can we move this specific annotation into XML property??
    //@Index(name="IDX_CTX_VAL", enabled=true, unique=false)
    private String _value;
    
//    @Basic @Column(name="PARTNER_LINK_ID", nullable=true, insertable=false, updatable=false)
//    @SuppressWarnings("unused")
//    private Long _partnerLinkId;

    @SuppressWarnings("unused")
    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) 
    @JoinColumn(name="PLINK")
    private PartnerLinkDAOImpl _partnerLink;


    public ContextValueDAOImpl() {}
    public ContextValueDAOImpl(PartnerLinkDAOImpl partnerLink, String namespace, String key){
        _partnerLink = partnerLink;
        _namespace = namespace;
        _key = key;
    }

    public String getKey() {
        return _key;
    }

    public String getValue() {
        if (_value != null) {
            return _value;
        }
        
        if (_data != null) {
            return _data;
        }
        
        return null;
    }

    public void setValue(String value) {
        // store large data in the clob, small data indexable in a varchar
        if (value.length() <= 250) {
            _value = value;
            _data = null;
        } else {
            _value = null;
            _data = value;
        }
    }

	public String getNamespace() {
		return _namespace;
	}

	public void setKey(String key) {
		_key = key;
	}

	public void setNamespace(String namespace) {
		_namespace = namespace;
	}
}
