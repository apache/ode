/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

import javax.xml.namespace.QName;

/**
 * Representation of a BPEL <code>&lt;partnerLink&gt;</code> decleration.
 */
public interface PartnerLink extends BpelObject {
  /**
   * Sets the 'myRole' of the partnerLink link.
   *
   * @param string role
   */
  void setMyRole(String string);

  /**
   * Gets the 'myRole' of the partnerLink link.
   *
   * @return myRole
   */
  String getMyRole();

  /**
   * Sets the name of the partnerLink link.
   *
   * @param string partnerLink link name.
   */
  void setName(String string);

  /**
   * Name of the partnerLink link.
   *
   * @return partnerLink link name
   */
  String getName();

  /**
   * Set the parther link type.
   *
   * @param type partnerLink link type
   */
  void setPartnerLinkType(QName type);

  /**
   * Get the partnerLink link type.
   *
   * @return partnerLink link type
   */
  QName getPartnerLinkType();

  /**
   * Set the 'partnerRole'
   *
   * @param string partnerLink role
   */
  void setPartnerRole(String string);

  /**
   * Get the 'partnerRole'
   *
   * @return partnerLink role
   */
  String getPartnerRole();

  /**
   * Returns <code>true</code> if partnerLink link has a 'myRole'
   *
   * @return 'myRole' is set.
   */
  boolean hasMyRole();

  /**
   * Determine if  'partnerRole' is set.
   *
   * @return <code>true</code> if partnerLink link has a 'partnerRole'
   */
  boolean hasPartnerRole();

  /**
   * Determine if the partner role has to be initialized with default value
   * @return initialize
   */
  public boolean isInitializePartnerRole();

  /**
   * Set whether partner has to be initialized
   * @param initializePartnerRole
   */
  public void setInitializePartnerRole(boolean initializePartnerRole);

}
