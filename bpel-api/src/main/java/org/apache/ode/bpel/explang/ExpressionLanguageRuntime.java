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
