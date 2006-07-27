/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.xsd;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

import java.io.IOException;
import java.net.URL;

/**
 * DefaultXMLEntityResolver - simple resolver that uses expandedURI
 */
class DefaultXMLEntityResolver implements XMLEntityResolver {

  public DefaultXMLEntityResolver() {
    super();
  }

  public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
    throws XNIException, IOException {
    XMLInputSource src = new XMLInputSource(resourceIdentifier);
    String expandedUri = resourceIdentifier.getExpandedSystemId();

    if (expandedUri != null) {
      src.setByteStream(new URL(expandedUri).openStream());
    }

    return src;
  }

}
