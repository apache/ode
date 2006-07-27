/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.rr;

import org.apache.ode.utils.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;


/**
 * WsdlLocator that defers to the {@link ResourceRepository} for its
 * resources.
 */
public class RepositoryWsdlLocator extends ImportingWsdlLocator {
  private static Log __log = LogFactory.getLog(RepositoryWsdlLocator.class);

  private ResourceRepository _repository;

  public RepositoryWsdlLocator(ResourceRepository rr, URI baseUri) {
    super(baseUri);
    _repository = rr;
  }

  /* (non-Javadoc)
   * @see org.apache.ode.utils.wsdl.ImportingWsdlLocator#resolveURI(java.lang.String, java.lang.String, java.lang.String)
   */
  public InputSource resolveURI(String requestingURI, String requestedURI, URI uri) {
    try {
      URL url = _repository.resolveURI(uri);

      if (url == null) {
        if (__log.isDebugEnabled())
          __log.debug("resource repository does not contain resource: " + url);
        return null;
      }

      InputSource source = new InputSource(uri.toASCIIString());
      // we read the URL's contents into memory in order to avoid
      // problems with lingering file handles.
      source.setByteStream(new ByteArrayInputStream(StreamUtils.read(url)));
      return source;
    }
    catch (IOException e) {
      if (__log.isDebugEnabled())
        __log.debug("error obtaining resource '" + uri + "' from repository.", e);
      return null;
    }
  }

}
