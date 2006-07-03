/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

public class ExpressionEvaluationEvent extends ScopeEvent {
  private static final long serialVersionUID = 1L;
  private String _expression;

  public String getExpression() {
    return _expression;
  }

  public void setExpression(String expression) {
    _expression = expression;
  }

}
