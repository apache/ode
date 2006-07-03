/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * Base interface for objects that can turn themselves into a stream of SAX events.
 */
public interface Marshallable {
  
  public void toSaxEvents(ContentHandler ch) throws SAXException;
}
