package com.fs.pxe.bpel.xsl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import com.fs.pxe.bpel.capi.CompilerContext;
import com.fs.pxe.bpel.capi.CompilationException;
import com.fs.pxe.bpel.capi.CompilationMessage;

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
    _cc.recoveredFromError(XslTransformHandler.getInstance(), ce);
  }
}
