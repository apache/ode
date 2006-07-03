/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.xml.capture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * {@link Tracker} for XML schema imports / includes.
 */
public class XmlSchemaTracker extends Tracker {
  private static final Log __log = LogFactory.getLog(XmlSchemaTracker.class);
  
  private static final String NS="http://www.w3.org/2001/XMLSchema" ;

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if (uri != null && uri.equals(NS) && (localName.equals("import") || localName.equals("include"))) {
      String loc = atts.getValue("schemaLocation");
      // If we don't specify a location, we assume that the namespace
      // is also the location.
      if (loc == null)
        loc = atts.getValue("namespace");
      
      __log.debug("found reference element " + uri + "@" + localName + "-->" +loc);
      
      addReference(loc);
    }
  }
}
