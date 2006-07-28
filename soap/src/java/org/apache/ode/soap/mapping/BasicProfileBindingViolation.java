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
package org.apache.ode.soap.mapping;


/**
 * An exception that encapsulates a WS-I Basic Profile violation.
 */
public class BasicProfileBindingViolation extends SoapBindingException {
  private String _violation;

  /**
   * Constructor.
   */
  public BasicProfileBindingViolation(String code, String locType, String loc, String msg) {
    super("Basic Profile Violation #" + code, locType, loc, code + ":" + msg);
    _violation = code;
  }


  /**
   * Get the BP-I violation identifier (e.g. "R2718").
   * @return violation identifier
   */
  public String getViolation() {
    return _violation;
  }
}