package com.fs.pxe.bpel.dd;

/**
 * Thrown when a deployment descriptor can't be processed properly.
 */
public class DDException extends Exception {

  public DDException(String message) {
    super(message);
  }

  public DDException(String message, Throwable cause) {
    super(message, cause);
  }

  public DDException(Throwable cause) {
    super(cause);
  }
}
