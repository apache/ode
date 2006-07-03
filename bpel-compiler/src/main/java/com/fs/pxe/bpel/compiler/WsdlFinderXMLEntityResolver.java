/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

public class WsdlFinderXMLEntityResolver implements XMLEntityResolver {

  private static final Log __log = LogFactory
      .getLog(WsdlFinderXMLEntityResolver.class);

  /**
   * Flag indicating whether the resolver should fail with an exception if the
   * requested resource is not found. The interface suggests that null should be
   * returned so that a "default" resolution mechanism can be used; however, we
   * don't necessarily want to use this default mechanism.
   */
  private boolean _failIfNotFound = true;

  private WsdlFinder _wsdlFinder;

  public WsdlFinderXMLEntityResolver(WsdlFinder finder) {
    _wsdlFinder = finder;
  }

  public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
      throws XNIException, IOException {

    if (__log.isDebugEnabled())
      __log.debug("resolveEntity(" + resourceIdentifier + ")");

    XMLInputSource src = new XMLInputSource(resourceIdentifier);
    URI location;

    try {
      if (resourceIdentifier.getExpandedSystemId() == null)
        location = new URI(resourceIdentifier.getNamespace());
      else
        location = new URI(resourceIdentifier.getExpandedSystemId());
    } catch (URISyntaxException e) {
      __log.debug("resolveEntity: URI syntax error", e);
      throw new IOException(e.getMessage());
    }

    if (__log.isDebugEnabled())
      __log.debug("resolveEntity: Expecting to find " + resourceIdentifier.getNamespace()
          + " at " + location);
    
    try {
      src.setByteStream(_wsdlFinder.openResource(location));
    } catch (IOException ioex) { 
      __log.debug("resolveEntity: IOExcepption opening " + location,ioex);
      
      if (_failIfNotFound) {
        __log.debug("resolveEntity: failIfNotFound set, rethrowing...");
        throw ioex;
      }

      __log.debug("resolveEntity: failIfNotFound NOT set, returning NULL");
      return null;
    }

    return src;
  }

}
