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
package org.apache.ode.bpel.runtime.explang.konst;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.explang.*;
import org.apache.ode.bpel.o.OConstantExpression;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.utils.xsd.Duration;
import org.w3c.dom.Node;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the {@link org.apache.ode.bpel.explang.ExpressionLanguageRuntime} interface
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
