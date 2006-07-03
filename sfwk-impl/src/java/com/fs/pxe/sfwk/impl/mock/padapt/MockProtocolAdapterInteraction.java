/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */



/*
 * Created on Mar 17, 2004
 *
 */
package com.fs.pxe.sfwk.impl.mock.padapt;

import com.fs.pxe.sfwk.spi.MessageExchangeException;
import com.fs.pxe.sfwk.spi.ServiceProviderException;


/**
 * Invocation API for MockProtocolAdapter. Allows us to introduce messages into
 * the PXE domain.
 */
public interface MockProtocolAdapterInteraction {

  /**
   * Set the target service.
   * @param serviceName name of the target service
   */
  public void setTargetService(String serviceName);

  /**
   * Sends a message for receipt by "receive", return element is response.
   *
   * @param op
   * @param msg
   *
   * @return response message
   *
   * @throws Exception
   */
  public String sendMessage(String port, String op, String msg, long timeout)
                      throws MessageExchangeException, ServiceProviderException, Exception;
  
  /**
   * Backwards compatability.  Assumes port is this first port as defined by descriptor, timeout is 60 seconds. 
   * @param op
   * @param msg
   * @return
   * @throws MessageExchangeException
   * @throws ServiceProviderException
   * @throws Exception
   */
  public String sendMessage(String op, String msg)
    throws MessageExchangeException, ServiceProviderException, Exception;
}
                                                  