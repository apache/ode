/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class ServicesContentHandler extends NestedContentHandler {

  private SystemDescriptorContentHandler _sdch;
  private int _depth;
  
  private ServiceContentHandler _sch;
  
  
  public ServicesContentHandler(SystemDescriptorContentHandler sdch) {
    _sdch = sdch;
    _depth = 0;
  }
  
  public SystemDescriptorContentHandler getSystemDescriptorContentHandler() {
    return _sdch;
  }
  
  Locator getLocator() {
    return _sdch.getLocator();
  }
  
  public void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) throws SAXException {
    ++_depth;
    if (_depth == 1) {
      _sch = new ServiceContentHandler(this);
    }
    _sch.startElement(namespaceURI,localName,qName,atts);
  }

  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
    _sch.endElement(namespaceURI,localName,qName);
    --_depth;
    if (_depth == 0) {
      _sdch.getDescriptor().addService(_sch.getService());
    }
  }
}
