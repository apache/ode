/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

abstract class NestedContentHandler implements ContentHandler {

  public void setDocumentLocator(Locator locator) {
    // ignore
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#startDocument()
   */
  public void startDocument() throws SAXException {
    // Ignore
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#endDocument()
   */
  public void endDocument() throws SAXException {
    // Ignore
  }
  
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    // ignore
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    // ignore
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    // ignore
  }

  public void ignorableWhitespace(char[] ch, int start, int length)
      throws SAXException {
    // ignore
  }

  public void processingInstruction(String target, String data)
      throws SAXException {
    // ignore
  }

  public void skippedEntity(String name) throws SAXException {
    // ignore
  }

}
