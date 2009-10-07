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

/**
 * Efficient storage of properties (bpel properties). Useful for identification
 * of process instances based on indexed lookup of property values.
 * 
 * @hibernate.class table="VAR_PROPERTY"
 * @hibernate.query name="SELECT_VARIABLE_PROPERTY_IDS_BY_INSTANCES" query="select id from HVariableProperty as p where p.xmlData in(select x.id from HXmlData x where x.instance in (:instances))"
 */
public class HVariableProperty extends HObject{
    public final static String SELECT_VARIABLE_PROPERTY_IDS_BY_INSTANCES = "SELECT_VARIABLE_PROPERTY_IDS_BY_INSTANCES";

    private String _propertyValue;
    private String _propertyName;
    private HXmlData _variable;

    /**
   * 
   */
    public HVariableProperty() {
        super();
    }

    public HVariableProperty(HXmlData var, String name, String value) {
        _variable = var;
        _propertyName = name;
        _propertyValue = value;
    }

    /**
     * @hibernate.many-to-one column="XML_DATA_ID" foreign-key="none"
     */
    public HXmlData getXmlData() {
        return _variable;
    }

    public void setXmlData(HXmlData xmldata) {
        _variable = xmldata;
    }

    /**
     * @hibernate.property column="PROP_VALUE" index="PROP_VALUE_IDX"
     */
    public String getValue() {
        return _propertyValue;
    }

    public void setValue(String value) {
        _propertyValue = value;
    }

    /**
     * @hibernate.property column="PROP_NAME" type="string" length="255"
     *                     not-null="true" index="PROP_NAME_IDX"
     */
    public String getName() {
        return _propertyName;
    }

    public void setName(String name) {
        _propertyName = name;
    }

}
