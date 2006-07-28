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

package org.apache.ode.bom.api;

/**
 * Representation of the BPEL <code>&lt;forEach&gt;</code> activity.
 */
public interface ForEachActivity extends CompositeActivity {

  /**
   * Name of the variable that will hold the counter used
   * for iteration.
   * @param counterName
   */
  void setCounterName(String counterName);

  /**
   * Gets the counter variable name used for iteration.
   * @return counter variable name
   */
  String getCounterName();

  /**
   * Defines whether the different iterations should be
   * executed parrallely or sequentially.
   * @param parrallel
   */
  void setParallel(boolean parrallel);

  /**
   * Returns whether this forEach executes iterations on nested
   * scopes parrallely or sequentially
   * @return true if parrallel, false if sequential
   */
  boolean isParallel();

  /**
   * Sets the expression that will be used as a start value for
   * the iteration counter. Should resolve to a xs:unsignedint.
   * @param expr
   */
  void setStartCounter(Expression expr);

  /**
   * Gets the expression that will be used as a start value for
   * the iteration counter.
   * @return start iteration counter
   */
  Expression getStartCounter();

  /**
   * Sets the expression that will be used as a termination value for
   * the forEach iterations. Should resolve to a xs:unsignedint.
   * @param expr
   */
  void setFinalCounter(Expression expr);

  /**
   * Sets the expression that will be used as a termination value for
   * the forEach iterations.
   * @return final counter expression
   */
  Expression getFinalCounter();

  /**
   * Sets a completion condition defining how many child scope
   * completions can occur before the forEach completes.
   * @param condition
   */
  void setCompletionCondition(CompletionCondition condition);

  /**
   * Gets a completion condition defining how many child scope 
   * completions can occur before the forEach completes.
   * @return completion condition
   */
  CompletionCondition getCompletionCondition();

  /**
   * Sets the scope activity that we will iterate on (several copies
   * will be executed with different counter values).
   * @param scope
   */
  void setScope(ScopeActivity scope);

  /**
   * Gets the scope activity that we will iterate on.
   * @return child scope activity
   */
  ScopeActivity getScope();
}
