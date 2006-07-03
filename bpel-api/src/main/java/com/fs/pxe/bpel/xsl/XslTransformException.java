package com.fs.pxe.bpel.xsl;

/**
 * Thrown when an XSL sheet wasn't parsed property or when the transformation fails.
 */
public class XslTransformException extends RuntimeException {

  public XslTransformException(String message) {
    super(message);
  }

  public XslTransformException(String message, Throwable cause) {
    super(message, cause);
  }

  public XslTransformException(Throwable cause) {
    super(cause);
  }
}
