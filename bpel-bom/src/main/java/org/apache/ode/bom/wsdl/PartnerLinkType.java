/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.wsdl;

import org.apache.ode.bom.api.BpelObject;

import java.util.Set;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

/**
 * Representation of BPEL <code>partnerLinkType</code> WSDL declaration.
 */
public interface PartnerLinkType extends BpelObject, ExtensibilityElement {

  /**
   * Set the name of this <code>&lt;partnerLinkType&gt;</code>.
   * @param qname
   */
  void setName(QName qname);

  /**
   * Get the name of this <code>&lt;partnerLinkType&gt;</code>.
   * @return the (qualified) name.
   */
  QName getName();


  /**
   * Get a role by name.
   *
   * @param roleName name of the role
   *
   * @return {@link Role} object or <code>null</code> (if name not found)
   */
  Role getRole(String roleName);

  /**
   * Get the roles defined in this <code>PartnerLinkType</code>. A partnerLink
   * link type may define one or two roles.
   *
   * @return roles defined in this partnerLink link type
   */
  Set <Role> getRoles();

  void addRole(Role role);

  void removeRole(Role role);

  /**
   * Representation of the WSDL partnerLink link type role elements.
   */
  public interface Role extends BpelObject {

    /**
     * Get the name of the role (e.g. "Buyer", "Seller").
     *
     * @return role role name
     */
    String getName();

    void setName(String name);

    /**
     * Get the port type for the role.
     */
    public QName getPortType();

    public void setPortType(QName portType);
  }

}
