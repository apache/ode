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

import org.apache.ode.bom.impl.nodes.BpelObjectImpl;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;


/**
 * WSDL4J representation of a BPEL <code>&lt;partnerLink&gt;</code>
 * declaration.
 * @see org.apache.ode.bom.wsdl.PartnerLinkTypeSerializer
 */
class PartnerLinkTypeImpl extends BpelObjectImpl implements ExtensibilityElement, Serializable, PartnerLinkType {
	
	private static final long serialVersionUID = -1L;

  private HashSet<Role> _roles = new HashSet<Role>();
  private QName _name;
  private QName _elementType;

  public void setElementType(QName arg0) {
    _elementType = arg0;
  }

  public QName getElementType() {
    return _elementType;
  }

  public void setName(QName qname) {
    _name = qname;
  }

  public QName getName() {
    return _name;
  }

  public void setRequired(Boolean arg0) {
  }

  public Boolean getRequired() {
    return Boolean.FALSE;
  }

  public Role getRole(final String roleName) {
    if (roleName == null)
      throw new IllegalArgumentException("Null name not permitted.");
    return CollectionsX.find_if(_roles,new MemberOfFunction<Role>() {
      public boolean isMember(Role o) {
        return o.getName() != null && o.getName().equals(roleName);
      }
    });
  }


  public Set <Role> getRoles() {
    return Collections.unmodifiableSet(_roles);
  }

  public void addRole(PartnerLinkType.Role role) {
    _roles.add(role);

  }

  public void removeRole(PartnerLinkType.Role role) {
    _roles.remove(role);
  }

  /**
   * Representation of the WSDL partnerLink link type role elements.
   */
  public static class RoleImpl extends BpelObjectImpl implements Role {
  	
    private static final long serialVersionUID = -1L;
    
    private String _name;
    private QName _portType;
//    public static final QName QNAME = new QName(Constants.NS_BPEL4WS_PARTNERLINK,
//                                         "partnerLinkType");
//    public static final QName ROLE_QNAME = new QName(Constants.NS_BPEL4WS_PARTNERLINK,
//        "role");
//    public static final QName PORTTYPE_QNAME = new QName(Constants.NS_BPEL4WS_PARTNERLINK,
//        "portType");

    /**
     * Get the portName of the role (e.g. "Buyer", "Seller").
     *
     * @return role portName
     */
    public String getName() {
      return _name;
    }

    /**
     * Get the WSDL portType of the role (i.e. the interface implemented by
     * the object acting in the role).
     *
     * @return role portType
     */
    public QName getPortType() {
      return _portType;
    }

    public void setName(String name) {
      _name = name;
    }

    public void setPortType(QName portType) {
      _portType = portType;
    }
  }
}
