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
 * Base interface of all BPEL break-point. Used to enable/disable the
 * break-point.
 */
public interface Breakpoint {

  /**
   * Enable or disable the break point.
   * 
   * @param enabled
   *          if <code>true</code> enable, otherwise disable
   */
  public void setEnabled(boolean enabled);

  /**
   * Test whether the break point is enabled.
   * 
   * @return <code>true</code> if break-point is enabled, <code>false</code>
   *         otherwise
   */
  public boolean isEnabled();

}
