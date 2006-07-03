/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime.explang.konst;

import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.explang.*;
import com.fs.pxe.bpel.o.OConstantExpression;
import com.fs.pxe.bpel.o.OExpression;
import com.fs.utils.xsd.Duration;
import org.w3c.dom.Node;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the {@link com.fs.pxe.bpel.explang.ExpressionLanguageRuntime} interface
 * for constant expressions.
 */
public class KonstExpressionLanguageRuntimeImpl implements ExpressionLanguageRuntime {

  public void initialize(Map properties) throws ConfigurationException {
  }

  public String evaluateAsString(OExpression cexp, EvaluationContext ctx) throws FaultException , EvaluationException {
    OConstantExpression konst = (OConstantExpression) cexp;
    if (konst.getVal() instanceof String)
      return (String) konst.getVal();
    throw new TypeCastException(TypeCastException.TYPE_STRING, konst.getVal().toString());
  }

  public boolean evaluateAsBoolean(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    OConstantExpression konst = (OConstantExpression) cexp;
    if (konst.getVal() instanceof Boolean)
      return ((Boolean)konst.getVal()).booleanValue();
    throw new TypeCastException(TypeCastException.TYPE_BOOLEAN, konst.getVal().toString());
  }

  public Number evaluateAsNumber(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    OConstantExpression konst = (OConstantExpression) cexp;
    if (konst.getVal() instanceof Number)
      return (Number)konst.getVal();
    throw new TypeCastException(TypeCastException.TYPE_NUMBER, konst.getVal().toString());
  }

  public List evaluate(OExpression cexp, EvaluationContext ctx)
          throws FaultException, EvaluationException {
    OConstantExpression konst = (OConstantExpression) cexp;
    if (konst.getVal() instanceof List)
      return (List) konst.getVal();
    throw new TypeCastException(TypeCastException.TYPE_NODELIST, konst.getVal().toString());
  }

  public Node evaluateNode(OExpression cexp, EvaluationContext context) throws FaultException, EvaluationException {
    OConstantExpression konst = (OConstantExpression) cexp;
    if (konst.getVal() instanceof Node)
      return (Node) konst.getVal();
    throw new TypeCastException(TypeCastException.TYPE_NODE, konst.getVal().toString());
  }

  public Calendar evaluateAsDate(OExpression cexp, EvaluationContext context)
          throws FaultException , EvaluationException {
    OConstantExpression konst = (OConstantExpression) cexp;

    if (konst.getVal() instanceof Calendar)
      return (Calendar) konst.getVal();

    throw new TypeCastException(TypeCastException.TYPE_DATE, konst.getVal().toString());

  }

  public Duration evaluateAsDuration(OExpression cexp, EvaluationContext context)
          throws FaultException, EvaluationException {

    OConstantExpression konst = (OConstantExpression) cexp;

    if (konst.getVal() instanceof Duration)
      return (Duration) konst.getVal();

    throw new TypeCastException(TypeCastException.TYPE_DURATION, konst.getVal().toString());
  }
}
