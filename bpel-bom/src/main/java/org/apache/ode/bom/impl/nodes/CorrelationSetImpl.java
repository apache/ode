/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.CorrelationSet;
import org.apache.ode.bom.api.Scope;

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
