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

package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.CompletionCondition;
import org.apache.ode.bom.api.Expression;
import org.apache.ode.bom.api.ForEachActivity;
import org.apache.ode.bom.api.ScopeActivity;

/**
 * BPEL object model representation of a  <code>&lt;forEach&gt;</code> activity.
 */
public class ForEachActivityImpl extends CompositeActivityImpl implements ForEachActivity {

  private String counterName;
  private boolean parallel;
  private Expression startCounter;
  private Expression finalCounter;
  private CompletionCondition completionCondition;
  private ScopeActivity scope;

  public String getCounterName() {
    return counterName;
  }

  public void setCounterName(String counterName) {
    this.counterName = counterName;
  }

  public boolean isParallel() {
    return parallel;
  }

  public void setParallel(boolean parallel) {
    this.parallel = parallel;
  }

  public Expression getStartCounter() {
    return startCounter;
  }

  public void setStartCounter(Expression startCounter) {
    this.startCounter = startCounter;
  }

  public Expression getFinalCounter() {
    return finalCounter;
  }

  public void setFinalCounter(Expression finalCounter) {
    this.finalCounter = finalCounter;
  }

  public CompletionCondition getCompletionCondition() {
    return completionCondition;
  }

  public void setCompletionCondition(CompletionCondition completionCondition) {
    this.completionCondition = completionCondition;
  }

  public ScopeActivity getScope() {
    return scope;
  }

  public void setScope(ScopeActivity scope) {
    this.scope = scope;
  }

  public String getType() {
    return "forEach";
  }
}
