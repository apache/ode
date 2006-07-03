/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import com.fs.pxe.sfwk.deployment.som.Port;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

abstract class PortsContentHandler extends NestedContentHandler {

  private int _depth;
  protected ServiceContentHandler _sch;
  private ContentHandler _target;
  
  public PortsContentHandler(ServiceContentHandler sch) {
    _sch = sch;
    _depth = 0;
  }
  
  Locator getLocator() {
    return _sch.getLocator();
  }
  
  public abstract void addPort(Port p);
  
  public ServiceContentHandler getServiceContentHandler() {
    return _sch;
  }
  
  public void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) throws SAXException {
    ++_depth;
    if (_depth == 1) {
      _target = new PortContentHandler(this);
    }
    _target.startElement(namespaceURI, localName,qName,atts);
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
    --_depth;
    _target.endElement(namespaceURI, localName, qName);
  }
}
