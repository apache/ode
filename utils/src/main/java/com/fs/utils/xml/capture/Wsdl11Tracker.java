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
 * Tracker for WSDL 11 documents. This class extends the
 * {@link com.fs.utils.xml.capture.XmlSchemaTracker} class because a WSDL
 * document may have in-line schemas.
 */
public class Wsdl11Tracker extends XmlSchemaTracker {
  private static final Log __log = LogFactory.getLog(Wsdl11Tracker.class);

  public static final String NS = "http://schemas.xmlsoap.org/wsdl/";

  public static final String EL_IMPORT = "import";

  public static final String ATTR_LOCATION = "location";

  public static final String ATTR_NAMESPACE = "targetNamespace";

  public void startElement(String uri, String localName, String qName,
      Attributes atts) throws SAXException {
    if (uri != null && NS.equals(uri) && EL_IMPORT.equals(localName)) {

      // We prefer the "location" attribute, but if it is missing
      // we assume the namespace is a URL.
      String schemaLoc = atts.getValue(ATTR_LOCATION);
      if (schemaLoc == null)
        schemaLoc = atts.getValue(ATTR_NAMESPACE);

      __log.debug("found WSDL11 reference element " + uri + "@" + localName
          + "-->" + schemaLoc);
      addReference(schemaLoc);
    }

    super.startElement(uri, localName, qName, atts);
  }

}
