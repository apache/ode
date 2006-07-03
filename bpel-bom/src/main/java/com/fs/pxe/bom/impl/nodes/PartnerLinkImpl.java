/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.PartnerLink;

import javax.xml.namespace.QName;
import java.io.Serializable;


/**
 * A BPEL object model representation of a partnerLink link.
 */
public class PartnerLinkImpl extends BpelObjectImpl implements Serializable, PartnerLink {
  private static final long serialVersionUID = -1L;

  private String _name;
  private String _myRole;
  private String _partnerRole;
  private QName _partnerLinkType;
  private boolean initializePartnerRole = false;
  private ScopeImpl _declaredIn;
 

  public void setMyRole(String string) {
    _myRole = string;
  }

  public String getMyRole() {
    return _myRole;
  }

  public void setName(String string) {
    _name = string;
  }

  public String getName() {
    return _name;
  }

  public void setPartnerLinkType(QName type) {
    _partnerLinkType = type;
  }

  public QName getPartnerLinkType() {
    return _partnerLinkType;
  }

  public void setPartnerRole(String string) {
    _partnerRole = string;
  }

  public String getPartnerRole() {
    return _partnerRole;
  }

  public boolean hasMyRole() {
    return _myRole != null;
  }

  public boolean hasPartnerRole() {
    return _partnerRole != null;
  }

  public boolean isInitializePartnerRole() {
    return initializePartnerRole;
  }

  public void setInitializePartnerRole(boolean initializePartnerRole) {
    this.initializePartnerRole = initializePartnerRole;
  }

  void setDeclaredIn(ScopeImpl scopeLikeConstruct) {
    _declaredIn = scopeLikeConstruct;
  }
}
