/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.explang;

/**
 * An expression evaluation exception.
 */
public class EvaluationException extends Exception {

  public EvaluationException(String message, Exception e) {
    super(message, e);
  }

}
