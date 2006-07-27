/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.explang;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.utils.xsd.Duration;
import org.w3c.dom.Node;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public interface ExpressionLanguageRuntime {

  /**
   * Initialize the expression evaluation runtime.
   * @param properties configuration properties
   */
  void initialize(Map properties) throws ConfigurationException;

  String evaluateAsString(OExpression cexp, EvaluationContext ctx)
          throws FaultException, EvaluationException;

  boolean evaluateAsBoolean(OExpression cexp, EvaluationContext ctx)
          throws FaultException, EvaluationException;

  Number evaluateAsNumber(OExpression cexp, EvaluationContext ctx)
          throws FaultException, EvaluationException;

  List evaluate(OExpression cexp, EvaluationContext ctx)
          throws FaultException, EvaluationException;

  Calendar evaluateAsDate(OExpression cexp, EvaluationContext context)
          throws FaultException, EvaluationException;

  Duration evaluateAsDuration(OExpression cexp, EvaluationContext context)
          throws FaultException, EvaluationException;

  Node evaluateNode(OExpression cexp, EvaluationContext context)
          throws FaultException, EvaluationException;
}
