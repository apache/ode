/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Catch;
import org.apache.ode.bom.api.FaultHandler;
import org.apache.ode.bom.api.Scope;

import java.util.ArrayList;


/**
 * BPEL object model rerpesentation of a fault handler consisting of
 * one or more {@link Catch} objects.
 */
public class FaultHandlerImpl extends BpelObjectImpl implements FaultHandler {

  private static final long serialVersionUID = -1L;

  private ArrayList<Catch> _catches = new ArrayList<Catch>();

  /**
   * For what scope is this a fault handler?
   */
  private Scope _scope;
  

  /**
   * Constructor.
   */
  public FaultHandlerImpl() {
  }

  public Catch[] getCatches() {
    return _catches.toArray(new Catch[_catches.size()]);
  }

  public Scope getScope() {
    return _scope;
  }

  public void addCatch(Catch c) {
    _catches.add(c);
  }

  void setDeclaredIn(ScopeImpl scopeLikeConstruct) {
    _scope = scopeLikeConstruct;
  }
}
