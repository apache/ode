/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

class ChannelsContentHandler extends NestedContentHandler {

  private SystemDescriptorContentHandler _sdch;

  private int _depth;
  private ContentHandler _target;
  
  public ChannelsContentHandler(SystemDescriptorContentHandler sdch) {
    _sdch = sdch;
    _depth = 0;
  }

  public SystemDescriptorContentHandler getSystemDescriptorContentHandler(){
    return _sdch;
  }
  
  public void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) throws SAXException {

    ++_depth;
    if (_depth == 1) {
      _target = new ChannelContentHandler(this);
    }
    _target.startElement(namespaceURI,localName,qName,atts);
  }

  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
      _target.endElement(namespaceURI,localName,qName);
    --_depth;
  }
 
}
