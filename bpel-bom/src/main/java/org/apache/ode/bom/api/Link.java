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
 * BOM representation of &lt;flow&gt;'s link declaration.
 */
public interface Link extends BpelObject {

  /**
   * The flow activity in which the link is declared.
   *
   * @return declaring &lt;flow&gt;
   */
  FlowActivity getDeclaredIn();

  /**
   * Get this link's name.
   *
   * @return name.
   */
  String getLinkName();

}
