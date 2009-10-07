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

import java.io.Serializable;

/**
 * This hibernate entity enables using the BPEL_MEX_PROPS table in HQL. This entity
 * is excluded from schema export.
 * 
 * @hibernate.class table="BPEL_MEX_PROPS" lazy="true"
 * @hibernate.query name="SELECT_MEX_PROPS_IDS_BY_INSTANCES" query="select p.id from HMessageExchangeProperty p, HMessageExchange e where p.mex = e and e.instance in (:instances)"
 */
@SuppressWarnings("serial")
public class HMessageExchangeProperty implements Serializable {
    public final static String SELECT_MEX_PROPS_IDS_BY_INSTANCES = "SELECT_MEX_PROPS_IDS_BY_INSTANCES";

	private HMessageExchange _mex;
	private String _name;
	private String _value;

	public HMessageExchangeProperty() {
	}

	/**
	 * @hibernate.id
	 * @return
	 */
	public HMessageExchangeProperty getKey() {
		return null;
	}

	public void setKey(HMessageExchangeProperty property) {
	}
	
    /**
     * @hibernate.many-to-one column="MEX" insert="false" update="false"
     */
	public HMessageExchange getMex() {
		return _mex;
	}

	public void setMex(HMessageExchange mex) {
		_mex = mex;
	}

	/**
	 * @hibernate.property column="NAME" type="string" length="8000" insert="false" update="false"
	 */
	public String getName() {
		return _name;
	}
	
	public void setName(String name) {
		_name = name;
	}

	/**
	 * @hibernate.property column="VALUE" type="string"  insert="false" update="false"
	 */
	public String getValue() {
		return _value;
	}

	public void setValue(String value) {
		_value = value;
	}

    public boolean equals(Object another) {
        // fake implementation to suppress hibernate warning on key not hash-searchable; 
        // actually HMessageExchangePropery is never retrieved by the id 
        return super.equals(another);
    }
    
    public int hashCode() {
        // fake implementation to suppress hibernate warning on key not hash-searchable
        // actually HMessageExchangePropery is never retrieved by the id 
        return _mex.hashCode() * 29 + _name.hashCode() * 13;
    }
}
