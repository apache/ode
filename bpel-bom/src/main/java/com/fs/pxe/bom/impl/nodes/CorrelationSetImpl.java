/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.CorrelationSet;
import com.fs.pxe.bom.api.Scope;

import javax.xml.namespace.QName;


/**
 * BPEL Object Model representation of a correlation set.
 */
public class CorrelationSetImpl extends BpelObjectImpl implements CorrelationSet {

  private static final long serialVersionUID = -1L;

  private String _name;
  private QName[] _properties;
  private ScopeImpl _declaredIn;
 
  public CorrelationSetImpl() {
  }

  public Scope getDeclaringScope() {
    return _declaredIn;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public QName[] getProperties() {
    return _properties;
  }

  public void setProperties(QName[] properties) {
    _properties = properties;
  }

  void setDeclaredIn(ScopeImpl scopeLikeConstruct) {
    _declaredIn = scopeLikeConstruct;
  }

}
