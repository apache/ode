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
import java.util.HashSet;

/**
 * @hibernate.class table="BPEL_XML_DATA"
 * @hibernate.query name="SELECT_XMLDATA_IDS_BY_INSTANCES" query="select id from HXmlData as x where x.instance in (:instances)"
 */
public class HXmlData extends HObject{
    public static final String SELECT_XMLDATA_IDS_BY_INSTANCES = "SELECT_XMLDATA_IDS_BY_INSTANCES";

    private boolean _simpleType;
    private HLargeData _data;
    private Collection<HVariableProperty> _properties = new HashSet<HVariableProperty>();
    private String _name;
    private HScope _scope;
    private HProcessInstance _instance;

    /** Constructor. */
    public HXmlData() {
        super();
    }

    /**
     * @hibernate.many-to-one column="LDATA_ID" cascade="delete" foreign-key="none"
     */
    public HLargeData getData() {
        return _data;
    }

    public void setData(HLargeData data) {
        _data = data;
    }

    /**
     * @hibernate.property column="NAME" type="string" length="255"
     *                     not-null="true"
     */
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    /**
     * @hibernate.bag lazy="true" inverse="true" cascade="delete"
     * @hibernate.collection-key column="XML_DATA_ID" foreign-key="none"
     * @hibernate.collection-one-to-many class="org.apache.ode.daohib.bpel.hobj.HVariableProperty"
     */
    public Collection<HVariableProperty> getProperties() {
        return _properties;
    }

    public void setProperties(Collection<HVariableProperty> properties) {
        _properties = properties;
    }

    /**
     * @hibernate.many-to-one column="SCOPE_ID" foreign-key="none"
     */
    public HScope getScope() {
        return _scope;
    }

    public void setScope(HScope scope) {
        _scope = scope;

        if (scope != null) {
            setInstance(scope.getInstance());
        }
    }

    /**
     * @hibernate.many-to-one column="PIID" foreign-key="none"
     */
    public HProcessInstance getInstance() {
        return _instance;
    }

    public void setInstance(HProcessInstance instance) {
        _instance = instance;
    }

    /**
     * @hibernate.property column="IS_SIMPLE_TYPE"
     */
    public boolean isSimpleType() {
        return _simpleType;
    }

    public void setSimpleType(boolean simpleType) {
        _simpleType = simpleType;
    }

}
