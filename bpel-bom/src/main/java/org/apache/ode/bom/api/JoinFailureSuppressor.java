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
 * Interface implemented by nodes (such as {@link Activity} and {@link Process}
 * that can suppress join failure.
 */
public interface JoinFailureSuppressor extends BpelObject {
  /** Model element does not specify a <code>suppressJoinFailure</code> override. */
  short SUPJOINFAILURE_NOTSET = 0;

  /** Model element overrides <code>suppressJoinFailure</code> to <code>no</code>. */
  short SUPJOINFAILURE_NO = -1;

  /** Model element overrides <code>suppressJoinFailure</code> to <code>no</code>. */
  short SUPJOINFAILURE_YES = 1;

  /**
   * Set the suppress join failure flag.
   *
   * @param suppressJoinFailure suppress join failure flag code
   */
  void setSuppressJoinFailure(short suppressJoinFailure);

  /**
   * Get the suppress join failure flag.
   *
   * @return suppress join failure flag code
   */
  short getSuppressJoinFailure();

}
