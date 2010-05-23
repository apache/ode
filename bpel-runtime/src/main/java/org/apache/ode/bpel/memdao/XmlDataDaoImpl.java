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
package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Properties;


/**
 * A very simple, in-memory implementation of the {@link XmlDataDAO} interface.
 */
class XmlDataDaoImpl extends DaoBaseImpl implements XmlDataDAO {

    private Node _data;
    private Properties _properties = new Properties();
    private ScopeDaoImpl _scope;
    private String _name;

    XmlDataDaoImpl(ScopeDaoImpl scope,String varname){
        _scope = scope;
        _name = varname;
    }

    /**
     * @see XmlDataDAO#isNull()
     */
    public boolean isNull() {
        return _data == null;
    }

    /**
     * @see XmlDataDAO#get()
     */
    public Node get() {
        if (_data == null) return null;
        
        Document doc = DOMUtils.newDocument();
        Node copy = doc.importNode(_data, true);
        if (_data instanceof Element) doc.appendChild(copy);
        else {
            Element wrapper = doc.createElement("wrapper");
            wrapper.appendChild(copy);
        }
        return copy;
    }

    /**
     * @see XmlDataDAO#remove()
     */
    public void remove() {
        _data = null;
    }

    /**
     * @see XmlDataDAO#set(org.w3c.dom.Node)
     */
    public void set(Node val) {
        if (val == null || !(val instanceof Element)) {
            _data = val;
            return;
        }
        // For some reason we're getting some weird DOM trees from ServiceMix. Until we
        // spot where it exactly comes from, this fixes it.
        // The weirdness lies in elements being in xmlns="" when printed with a DOMWriter
        // but having a ns when the elements are queried directly.
        Document doc = DOMUtils.newDocument();
        _data = doc.importNode(val, true);
        doc.appendChild(_data);
    }

    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#getProperty(java.lang.String)
     */
    public String getProperty(String propertyName) {
        return _properties.getProperty(propertyName);
    }

    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#setProperty(java.lang.String, java.lang.String)
     */
    public void setProperty(String pname, String pvalue) {
        _properties.setProperty(pname, pvalue);
    }

    public Properties getProperties() {
        return _properties;
    }

    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#getScopeDAO()
     */
    public ScopeDAO getScopeDAO() {
        return _scope;
    }

    public String getName() {
        return _name;
    }

}
