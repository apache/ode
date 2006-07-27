/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

/**
 * A run-time exception indicating that the process is invalid. This should not
 * normally occur: that is, a process should be found to be invalid before it is
 * executed. Therefore, this can be viewed as a rather rare and serious condition.
 */
public class InvalidProcessException extends RuntimeException {
    private static final long serialVersionUID = 9184731070635430159L;
  
  public final static int DEFAULT_CAUSE_CODE = 0;
  public final static int RETIRED_CAUSE_CODE = 1;
  
  private final int causeCode;
  
  public InvalidProcessException(String msg, Throwable cause) {
    super(msg,cause);
    this.causeCode = DEFAULT_CAUSE_CODE;
  }

  public InvalidProcessException(String msg) {
    super(msg);
    this.causeCode = DEFAULT_CAUSE_CODE;
  }

  public InvalidProcessException(Exception cause) {
    super(cause);
    this.causeCode = DEFAULT_CAUSE_CODE;
  }

  /**
   * @param causeCode
   */
  public InvalidProcessException(final int causeCode) {
    super();
    this.causeCode = causeCode;
  }
 
  /**
   * @param message
   * @param causeCode
   */
  public InvalidProcessException(String message, final int causeCode) {
    super(message);
    this.causeCode = causeCode;
  }

  /**
   * @return the cause code
   */
  public int getCauseCode() {
    return causeCode;
  }
}
