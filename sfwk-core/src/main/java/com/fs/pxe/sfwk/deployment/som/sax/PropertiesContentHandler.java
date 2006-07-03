/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class PropertiesContentHandler extends NestedContentHandler {

  private PropertyTarget _pt;
  
  public PropertiesContentHandler(PropertyTarget pt) {
    _pt = pt;
  }
  
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
      throws SAXException {
    _pt.addProperty(atts.getValue("name"), atts.getValue("value"));
  }

  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
    // ignore
  }
}
