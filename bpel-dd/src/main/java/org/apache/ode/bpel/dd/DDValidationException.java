package org.apache.ode.bpel.dd;

/**
 * Thrown when the deployment descriptor can't be validated.
 */
public class DDValidationException extends DDException {

  public DDValidationException(String message) {
    super(message);
  }

  public DDValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public DDValidationException(Throwable cause) {
    super(cause);
  }
}
