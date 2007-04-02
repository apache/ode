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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.compiler.api.SourceLocation;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;


/**
 * Reports errors that occured during Xsl sheets processing. This implementation isn't
 * built to be thread safe in case multiple compilations occur parrallely, however
 * this shouldn't occur.
 */
public class XslCompilationErrorListener implements ErrorListener {

  private static final Log __log = LogFactory.getLog(XslCompilationErrorListener.class);
  private CompilerContext _cc;

  public XslCompilationErrorListener(CompilerContext cc) {
    _cc = cc;
  }

  public void warning(TransformerException exception) throws TransformerException {
    if (__log.isWarnEnabled()) {
      __log.warn(exception);
    }
    recover(CompilationMessage.WARN, exception);
  }

  public void error(TransformerException exception) throws TransformerException {
    if (__log.isErrorEnabled()) {
      __log.error(exception);
    }
    recover(CompilationMessage.ERROR, exception);
    throw exception;
  }

  public void fatalError(TransformerException exception) throws TransformerException {
    if (__log.isFatalEnabled()) {
      __log.fatal(exception);
    }
    recover(CompilationMessage.ERROR, exception);
    throw exception;
  }

  // If somebody has a better idea to handle errors thrown by the XSL engine I'm
  // really, really, REALLY open to suggestions.
  private void recover(short severity, TransformerException exception) {
    CompilationMessage cmsg = new CompilationMessage();
    cmsg.severity = severity;
    cmsg.code = "parseXsl";
    cmsg.phase = 0;
    cmsg.messageText = exception.getMessageAndLocation();
    CompilationException ce = new CompilationException(cmsg, exception);
    SourceLocation loc = exception.getLocator() != null ? new SourceLocatorWrapper(exception.getLocator()) : null;
      if (_cc != null)
        _cc.recoveredFromError(loc,ce);
      else
      __log.error("XSL stylesheet parsing error! ", exception);
  }
}
