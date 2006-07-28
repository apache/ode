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

package org.apache.ode.bpel.elang.xpath10.compiler;

import org.apache.ode.bpel.capi.CompilationMessage;
import org.apache.ode.bpel.capi.CompilationMessageBundle;

/**
 * Compilation messages related to XPath 1.0 expressions.
 */
public class XPathMessages extends CompilationMessageBundle {

  /** Invalid number of argument to function "{0}". */
  public CompilationMessage errInvalidNumberOfArguments(String functionName) {
    return super.formatCompilationMessage("Invalid number of argument to function \"{0}\".",
        functionName);
  }

  /** Attempt to use an unrecognized BPEL function "{0}". */
  public CompilationMessage errUnknownBpelFunction(String functionName) {
    return super.formatCompilationMessage(
        "Attempt to use an unrecognized BPEL function \"{0}\".", functionName);
  }

  /** The expression "{0}" must be a literal string. */
  public CompilationMessage errLiteralExpected(String text) {
    return super.formatCompilationMessage("The expression \"{0}\" must be a literal string.",
        text);
  }

  /** The prefix "{0}" on the XPath function "{1}" is not bound to a URI. */
  public CompilationMessage errUndeclaredFunctionPrefix(String prefix, String functionName) {
    return super.formatCompilationMessage("The prefix \"{0}\" on the XPath function \"{1}\""
        + " is not bound to a URI.", prefix, functionName);
  }

  /**
   * The prefixed name "{0}" could not be dereferenced in this namespace
   * context.
   */
  public CompilationMessage errInvalidQName(String qnameStr) {
    return super.formatCompilationMessage(
        "The prefixed name \"{0}\" could not be dereferenced in this namespace context.",
        qnameStr);
  }

  /** The string "{0}" is not a valid XPath 1.0 expression. */
  public CompilationMessage errXPathSyntax(String xPathString) {
    return super.formatCompilationMessage(
        "The string \"{0}\" is not a valid XPath 1.0 expression.", xPathString);
  }

  /** The XPath node with value "{0}" was not a text node. */
  public CompilationMessage errUnexpectedNodeTypeForXPath(String string) {
    return super.formatCompilationMessage(
        "The XPath node with value \"{0}\" was not a text node.", string);
  }

  /** Unexpected compilator error: {0} */
  public CompilationMessage errUnexpectedCompilationError(String string) {
    return super.formatCompilationMessage(
        "Unexpected compilation error: {0}", string);
  }

  /** bpws:xsltStylesheetNotFound the declared stylesheet could not be found: {0} */
  public CompilationMessage errXsltStylesheetNotFound(String string) {
    return super.formatCompilationMessage(
        "bpws:xsltStylesheetNotFound the declared stylesheet could not be found: {0}", string);
  }
}
