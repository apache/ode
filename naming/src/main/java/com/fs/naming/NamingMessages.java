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

package com.fs.naming;

import org.apache.ode.utils.msg.MessageBundle;

import javax.naming.Name;

/**
 * Internationalization for the <code>com.fs.naming</code> package.
 */
public class NamingMessages extends MessageBundle {

  /**
   * Communication error encountered in JNDI: {0}
   */
  public String msgCommunicationError(String message) {
    return this.format("Communication error encountered in JNDI: {0}", message);
  }

  /**
   * Unable to connect to JNDI provider at URL "{0}".
   */
  public String msgConnectErr(String urlS) {
    return this.format("Unable to connect to JNDI provider at URL \"{0}\".", urlS);
  }

  /**
   * The context "{0}" is not empty and cannot be removed.
   */
  public String msgContextNotEmpty(Name name) {
    return this.format("The context \"{0}\" is not empty and cannot be removed.", name);
  }

  /**
   * Unable to dereference object named "{0}".
   */
  public String msgDeRefError(Name name) {
    return this.format("Unable to dereference object named \"{0}\".", name);
  }

  /**
   * The name "{0}" is invalid.
   */
  public String msgInvalidName(Name name) {
    return this.format("The name \"{0}\" is invalid.", name);
  }

  /**
   * The JNDI provider URL "{0}" is not valid.
   */
  public String msgInvalidProviderURL(String providerURL) {
    return this.format("The JNDI provider URL \"{0}\" is not valid.", providerURL);
  }

  /**
   * The name "{0}" is already bound in context "{1}".
   */
  public String msgNameAlreadyBound(Name name) {
    return this.format("The name \"{0}\" is already bound in context \"{1}\".", name);
  }

  /**
   * No object named "{0}" found.
   */
  public String msgNameNotFound(Name name) {
    return this.format("No object named \"{0}\" found.", name);
  }

  /**
   * No object named "{0}" found; found up to "{1}".
   */
  public String msgNameNotFound(String simple, Name prefix) {
    return this.format("Noobject named \"{0}\" found; found up to \"{1}\".", simple, prefix);
  }

  /**
   * The value "{1}" is not bindable at name "{0}".
   */
  public String msgNotBindable(Name name, String name1) {
    return this.format("The value \"{1}\" is not bindable at name \"{0}\".", name);
  }

  /**
   * The object "{0}" is not a context.
   */
  public String msgNotContext(Name name) {
    return this.format("The object \"{0}\" is not a context.", name);
  }

  /**
   * "{0}" and "{1}" are not the same context.
   */
  public String msgNotSameContext(Name oldName, Name newName) {
    return this.format("\"{0}\" and \"{1}\" are not the same context.", oldName, newName);
  }

  /**
   * The provider URL is not set.
   */
  public String msgProviderUrlNotSet() {
    return this.format("The provider URL is not set.");
  }

  /**
   * The context "{0}" is read-only.
   */
  public String msgReadOnly(Name name) {
    return this.format("The context \"{0}\" is read-only.", name);
  }

}
