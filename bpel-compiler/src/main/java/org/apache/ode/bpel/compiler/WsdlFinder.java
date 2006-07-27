/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.wsdl.Definition4BPEL;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;

/**
 * Simple wrapper for WSDL location.
 */
public interface WsdlFinder {
  
  /**
   * Set the base URL to compose relative URLs against.
   * @param base the base URL to resolve against or <code>null</code> if none exists.
   */
  void setBaseURI(URI base);
  
  /**
   * Resolve a URI to a definition with BPEL-specific additions.
   * @param f the WSDLReader to use.
   * @param uri the URI of the definition
   * @return the definition
   * @throws WSDLException if one occurs while reading the WSDL or its imports.
   */
  Definition4BPEL loadDefinition(WSDLReader f, URI uri) throws WSDLException;

  InputStream openResource(URI uri) throws MalformedURLException, IOException;
}

