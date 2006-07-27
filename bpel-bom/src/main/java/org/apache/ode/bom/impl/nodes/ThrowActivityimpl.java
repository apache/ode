/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.ThrowActivity;
import org.apache.ode.utils.NSContext;

import javax.xml.namespace.QName;


/**
 * ThrowActivityimpl
 *
 * @author jguinney
 */
public class ThrowActivityimpl extends ActivityImpl implements ThrowActivity {

  private static final long serialVersionUID = -1L;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public ThrowActivityimpl(NSContext nsContext) {
    super(nsContext);
  }

  private QName _faultName;
  private String _faultVariable;

  public ThrowActivityimpl() {
    super();
  }

  public void setFaultName(QName faultName) {
    _faultName = faultName;
  }

  public QName getFaultName() {
    return _faultName;
  }

  public void setFaultVariable(String faultVariable) {
    _faultVariable = faultVariable;
  }

  public String getFaultVariable() {
    return _faultVariable;
  }

  /**
   * @see ActivityImpl#getType()
   */
  public String getType() {
    return "throw";
  }
}
