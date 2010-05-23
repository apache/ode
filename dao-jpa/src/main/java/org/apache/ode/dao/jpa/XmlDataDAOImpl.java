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

import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
@Entity
@Table(name="ODE_XML_DATA")
@NamedQueries({
    @NamedQuery(name=XmlDataDAOImpl.SELECT_XMLDATA_IDS_BY_PROCESS, query="select distinct x._id from XmlDataDAOImpl as x where x._scope._processInstance._process = :process"),
    @NamedQuery(name=XmlDataDAOImpl.SELECT_XMLDATA_IDS_BY_INSTANCE, query="select distinct x._id from XmlDataDAOImpl as x where x._scope._processInstance = :instance"),
    @NamedQuery(name=XmlDataDAOImpl.DELETE_XMLDATA_BY_SCOPE_IDS, query="delete from XmlDataDAOImpl as x where x._scopeId in(:scopeIds)")
})
public class XmlDataDAOImpl implements XmlDataDAO {
	public final static String SELECT_XMLDATA_IDS_BY_PROCESS = "SELECT_XMLDATA_IDS_BY_PROCESS";
	public final static String SELECT_XMLDATA_IDS_BY_INSTANCE = "SELECT_XMLDATA_IDS_BY_INSTANCE";
	public final static String DELETE_XMLDATA_BY_SCOPE_IDS = "DELETE_XMLDATA_BY_SCOPE_IDS";
	
	@Id @Column(name="XML_DATA_ID") 
	@GeneratedValue(strategy=GenerationType.AUTO)
	@SuppressWarnings("unused")
	private Long _id;
	@Lob @Column(name="DATA")
    private String _data;
	@Transient
    private Node _node;
	@Basic @Column(name="IS_SIMPLE_TYPE")
    private boolean _isSimpleType;
	@Basic @Column(name="NAME")
    private String _name;

    @OneToMany(targetEntity=XmlDataProperty.class,mappedBy="_xmlData",fetch=FetchType.EAGER,cascade={CascadeType.ALL})
    private Collection<XmlDataProperty> _props = new ArrayList<XmlDataProperty>();

    @SuppressWarnings("unused")
	@Basic @Column(name="SCOPE_ID", nullable=true, insertable=false, updatable=false)
    private Long _scopeId;
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="SCOPE_ID")
	private ScopeDAOImpl _scope;
	
	public XmlDataDAOImpl() {}
	public XmlDataDAOImpl(ScopeDAOImpl scope, String name){
		_scope = scope;
		_name = name;
	}

	public Node get() {
		if ( _node == null && _data != null ) {
		   if(_isSimpleType){
		        Document d = DOMUtils.newDocument();
		        // we create a dummy wrapper element
		        // prevents some apps from complaining
		        // when text node is not actual child of document
		        Element e = d.createElement("text-node-wrapper");
		        Text tnode = d.createTextNode(_data);
		        d.appendChild(e);
		        e.appendChild(tnode);
		        _node = tnode;
		   }else{
		      try{
		          _node = DOMUtils.stringToDOM(_data);
		      }catch(Exception e){
		          throw new RuntimeException(e);
		      }
		   }
		}
		
		return _node;
	}

	public String getName() {
		return _name;
	}

	public String getProperty(String propertyName) {
        for (XmlDataProperty prop : _props) {
            if (prop.getPropertyKey().equals(propertyName)) return prop.getPropertyValue();
        }
        return null;
	}

    private XmlDataProperty getPropertyObject(String propertyName) {
        for (XmlDataProperty prop : _props) {
            if (prop.getPropertyKey().equals(propertyName)) return prop;
        }
        return null;
    }

	public ScopeDAO getScopeDAO() {
		return _scope;
	}

	public boolean isNull() {
		return _data == null;
	}

	public void remove() {

	}

	public void set(Node val) {
		_node = val;
		if ( val instanceof Element ) {
			_isSimpleType = false;
			_data = DOMUtils.domToString(val);
		} else if (_node != null) {
			_isSimpleType = true;
			_data = _node.getNodeValue();
		}
	}

	public void setProperty(String pname, String pvalue) {
        XmlDataProperty prop = getPropertyObject(pname);
        if (prop == null) {
            _props.add(new XmlDataProperty(pname, pvalue, this));
        } else {
            prop.setPropertyValue(pvalue);
        }
	}

}
