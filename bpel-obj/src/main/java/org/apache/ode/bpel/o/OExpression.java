/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

/**
 * Base class for compiled expressions. The exact form of a compiled expression is
 * dependent on the compiler implementation.
 */
public abstract class OExpression extends OBase {
  static final long serialVersionUID = -1L  ;
  
  public OExpressionLanguage expressionLanguage;

  public OExpression(OProcess owner) {
    super(owner);
  }

  /** Get the expression language used to generate this expression. */
  public OExpressionLanguage getExpressionLanguage() {
    return expressionLanguage;
    }

}
