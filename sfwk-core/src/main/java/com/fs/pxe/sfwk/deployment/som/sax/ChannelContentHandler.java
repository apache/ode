/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import com.fs.pxe.sfwk.deployment.som.impl.ChannelImpl;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class ChannelContentHandler extends NestedContentHandler {

  private ChannelsContentHandler _cch;

  private ChannelImpl _c;
  private int _depth;
  
  public ChannelContentHandler(ChannelsContentHandler cch) {
    _cch = cch;
    _depth = 0;
  }

  Locator getLocator() {
    return _cch.getSystemDescriptorContentHandler().getLocator();
  }
  
  public void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) throws SAXException {

    ++_depth;
    if (_depth == 1) {
      _c = _cch.getSystemDescriptorContentHandler().getDescriptor().newChannel();
      _c.setLocator(getLocator());
      if (atts.getValue("name") != null) {
        _c.setName(atts.getValue("name"));
      }
      if (atts.getValue("uuid") != null) {
        _c.setUuid(atts.getValue("uuid"));
      }
      _cch.getSystemDescriptorContentHandler().getDescriptor().addChannel(_c);
    }
  }

  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
    --_depth;
  }
}
