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

package org.apache.ode.bpel.runtime.msgs;

import org.apache.ode.utils.msg.MessageBundle;

/**
 * Message bundle for this package.
 */
public class Messages extends MessageBundle {

  /**
   * Format a message about being unable to compile an XPath expression.
   * 
   * @param expr
   *          the expression in error
   * @param lineNo
   *          the line number for the expression
   * @param reason
   *          the reason that compilation failed.
   * 
   * Unable to compile XPath expression {0} (line#{1}): {2}
   */
  public String msgUnableToCompileXPath(String expr, int lineNo, String reason) {
    return this.format("Unable to compile XPath expression {0} (line#{1}): {2}", expr, lineNo,
        reason);
  }

  /**
   * Format a message about passing a non-static location path where one is
   * expected.
   * 
   * @param pathString
   *          the errant XPath expression
   * @param lineNo
   *          the line number where the expression occurs
   * @param type
   *          the type of the expression
   * 
   * Non-static string values for location paths are not allowed; the expression
   * {0} at line {1} evaluates to type {2}.
   */
  public String msgLocationMustBeString(String pathString, int lineNo, String type) {
    return this.format("Non-static string values for location paths are not allowed;"
        + "the expression {0} at line {1} evaluates to type {2}.", pathString, lineNo, type);
  }

  // TODO better message
  public String msgPropertyAliasReturnedRValue(String alias, String variable) {
    return this.format("msgPropertyAliasReturnedRValue: {0} {1}", alias, variable);
  }

  // TODO better message
  public String msgPropertyAliasReturnedNonElement(String alias, String variable) {
    return this.format("msgPropertyAliasReturnedNonElement: {0} {1}", alias, variable);
  }

  public String msgMessageExchangeFailureOnProcessCompletion() {
    return "Process has been completed, pending message exchanges must be failed.";
  }

  public String msgUnknownEPR(String string) {
    return format("Unknown EPR: {0}", string);
  }

}
