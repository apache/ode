/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

import javax.xml.namespace.QName;


/**
 * Representation of a message-driven event handler.
 */
public interface OnEvent extends OnMessage, BpelObject {

  /**
   * Set the (message) type of the local variable to be used for the incoming
   * message body.
   * @param q the QName of the WSDL message.
   */
  void setMessageType(QName q);
  
  /**
   * Set the element type of the local variable to be used for the incoming 
   * message body. 
   * @param q the QName of the element.
   */
  void setElement(QName q);
  
  /**
   * @return the WSDL message type to be used for the incoming message body or
   * <code>null</code> if an element type is to be used instead.
   * @see #getElement()
   */
  QName getMessageType();
  
  /**
   * @return the element type to be used for the incoming message body or
   * <code>null</code> if a WSDL message type is to be used instead.
   * @see #getMessageType()
   */
  QName getElement();
  
}