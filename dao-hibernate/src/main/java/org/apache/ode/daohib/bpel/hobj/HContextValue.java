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
 * Hibernate table-mapped class representing one valued property in a
 * context.
 * 
 * @hibernate.class table="BPEL_CONTEXT_VALUE"
 */
public class HContextValue extends HObject {
    private String _key;
    private String _data;
    private String _value;
    private String _namespace;
    
    private HPartnerLink _partnerLink;

    /**
     * @hibernate.property
     * @hibernate.column name="NAMESPACE" length="100" not-null="true" index="IDX_CTX_NSKEY"
     */
    public String getNamespace() {
        return _namespace;
    }

    public void setNamespace(String namespace) {
        _namespace = namespace;
    }

    /**
     * @hibernate.property
     * @hibernate.column name="KEY_NAME" length="100" not-null="true" index="IDX_CTX_NSKEY"
     */
    public String getKey() {
        return _key;
    }

    public void setKey(String key) {
        _key = key;
    }
    
    /**
     * @hibernate.property
     * @hibernate.column name="VALUE" length="255" not-null="true" index="IDX_CTX_VAL"
     */
    public String getInternalValue() {
        return _value;
    }
    
    public void setInternalValue(String value) {
        _value = value;
    }
    
    /**
     * @hibernate.property
     * @hibernate.column name="DATA" sql-type="blob(2G)"
     */
    public String getData() {
        return _data;
    }
    
    public void setData(String data) {
        _data = data;
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

    /**
     * @hibernate.many-to-one column="PARTNERLINK" foreign-key="none"
     */
    public HPartnerLink getPartnerLink() {
        return _partnerLink;
    }

    public void setPartnerLink(HPartnerLink pl) {
        _partnerLink = pl;
    }

}
