package com.fs.pxe.bpel.compiler;

import com.fs.utils.StreamUtils;
import java.net.URI;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class DefaultXsltFinder implements XsltFinder {

  private static final Log __log = LogFactory.getLog(DefaultXsltFinder.class);

  private URI _base;

  public DefaultXsltFinder() {
    // no base URL
  }

  public DefaultXsltFinder(URI u) {
    _base = u;
  }

  public void setBaseURI(URI u) {
    _base = u;
  }

  public String loadXsltSheet(URI uri) {
    try {
      return new String(StreamUtils.read(_base.resolve(uri).toURL()));
    } catch (IOException e) {
      if (__log.isDebugEnabled())
        __log.debug("error obtaining resource '" + uri + "' from repository.", e);
      return null;
    }
  }

}
