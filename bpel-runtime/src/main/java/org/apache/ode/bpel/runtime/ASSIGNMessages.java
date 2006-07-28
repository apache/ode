
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

package org.apache.ode.bpel.runtime;

import org.apache.ode.utils.msg.MessageBundle;

/**
 * Internationalization for messages related to assignment.
 */
public class ASSIGNMessages extends MessageBundle {

  /** R-Value expression "{0}" returned multiple nodes. */
  public String msgRValueMultipleNodesSelected(String rvalue) {
    return this.format("R-Value expression \"{0}\" returned multiple nodes.", rvalue);
  }

  /** R-Value expression "{0}" did not select any nodes. */
  public String msgRValueNoNodesSelected(String rvalue) {
    return this.format("R-Value expression \"{0}\" did not select any nodes.", rvalue);
  }

  /** A literal must not contain more than one element information item (EII). */
  public String msgLiteralContainsMultipleEIIs() {
    return this
        .format("A literal must not contain more than one element information item (EII).");
  }

  /** A literal must not contain mixed content. */
  public String msgLiteralContainsMixedContent() {
    return this.format("A literal must not contain mixed content.");
  }

  /**
   * A literal must contain either a text information item (TII) or a single
   * element information item (EII).
   */
  public String msgLiteralMustContainTIIorEII() {
    return this.format("A literal must contain either a text information item (TII)"
        + " or a single element information item (EII).");
  }

  /** InternalError: Unexpected selection failure (contact tech support): {0} */
  public String msgInternalError(String msg) {
    return this.format(
        "InternalError: Unexpected selection failure (contact tech support): {0}", msg);
  }

  /**
   * An exception occured while evaluating "{0}": {1}
   */
  public String msgEvalException(String expr, String msg) {
    return this.format("An exception occured while evaluating \"{0}\": {1}", expr, msg);
  }

  /** The R-Value must select one item. */
  public String msgEmptyRValue() {
    return this.format("The R-Value must select one item.");
  }

  /**
   * The R-Value must be a text information item (TII) or element information
   * item (EII).
   */
  public String msgInvalidRValue() {
    return this.format("The R-Value must be a text information item (TII) or"
        + " element information item (EII)");
  }

  /**
   * The L-Value must be a text information item (TII), element information item
   * (EII), or attribute information item (AII).
   */
  public String msgInvalidLValue() {
    return this.format("The L-Value must be a text information item (TII),"
        + " element information item (EII), or attribute information item (AII).");
  }

}
