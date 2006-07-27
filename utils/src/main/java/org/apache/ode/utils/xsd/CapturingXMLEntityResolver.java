/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.xsd;

import org.apache.ode.utils.StreamUtils;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A wrapper for {@link XMLEntityResolver}s that defers resolution to the
 * wrapped object and saves the byte stream of each resolved entity in a map.
 * The purpose of this object is to provide a mechanism for capturing the
 * "whole" of a schema document (including imports and other dependencies).
 */
public class CapturingXMLEntityResolver implements XMLEntityResolver {
  private static final Log __log = LogFactory
      .getLog(CapturingXMLEntityResolver.class);

  private XMLEntityResolver _resolver;

  private Map<URI, byte[]> _capture;

  public CapturingXMLEntityResolver(Map<URI, byte[]> capture,
      XMLEntityResolver resolver) {
    _resolver = resolver;
    _capture = capture;
  }

  public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
      throws XNIException, IOException {
    if (__log.isTraceEnabled()) {
      StringBuffer buf = new StringBuffer("resolveEntity: base=");
      buf.append(resourceIdentifier.getBaseSystemId());
      buf.append(", literal=");
      buf.append(resourceIdentifier.getLiteralSystemId());
      buf.append(", expanded=");
      buf.append(resourceIdentifier.getExpandedSystemId());
      buf.append(", ns=");
      buf.append(resourceIdentifier.getNamespace());
      buf.append(", publicId=");
      buf.append(resourceIdentifier.getPublicId());
      __log.trace(buf.toString());
    }

    XMLInputSource src = _resolver.resolveEntity(resourceIdentifier);
    InputStream is = src.getByteStream();
    
    if (is == null) {
      __log.debug("resolveEntity: stream not available for: " + src);
      throw new IOException("Unable to locate resource for namespace " + resourceIdentifier.getNamespace());
    }
    
    byte[] data;
    try {
      data = StreamUtils.read(is);
    } finally {
      is.close();
    }

    try {
      URI systemId = new URI(
          resourceIdentifier.getExpandedSystemId() == null ? resourceIdentifier
              .getNamespace() : resourceIdentifier.getExpandedSystemId());

      _capture.put(systemId, data);
    } catch (URISyntaxException use) {
      __log.error("Invalid URI: " + resourceIdentifier.getExpandedSystemId());
      throw new XNIException(use);
    }
    

    // re-create the InputSource since reading exhausted the XML stream
    return _resolver.resolveEntity(resourceIdentifier);
  }
}
