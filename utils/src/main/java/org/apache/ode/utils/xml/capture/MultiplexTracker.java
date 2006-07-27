/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.xml.capture;

import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * A multiplexing content handler then forwards each SAX event to an
 * arbitraty number of {@link Tracker}s.
 */
class MultiplexTracker implements ContentHandler {
  private Set<Tracker> contentHandlers_;

  /**
   * Constructor.
   * @param handlers {@link Set} of {@link Tracker}s to forward events to
   */
  MultiplexTracker(Set<Tracker> handlers) {
    contentHandlers_ = handlers;
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().characters(ch, start, length);
  }

  public void endDocument() throws SAXException {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().endDocument();
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().endElement(uri, localName, qName);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().endPrefixMapping(prefix);
  }

  public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().ignorableWhitespace(ch, start, length);
  }

  public void processingInstruction(String target, String data) throws SAXException {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().processingInstruction(target, data);
  }

  public void setDocumentLocator(Locator locator) {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().setDocumentLocator(locator);
  }

  public void skippedEntity(String name) throws SAXException {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().skippedEntity(name);
  }

  public void startDocument() throws SAXException {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().startDocument();
  }

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().startElement(uri, localName, qName, atts);
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().startPrefixMapping(prefix, uri);
  }

  void init(URI uri, Set<URI> references) {
    for (Iterator<Tracker> i = contentHandlers_.iterator(); i.hasNext(); )
      i.next().init(uri, references);
  }
}
