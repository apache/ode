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

import javax.xml.namespace.QName;

/**
 * Hibernate table-mapped class representing one valued property in a
 * correlation set.
 * 
 * @hibernate.class table="BPEL_CORRELATION_PROP"
 * @hibernate.query name="SELECT_CORPROP_IDS_BY_INSTANCES" query="select id from HCorrelationProperty as p where p.correlationSet in(select s from HCorrelationSet as s where s.instance in (:instances))"
 */
public class HCorrelationProperty extends HObject {
    public final static String SELECT_CORPROP_IDS_BY_INSTANCES = "SELECT_CORPROP_IDS_BY_INSTANCES";

    private String _name;
    private String _namespace;
    private String _value;
    private HCorrelationSet _correlationSet;

    public HCorrelationProperty() {
        super();
    }

    public HCorrelationProperty(String name, String namespace, String value,
            HCorrelationSet correlationSet) {
        super();
        _name = name;
        _namespace = namespace;
        _value = value;
        _correlationSet = correlationSet;
    }

    public HCorrelationProperty(QName qname, String value,
            HCorrelationSet correlationSet) {
        super();
        _name = qname.getLocalPart();
        _namespace = qname.getNamespaceURI();
        _value = value;
        _correlationSet = correlationSet;
    }

    /**
     * @hibernate.property column="NAME"
     */
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    /**
     * @hibernate.property column="NAMESPACE"
     */
    public String getNamespace() {
        return _namespace;
    }

    public void setNamespace(String namespace) {
        _namespace = namespace;
    }

    /**
     * @hibernate.property column="VALUE"
     */
    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    /**
     * @hibernate.many-to-one column="CORR_SET_ID" foreign-key="none"
     */
    public HCorrelationSet getCorrelationSet() {
        return _correlationSet;
    }

    public void setCorrelationSet(HCorrelationSet correlationSet) {
        _correlationSet = correlationSet;
    }

    public QName getQName() {
        return new QName(getNamespace(), getName());
    }
}
