/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

/**
 * Assignment R-value defined in terms of a BPEL expression. This corresponds
 * to the "expression" form of the from-spec.
 * Note: With bpel 2.0, an expression may now be an L-value
 * (hence, the extension of 'To')
 */
public interface ExpressionVal extends From, To {
  /**
   * Get the R-Value {@link Expression}.
   * @return expression
   */
  Expression getExpression();

  /**
   * Set the R-Value {@link Expression}.
   * @param expression  r-valeu {@link Expression}
   */
  void setExpression(Expression expression);
}
