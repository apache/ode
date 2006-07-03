/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;


/**
 * Exception thrown to indicate that the requested port does not exist.
 */
public class NoSuchPortException extends MessageExchangeException {
  private static final long serialVersionUID = 1L;

  /**
   * Construct a new instance with the supplied information.
   *
   * @param port the name of the port
   */
  public NoSuchPortException(String port) {
    super("The port " + port.toString() + " does not exist");
  }
}
