/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.explang.ConfigurationException;
import com.fs.pxe.bpel.explang.EvaluationContext;
import com.fs.pxe.bpel.explang.EvaluationException;
import com.fs.pxe.bpel.explang.ExpressionLanguageRuntime;
import com.fs.pxe.bpel.o.OExpression;
import com.fs.pxe.bpel.o.OExpressionLanguage;
import com.fs.utils.xsd.Duration;
import org.w3c.dom.Node;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A registry of {@link ExpressionLanguageRuntime} objects that is able to map
 * a given expression to the appropriate language runtime.
 */
public class ExpressionLanguageRuntimeRegistry  {
  private final Map<OExpressionLanguage, ExpressionLanguageRuntime> _runtimes =
    new HashMap<OExpressionLanguage, ExpressionLanguageRuntime>();

  public ExpressionLanguageRuntimeRegistry()  {}

  public void registerRuntime(OExpressionLanguage oelang) throws ConfigurationException {
    try {
      String className = oelang.properties.get("runtime-class");
      Class cls = Class.forName(className);
      ExpressionLanguageRuntime elangRT = (ExpressionLanguageRuntime) cls.newInstance();
      elangRT.initialize(oelang.properties);
      _runtimes.put(oelang, elangRT);
    } catch (ConfigurationException ce) {
      throw ce;
    } catch (IllegalAccessException e) {
      throw new ConfigurationException("Illegal Access Error", e);
    } catch (InstantiationException e) {
      throw new ConfigurationException("Instantiation Error", e);
    } catch (ClassNotFoundException e) {
      throw new ConfigurationException("Class Not Found Error", e);
    }

  }

  public String evaluateAsString(OExpression cexp, EvaluationContext ctx) throws FaultException , EvaluationException {
    return findRuntime(cexp).evaluateAsString(cexp, ctx);
  }

  public boolean evaluateAsBoolean(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    return findRuntime(cexp).evaluateAsBoolean(cexp, ctx);
  }

  public Number evaluateAsNumber(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    return findRuntime(cexp).evaluateAsNumber(cexp, ctx);
  }

  public List evaluate(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    return findRuntime(cexp).evaluate(cexp, ctx);
  }

  public Node evaluateNode(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    return findRuntime(cexp).evaluateNode(cexp, ctx);
  }

  public Calendar evaluateAsDate(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    return findRuntime(cexp).evaluateAsDate(cexp, ctx);
  }

  public Duration evaluateAsDuration(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    return findRuntime(cexp).evaluateAsDuration(cexp, ctx);
  }

  private ExpressionLanguageRuntime findRuntime(OExpression cexp) {
    return _runtimes.get(cexp.expressionLanguage);
  }

}
