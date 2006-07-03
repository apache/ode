/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.bapi.dao;

import org.w3c.dom.Element;


/**
 * Data access object for messages (a message refers to the content of a WSDL
 * message represented as a DOM element).
 */
public interface MessageDAO {

  /**
   * Get the {@link MessageExchangeDAO} to which this message belongs.
   * @return associated {@link MessageExchangeDAO} 
   */
  MessageExchangeDAO getMessageExchange();

  /**
   * Sets the data content as a DOM element.
   *
   * @param partName part name
   * @param value value of the part
   */
  void setPart(String partName, Element value);


  /**
   * Get the data content as a DOM element.
   *
   * @return element content of the WSDL message represented as a DOM element
   */
  Element getPart(String partName);

}
