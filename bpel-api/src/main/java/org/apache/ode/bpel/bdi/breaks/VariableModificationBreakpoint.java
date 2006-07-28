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
package org.apache.ode.bpel.bdi.breaks;

/**
 * A break point that is activated when a variable is modified.
 */
public interface VariableModificationBreakpoint extends Breakpoint{
  
  /** 
   * Get the name of the declaring scope for the variable. 
   * @return the name of the scope of <code>null</code> if the scope is the process
   * itself.
   */
  public String scopeName();
  
  /**
   * Get the name of the variable for which the break point is set.
   * @return the name of the variable.
   */
  public String variableName();
}
