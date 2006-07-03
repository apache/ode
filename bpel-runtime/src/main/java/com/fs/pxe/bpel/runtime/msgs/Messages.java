/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.bpel.runtime.msgs;

import javax.xml.namespace.QName;

import com.fs.utils.msg.MessageBundle;

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
   * Unable to evaluate apply property alias "{0}" to incoming message: {1}
   */
  public String msgPropertyAliasDerefFailedOnMessage(String aliasDescription, String reason) {
    return this.format(
        "Unable to evaluate apply property alias \"{0}\" to incoming message: {1}",
        aliasDescription, reason);
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
  public String msgPropertyAliasReturnedNullSet(String alias, String variable) {
    return this.format("msgPropertyAliasReturnedNullSet: {0} {1}", alias, variable);
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

  public String msgUnknownOperation(String operationName,QName portType) {
    return format("Unknown operation \"{0}\" for port type \"{1}\".",operationName,portType);
  }

  public String msgMyRoleRoutingFailure(String messageExchangeId) {
    return format("Unable to route message exchange {0}, EPR was not specified " +
        "and the target my-role could not be inferred.",messageExchangeId);

  }

}
