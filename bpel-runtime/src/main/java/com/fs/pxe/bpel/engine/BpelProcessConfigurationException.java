/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.engine;

/**
 * Exception indicating a problem with the BPEL process configuration.
 */
class BpelProcessConfigurationException extends Exception {
  public BpelProcessConfigurationException(String s) {
    super(s);
  }
}
