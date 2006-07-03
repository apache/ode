/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.iapi;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;


/**
 * A representation of a WSDL-typed message. The implementation of this 
 * interface is provided by the integration layer. 
 * 
 * TODO: flush this out WRT Sybase requirements.
 * TODO: should we provide meta-data through this interface or will that
 *       put an undue burden on the integration layer?
 */
public interface Message {

  /**
   * Get the message type. 
   * @return message type.
   */
  QName getType();
  
  List<String> getParts();
  
  /**
   * Get a message part.
   * @param partName name of the part
   * @return named {@l
   */
  Content getPart(String partName);
   
  /**
   * Set the message part.
   * @param partName name of part
   * @param content part content
   */
  void setMessagePart(String partName, Content content);

  /**
   * Set the message as an element. The name of the element is irrelevant,
   * but it should have one child element for each message part.
   * TODO: remove this, temporary hack.
   */
  void setMessage(Element msg);

  /**
   * Get the message as an element. The returned element will have one 
   * child element corresponding (and named after) each part in the message.
   * TODO: remove this, temporary hack.
   */ 
  Element getMessage();
  
}
