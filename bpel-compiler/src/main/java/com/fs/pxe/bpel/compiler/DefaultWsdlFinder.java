/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.wsdl.Definition4BPEL;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;


class DefaultWsdlFinder implements WsdlFinder {
  
  private URI _base;
  
  public DefaultWsdlFinder() {
    // no base URL
  }
  
  public DefaultWsdlFinder(URI u) {
    _base = u;
  }
  
  public void setBaseURI(URI u) {
    _base = u;
  }
 
  public Definition4BPEL loadDefinition(WSDLReader r, URI uri) throws WSDLException {
    return (Definition4BPEL) r.readWSDL(
        (_base == null?null:(_base.toASCIIString())),
        uri.toASCIIString());
  }

	public InputStream openResource(URI uri) throws MalformedURLException, IOException {
		return uri.toURL().openStream();
	}
  
  
}
