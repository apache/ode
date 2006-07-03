/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Map;


/**
 * An inter-service message.
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface Message {
  /** DOCUMENTME */
  public static final QName PXE_MESSAGE_QNAME = new QName("", "message");

  /**
   * Get the WSDL description of this message.
   *
   * @return WSDL description element
   */
  javax.wsdl.Message getDescription();

  /**
   * Determine if the message is valid, that it conforms to the message
   * schema.  Throws an exception with explanation if invalid. 
   */
   void checkValid() throws MessageFormatException;


  /**
   * Sets the message data. The input element must conform or partially conform
   * to the message schema. A message is said to be partially conformant if
   * it is missing some required parts, but is otherwise a valid message.
   *
   * @param e message element.
   */
  void setMessage(Element e) throws MessageFormatException;

  /**
   * Return the message data.
   *
   * @return message root element.
   */
  Element getMessage();

  /**
   * Get the element representing the WSDL 1.1 message part. <em>Note, for
   * element parts this will simply return the part element, while for typed
   * parts this will return a wrapper element whose element name will be the
   * same as the part name, and whose namespace will be null.</em>
   *
   * @param partName name of the part
   *
   * @return part element representing the part data
   */
  Element getPart(String partName);

  /**
   * Get all the parts, in {@link Map} form.
   * @return {@link Map} from part name to part element.
   */
  Map<String, Element> getParts();

  /**
   * Set the element representing the WSDL 1.1 message part.
   * @param partName message part name
   * @param partData message part data
   */
  void setPart(String partName, Element partData) throws MessageFormatException ;

  void setPart(String partName, String partData) throws MessageFormatException;

  /**
   * Set the 'from' endpoint when it's present in the incoming message.
   * @param endpointAddress
   */
  public void setFromEndpoint(Node endpointAddress);

  public Node getFromEndpoint();

  /**
   * Set the 'to' endpoint when it's present in the incoming message.
   * @param endpointAddress
   */
  public void setToEndpoint(Node endpointAddress);

  public Node getToEndpoint();
}
