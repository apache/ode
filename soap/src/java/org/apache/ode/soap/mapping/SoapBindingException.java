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
 * Indicates a problem with the SOAP WSDL binding.
 */
public class SoapBindingException extends Exception {
  private static final long serialVersionUID = 2119776415112240117L;
  
  /** Internationalized, human-readable messsage. */
  private String _msg;
  private String _loc;
  private String _elType;

  /**
   * Constructor.
   * @param reason internal error message (printed in stack trace)
   * @param locType element type where error occured
   * @param loc name of element where error occured
   * @param ilmsg internationalized message
   */
  public SoapBindingException(String reason, String locType, String loc, String ilmsg) {
    super(reason);
    _elType =locType;
    _loc = loc;
    _msg = ilmsg;
  }

  public String getLocalizedMessage() {
    StringBuffer buf = new StringBuffer();
    buf.append(_elType);
    buf.append(" \"");
    buf.append(_loc);
    buf.append("\" : ");

    if (_msg == null)
      buf.append(getMessage());
    else
      buf.append(_msg);

    return buf.toString();
  }

  public String getLocationType() {
    return _elType;
  }

  public String getLocation() {
    return _loc;
  }
}

