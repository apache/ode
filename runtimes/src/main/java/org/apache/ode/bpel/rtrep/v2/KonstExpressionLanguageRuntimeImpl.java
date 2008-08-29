/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.rtrep.v2;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.utils.xsd.Duration;
import org.w3c.dom.Node;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the ExpressionLanguageRuntime interface
 * for constant expressions.
 */
public class KonstExpressionLanguageRuntimeImpl implements ExpressionLanguageRuntime {

  public void initialize(Map properties) {
  }

  public String evaluateAsString(OExpression cexp, EvaluationContext ctx) throws FaultException  {
    OConstantExpression konst = (OConstantExpression) cexp;
    if (konst.getVal() instanceof String)
      return (String) konst.getVal();
    throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue);
  }

  public boolean evaluateAsBoolean(OExpression cexp, EvaluationContext ctx) throws FaultException {
    OConstantExpression konst = (OConstantExpression) cexp;
    if (konst.getVal() instanceof Boolean)
      return (Boolean) konst.getVal();
    throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue);

  }

  public Number evaluateAsNumber(OExpression cexp, EvaluationContext ctx) throws FaultException{
    OConstantExpression konst = (OConstantExpression) cexp;
    if (konst.getVal() instanceof Number)
      return (Number)konst.getVal();
    throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue);
  }

  public List evaluate(OExpression cexp, EvaluationContext ctx)
          throws FaultException {
    OConstantExpression konst = (OConstantExpression) cexp;
    if (konst.getVal() instanceof List)
      return (List) konst.getVal();
    throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue);
  }

  public Node evaluateNode(OExpression cexp, EvaluationContext context) throws FaultException{
    OConstantExpression konst = (OConstantExpression) cexp;
    if (konst.getVal() instanceof Node)
      return (Node) konst.getVal();
    throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue);
  }

  public Calendar evaluateAsDate(OExpression cexp, EvaluationContext context)
          throws FaultException  {
    OConstantExpression konst = (OConstantExpression) cexp;

    if (konst.getVal() instanceof Calendar)
      return (Calendar) konst.getVal();

    throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue);

  }

  public Duration evaluateAsDuration(OExpression cexp, EvaluationContext context)
          throws FaultException{

    OConstantExpression konst = (OConstantExpression) cexp;

    if (konst.getVal() instanceof Duration)
      return (Duration) konst.getVal();
    throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue);

  }
}
