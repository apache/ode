/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.bapi.dao.MessageDAO;
import com.fs.pxe.sfwk.spi.Message;
import com.fs.pxe.sfwk.spi.MessageFormatException;
import com.fs.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.wsdl.Part;
import javax.xml.namespace.QName;
import java.util.*;


/**
 * Simple implementation of the {@link com.fs.pxe.sfwk.spi.Message} interface.
 */
class MessageImpl implements Message {

  private javax.wsdl.Message _messageDescription;

  /** Backing store for the message. */
  MessageDAO _dao;

  MessageImpl(javax.wsdl.Message messageDescription, MessageDAO messageStore) {
    assert messageDescription != null;
    _messageDescription = messageDescription;
    _dao = messageStore;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.Message#getDescription()
   */
  public javax.wsdl.Message getDescription() {
    return _messageDescription;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.Message#checkValid()
   */
  public void checkValid() throws MessageFormatException {
    Set<String> parts = new HashSet<String>();

    for(Iterator<String> iter = this.getParts().keySet().iterator(); iter.hasNext(); ) {
      String s = iter.next();

      if(parts.contains(s)) {
        throw new MessageFormatException("Message contains multiple parts of the same name (part=" + s + ")");
      }

      parts.add(s);
    }

    this.checkValidElements(parts);
  }


  /**
   * @see com.fs.pxe.sfwk.spi.Message#setMessage(org.w3c.dom.Element)
   */
  public void setMessage(Element msg) throws MessageFormatException {
    if (msg == null)
      throw new IllegalArgumentException("null message");

    checkValid(msg);

    NodeList nl = msg.getChildNodes();
    for (int i = 0; i < nl.getLength(); ++i) {
      Node next = nl.item(i);
      if (next.getNodeType() != Node.ELEMENT_NODE)
         continue;
      Element el = (Element) next;
      String partName = el.getNodeName();

      _dao.setPart(el.getNodeName(), isElementPartType(partName)
          ? getElementChild(el)
          : el);
    }
  }

  private Element getElementChild(Element el){
    NodeList nl = el.getChildNodes();
    for(int i = 0; i < nl.getLength(); ++i){
      Node n = nl.item(i);
      if(n.getNodeType() == Node.ELEMENT_NODE)
        return (Element)n;
    }
    throw new IllegalStateException("Element does not contain any children of type 'element'");
  }

  private boolean isElementPartType(String partName){
    return _messageDescription.getPart(partName).getElementName() != null;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.Message#getMessage()
   */
  public Element getMessage() {
    Document msg = DOMUtils.newDocument();
    Element msgEl = msg.createElementNS(PXE_MESSAGE_QNAME.getNamespaceURI(), PXE_MESSAGE_QNAME.getLocalPart());
    msg.appendChild(msgEl);

    for (Iterator i = _messageDescription.getParts().keySet().iterator(); i.hasNext(); ) {
      String partName = (String)i.next();
      Element partVal = _dao.getPart(partName);
      if (partVal != null){
        Node node = msg.importNode(partVal,true);
        if(isElementPartType(partName)){
          Element partElement = msg.createElementNS(null, partName);
          msgEl.appendChild(partElement);
          partElement.appendChild(node);
        }else{
          msgEl.appendChild(node);
        }
      }
    }
    return msgEl;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.Message#getPart(java.lang.String)
   */
  public Element getPart(String partName) {
    if (partName == null)
      throw new IllegalArgumentException("Null part name.");

    if (_messageDescription.getPart(partName) == null)
      throw new IllegalArgumentException("No such part: " + partName);

    return _dao.getPart(partName);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.Message#getParts()
   */
  public Map<String, Element> getParts() {
    Map<String, Element> retVal = new HashMap<String, Element>();
    for(Iterator i = _messageDescription.getParts().keySet().iterator();i.hasNext(); ) {
      String partName = (String)i.next();
      retVal.put(partName, _dao.getPart(partName));
    }
    return retVal;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.Message#setPart(java.lang.String, org.w3c.dom.Element)
   */
  public void setPart(String partName, Element part) throws MessageFormatException {
    if (partName  == null)
      throw new IllegalArgumentException("Null part name!");

    Part partDesc = _messageDescription.getPart(partName);
    if (partDesc == null)
      throw new MessageFormatException("Unknown part: " + partName);

    if(part == null)
      throw new MessageFormatException("Null part value for part " + partName);

    if(partDesc.getElementName() != null){
      QName element = new QName(part.getNamespaceURI(), part.getLocalName());
      if(!partDesc.getElementName().equals(element))
        throw new MessageFormatException("Invalid part element '" + element + "' for part '" + partName + "'");
    }
    _dao.setPart(partName, part);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.Message#setPart(java.lang.String, java.lang.String)
   */
  public void setPart(String partName, String partVal) throws MessageFormatException {
    Document doc = DOMUtils.newDocument();
    Element partEl = doc.createElementNS(null, partName);
    partEl.appendChild(doc.createTextNode(partVal));
    setPart(partName, partEl);
  }

  public void setFromEndpoint(Node endpointAddress) {
    if (endpointAddress != null) {
      Element partElmt = null;
      // Nasty but I rather that more than changing the whole MessageDAO usage
      if (endpointAddress.getNodeType() == Node.TEXT_NODE) {
        Document doc = DOMUtils.newDocument();
        partElmt = doc.createElement("wrapper-for-text");
        partElmt.appendChild(doc.importNode(endpointAddress, true));
      } else if (endpointAddress.getNodeType() == Node.ELEMENT_NODE) {
        partElmt = (Element) endpointAddress;
      }
      if (partElmt != null) _dao.setPart("fromEndpoint", partElmt);
    }
  }

  public Node getFromEndpoint() {
    Element partEl = _dao.getPart("fromEndpoint");
    if (partEl == null) return null;
    // Nasty but I rather that more than changing the whole MessageDAO usage
    if (partEl.getLocalName().equals("wrapper-for-text")) return partEl.getFirstChild();
    else return partEl;
  }

  public void setToEndpoint(Node endpointAddress) {
    if (endpointAddress != null) {
      Element partElmt = null;
      // Nasty but I rather that more than changing the whole MessageDAO usage
      if (endpointAddress.getNodeType() == Node.TEXT_NODE) {
        Document doc = DOMUtils.newDocument();
        partElmt = doc.createElement("wrapper-for-text");
        partElmt.appendChild(doc.importNode(endpointAddress, true));
      } else if (endpointAddress.getNodeType() == Node.ELEMENT_NODE) {
        partElmt = (Element) endpointAddress;
      }
      if (partElmt != null) _dao.setPart("toEndpoint", partElmt);
    }
  }

  public Node getToEndpoint() {
    Element partEl = _dao.getPart("toEndpoint");
    if (partEl == null) return null;
    // Nasty but I rather that more than changing the whole MessageDAO usage
    if (partEl.getLocalName().equals("wrapper-for-text")) return partEl.getFirstChild();
    else return partEl;
  }

  private void checkValid(Element msg) throws MessageFormatException {
    Set<String> parts = new HashSet<String>();

    // Invalid QName.
    QName qname = new QName(msg.getNamespaceURI(), msg.getLocalName());
    if (!qname.equals(Message.PXE_MESSAGE_QNAME)) {
      throw new MessageFormatException("Invalid root element; expected " + Message.PXE_MESSAGE_QNAME+ ", but got " + qname);
    }

    NodeList children = msg.getChildNodes();

    for (int i = 0; i < children.getLength(); ++i) {
      Node n = children.item(i);
      if (n.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }

      Element el = (Element)n;

      if(el.getNamespaceURI() != null) {
        throw new MessageFormatException("Invalid part " + el.getNodeName() + ": expected null namespace");
      }

      if(parts.contains(el.getNodeName())) {
        throw new MessageFormatException("Message contains multiple parts of the same name (part=" + el.getNodeName() + ")");
      }

      parts.add(el.getNodeName());
    }
  }

  @SuppressWarnings("unchecked")
  private void checkValidElements(Set<String> parts) throws MessageFormatException {
    Set<String> requiredParts = new HashSet<String>(_messageDescription.getParts().keySet());

    //  Not all elements present.
    if (!parts.containsAll(requiredParts)) {
      Set<String> missing = new HashSet<String>(requiredParts);
      missing.removeAll(requiredParts);
      throw new MessageFormatException("Message does not contain all required elements. Missing are:" + missing);
    }

    // Invalid elements present.
    if (!requiredParts.containsAll(parts)) {
      Set<String> extra = new HashSet<String>(parts);
      extra.removeAll(requiredParts);
      throw new MessageFormatException("Message contains extra (unrecognized) elements: " + extra);
    }
  }
}
