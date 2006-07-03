package com.fs.pxe.bpel.iapi;

/**
 * Exception thrown by the integration layer. 
 *
 */
public class ContextException extends RuntimeException {

  public ContextException() {
    super();
  }
  public ContextException(String string, Exception ex) {
    super(string,ex);
  }

}
