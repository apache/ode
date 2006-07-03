/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.api.WaitActivity;
import com.fs.utils.NSContext;


/**
 * WaitActivityImpl
 */
public class WaitActivityImpl extends ActivityImpl implements WaitActivity {

  private static final long serialVersionUID = -1L;

  private Expression _for;
  private Expression _until;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public WaitActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public WaitActivityImpl() {
    super();
  }

  public void setFor(Expression for1) {
    _for = for1;
  }

  public com.fs.pxe.bom.api.Expression getFor() {
    return _for;
  }

  /**
   * @see ActivityImpl#getType()
   */
  public String getType() {
    return "wait";
  }

  public void setUntil(Expression until) {
    _until = until;
  }

  public Expression getUntil() {
    return _until;
  }
}
