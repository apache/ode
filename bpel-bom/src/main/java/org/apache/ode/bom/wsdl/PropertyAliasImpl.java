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
package org.apache.ode.bom.wsdl;

import org.apache.ode.bom.api.Query;
import org.apache.ode.utils.NSContext;

import java.io.Serializable;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;


/**
 * WSDL extension element for a BPEL <code>&lt;propertyAlias&gt;</code> element.
 * @see org.apache.ode.bom.wsdl.PropertyAliasSerializer_11
 */
class PropertyAliasImpl implements PropertyAlias, ExtensibilityElement, Serializable {
	
	private static final long serialVersionUID = -1L;

  private QName _propertyName;
  private QName _messageType;
  private String _part;
  private Query _query;
  private NSContext _nsc;
  private QName _elementType;

  /**
   * @see javax.wsdl.extensions.ExtensibilityElement#setElementType(javax.xml.namespace.QName)
   */
  public void setElementType(QName arg0) {
    _elementType = arg0;
  }

  /**
   * @see javax.wsdl.extensions.ExtensibilityElement#getElementType()
   */
  public QName getElementType() {
    return _elementType;
  }

  /**
   * Set the name of the WSDL <code>message</code> type that this alias is to apply to.
   * @param name the <code>QName</code> of the message type
   */
  public void setMessageType(QName name) {
    _messageType = name;
  }

  /**
   * Get the name of the WSDL <code>message</code> type that this alias it to apply to.
   * @return the <code>QName</code> for the <code>messageType</code>
   */
  public QName getMessageType() {
    return _messageType;
  }

  /**
   * Set the name of the WSDL <code>part</code> that this alias is to apply to (within the
   * specified <code>message</code>).
   * @param name
   * @see #setMessageType(QName)
   */
  public void setPart(String name) {
    _part = name;
  }

  /**
   * Get the name of the WSDL <code>part</code> that this alias is to apply to (within the
   * specified <code>message</code>).
   * @return the name of the part
   * @see #getMessageType() 
   */
  public String getPart() {
    return _part;
  }

  /**
   * Set the <code>QName</code> of the property that this alias applies to.
   * @param name the <code>QName</code> of the property.
   */
  public void setPropertyName(QName name) {
    _propertyName = name;
  }

  /**
   * Get the <code>QName</code> of the property that this alias applies to.
   * @return the property <code>QName</code>
   */
  public QName getPropertyName() {
    return _propertyName;
  }

  /**
   * Set the location path query for this <code>OPropertyAlias</code>.  <em>No attempt is
   * made at this point to determine whether the path is a valid location path.</em>
   *
   * @param string the location path
   */
  public void setQuery(Query string) {
    _query = string;
  }

  /**
   * Get the location path query for the <code>OPropertyAlias</code> as originally
   * specified in the WSDL.
   *
   * @return the query
   */
  public Query getQuery() {
    return _query;
  }

  /**
   * @see javax.wsdl.extensions.ExtensibilityElement#setRequired(java.lang.Boolean)
   */
  public void setRequired(Boolean arg0) {
  }

  /**
   * @see javax.wsdl.extensions.ExtensibilityElement#getRequired()
   */
  public Boolean getRequired() {
    return Boolean.FALSE;
  }
  
  /**
   * Set the namespace context for the <code>&lt;propertyAlias&gt;</code> element that
   * created this object.  It will be used later.
   * @param n the <code>NSContext</code> that encapsulates the namespace context
   */
  public void setNSContext(NSContext n) {
    _nsc = n;
  }
  
  /**
   * Get the namespace context for the <code>&lt;propertyAlias&gt;</code> element that
   * created this object.
   * @return the <code>NSContext</code> the encapsulates the namespace context
   */
  public NSContext getNSContext() {
    return _nsc;
  }
}
