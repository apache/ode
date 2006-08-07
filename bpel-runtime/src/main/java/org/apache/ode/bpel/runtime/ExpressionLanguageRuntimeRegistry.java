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
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.explang.ExpressionLanguageRuntime;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OExpressionLanguage;
import org.apache.ode.utils.xsd.Duration;
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
      // backward compatibility.
      className = className.replace("com.fs.pxe.","org.apache.ode.");
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
