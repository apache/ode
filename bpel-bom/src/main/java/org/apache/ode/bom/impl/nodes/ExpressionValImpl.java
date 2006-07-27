/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Expression;
import org.apache.ode.bom.api.ExpressionVal;
import org.apache.ode.utils.NSContext;

public class ExpressionValImpl extends BpelObjectImpl implements ExpressionVal {
  private static final long serialVersionUID = 1L;
	Expression _expression;

  public ExpressionValImpl(NSContext ns) {
    super(ns);
  }

  public org.apache.ode.bom.api.Expression getExpression() {
    return _expression;
  }

  public void setExpression(Expression expression) {
    _expression = expression;
  }

}
