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
 * Representation of the BPEL <code>&lt;while&gt;</code> activity.
 */
public interface WhileActivity extends Activity {
  /**
   * Set the child (repeated) activity.
   *
   * @param activity repeated activity
   */
  void setActivity(Activity activity);

  /**
   * Get the child (repeated) activity.
   *
   * @return repeated activity
   */
  Activity getActivity();

  /**
   * Set the while condition.
   *
   * @param condition the while condition
   */
  void setCondition(Expression condition);

  /**
   * Get the while condition.
   *
   * @return the while condition
   */
  Expression getCondition();
}
