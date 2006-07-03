/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

import javax.xml.namespace.QName;


/**
 * Exception thrown to indicate that the request operation does not exist on
 * the given port.
 *
 * @todo Rename this to NoSuchMessageExchange for consistency
 */
public class NoSuchOperationException extends MessageExchangeException {
  /**
   * Create a new instance with the port and operation supplied.
   *
   * @param port the {@link QName} of the port
   * @param op the name of the (supposed) operation
   */
  public NoSuchOperationException(QName port, String op) {
    // TODO: Replace this with something from the message bundle.
    super("the operation " + op + " does not exist on the port "
          + port.toString());
  }
}
