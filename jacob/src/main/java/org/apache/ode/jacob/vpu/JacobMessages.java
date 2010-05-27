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

package org.apache.ode.jacob.vpu;

import org.apache.ode.utils.msg.MessageBundle;

/**
 * Messages for the Jacob VPU.
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class JacobMessages extends MessageBundle {

  /**
   * Error indicating that client-code (i.e. not the VPU kernel) threw an
   * unexpected exception.
   *
   * @param methodName
   *          name of offending method
   * @param className
   *          name of offending class
   *
   * Method "{0}" in class "{1}" threw an unexpected exception.
   */
  public String msgClientMethodException(String methodName, String className) {
    return this.format("Method \"{0}\" in class \"{1}\" threw an unexpected exception.",
        methodName, className);
  }

  // TODO
  public String msgContDeHydrationErr(String channel, String name) {
    throw new UnsupportedOperationException();
  }

  /**
   * Error indicating that a re-hydration of a saved _continuation object could
   * not be completed.
   *
   * @param channel
   *          channel with the dangling _continuation
   * @param mlClassName
   *          name of de-hydrated {@link org.apache.ode.jacob.ChannelListener} object
   *
   */
  public String msgContHydrationErr(String channel, String mlClassName) {
    throw new UnsupportedOperationException();
  }

  /**
   * Internal error indicating that a required client method was not accessible
   * due to security protections errors. This may be caused by a change to the
   * client class definitions.
   *
   * Method "{0}" in class "{1}" is not accessible.
   */
  public String msgMethodNotAccessible(String methodName, String className) {
    return this.format("Method \"{0}\" in class \"{1}\" is not accessible.", methodName,
        className);
  }

}
