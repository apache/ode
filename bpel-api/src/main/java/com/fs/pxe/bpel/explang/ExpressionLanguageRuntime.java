/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.explang;

import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.o.OExpression;
import com.fs.utils.xsd.Duration;
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
