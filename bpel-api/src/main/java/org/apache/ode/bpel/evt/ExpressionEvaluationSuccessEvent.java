/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.evt;

/**
 * Evaluation of an xpath expression.
 */
public class ExpressionEvaluationSuccessEvent extends ExpressionEvaluationEvent {
  private static final long serialVersionUID = 1L;
  private String _result;

	/** result of expression, cast as a string */
	public String getResult() {
    return _result;
  }

  public void setResult(String result) {
    _result = result;
  }
}
