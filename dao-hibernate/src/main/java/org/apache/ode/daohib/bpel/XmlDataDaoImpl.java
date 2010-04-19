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
package org.apache.ode.daohib.bpel;

import java.util.Iterator;

import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HVariableProperty;
import org.apache.ode.daohib.bpel.hobj.HXmlData;
import org.apache.ode.utils.DOMUtils;
import org.hibernate.Query;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Hibernate-based {@link XmlDataDAO} implementation.
 */
public class XmlDataDaoImpl extends HibernateDao implements XmlDataDAO {


    private static final String QUERY_PROPERTY =
            "from " + HVariableProperty.class.getName() +
                    " as p where p.xmlData.id = ? and p.name = ?";

    private HXmlData _data;
    private Node _node;

    /**
     * @param hobj
     */
    public XmlDataDaoImpl(SessionManager sm, HXmlData hobj) {
        super(sm, hobj);
        entering("XmlDataDaoImpl.XmlDataDaoImpl");
        _data = hobj;
    }
    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#isNull()
     */
    public boolean isNull() {
        entering("XmlDataDaoImpl.isNull");
        return (_data.getSimpleValue() == null && (_data.getData() == null || _data.getData().length == 0));
    }

    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#get()
     */
    public Node get() {
        entering("XmlDataDaoImpl.get");
        if(_node == null){
            _node = prepare();
        }
        return _node;
    }

    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#remove()
     */
    public void remove() {

    }
    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#set(org.w3c.dom.Node)
     */
    public void set(Node val) {
        entering("XmlDataDaoImpl.set");
        _node = val;
        if (val != null && val.getNamespaceURI() == null && "temporary-simple-type-wrapper".equals(val.getLocalName())) {
            _data.setSimpleType(true);
            String value = _node.getTextContent();
            if (value.length() <= 255) {
                _data.setSimpleValue(value);
                _data.setData(null);
            } else {
                _data.setData(value.getBytes());
                _data.setSimpleValue(null);
            }
        } else if (val instanceof Element) {
            _data.setSimpleType(false);
            _data.setData(DOMUtils.domToString(_node).getBytes());
            _data.setSimpleValue(null);
        } else {
            _data.setSimpleType(true);
            _data.setSimpleValue(_node.getNodeValue());
            _data.setData(null);
        }

        getSession().saveOrUpdate(_data);
        leaving("XmlDataDaoImpl.set");
    }
    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#getProperty(java.lang.String)
     */
    public String getProperty(String propertyName) {
        entering("XmlDataDaoImpl.getProperty");
        HVariableProperty p = _getProperty(propertyName);
        return p == null
                ? null
                : p.getValue();
    }

    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#setProperty(java.lang.String, java.lang.String)
     */
    public void setProperty(String pname, String pvalue) {
        entering("XmlDataDaoImpl.setProperty");
        HVariableProperty p = _getProperty(pname);
        if(p == null){
            p = new HVariableProperty(_data, pname, pvalue);
            getSession().save(p);
//            _data.addProperty(p);
        }else{
            p.setValue(pvalue);
            getSession().update(p);
        }
    }

    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#getScopeDAO()
     */
    public ScopeDAO getScopeDAO() {
        entering("XmlDataDaoImpl.getScopeDAO");
        return new ScopeDaoImpl(_sm,_data.getScope());
    }

    private HVariableProperty _getProperty(String propertyName){
        entering("XmlDataDaoImpl._getProperty");
        Iterator iter;
        Query qry = getSession().createQuery(QUERY_PROPERTY);
        qry.setLong(0, _data.getId());
        qry.setString(1, propertyName);
        iter = qry.iterate();
        return iter.hasNext()
                ? (HVariableProperty)iter.next()
                : null;
    }

    private Node prepare(){
        if(_data.isSimpleType()) {
            String data;
            if (_data.getSimpleValue() != null) {
                data = _data.getSimpleValue();
            } else {
                if(_data.getData() == null || _data.getData().length == 0)
                    return null;
                data = new String(_data.getData());
            }
            Document d = DOMUtils.newDocument();
            // we create a dummy wrapper element
            // prevents some apps from complaining
            // when text node is not actual child of document
            Element e = d.createElement("temporary-simple-type-wrapper");
            Text tnode = d.createTextNode(data);
            d.appendChild(e);
            e.appendChild(tnode);
            return tnode;
        } else {
            if(_data.getData() == null || _data.getData().length == 0)
                return null;
            String data = new String(_data.getData());
            try{
                return DOMUtils.stringToDOM(data);
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    public String getName() {
        return _data.getName();
    }

}
