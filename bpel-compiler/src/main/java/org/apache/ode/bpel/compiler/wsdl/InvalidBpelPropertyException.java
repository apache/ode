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
package org.apache.ode.bpel.compiler.wsdl;

import javax.wsdl.WSDLException;


/**
 * An exception thrown in response to an invalid BPEL <code>&lt;property&gt;</code>
 * declaration.
 */
public class InvalidBpelPropertyException extends WSDLException {
  private static final long serialVersionUID = 1L;
    /**
   * Construct a new instance with the specified explanatory message.
   * @param msg an explanatory message.
   * @see WSDLException#WSDLException(java.lang.String, java.lang.String)
   */
  public InvalidBpelPropertyException(String msg) {
    super(WSDLException.INVALID_WSDL, msg);
  }
  /**
   * Construct a new instance with the specified explanatory message and the
   * exception that triggered this exception.
   * @param msg an explanatory message
   * @param t the <code>Throwable</code> that triggered this exception.
   * @see WSDLException#WSDLException(java.lang.String, java.lang.String, java.lang.Throwable)
   */
  public InvalidBpelPropertyException(String msg,
      Throwable t) {
    super(WSDLException.INVALID_WSDL, msg, t);
  }
}
