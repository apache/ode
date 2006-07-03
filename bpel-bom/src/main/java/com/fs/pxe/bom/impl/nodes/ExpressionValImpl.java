/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.api.ExpressionVal;
import com.fs.utils.NSContext;

public class ExpressionValImpl extends BpelObjectImpl implements ExpressionVal {
  private static final long serialVersionUID = 1L;
	Expression _expression;

  public ExpressionValImpl(NSContext ns) {
    super(ns);
  }

  public com.fs.pxe.bom.api.Expression getExpression() {
    return _expression;
  }

  public void setExpression(Expression expression) {
    _expression = expression;
  }

}
