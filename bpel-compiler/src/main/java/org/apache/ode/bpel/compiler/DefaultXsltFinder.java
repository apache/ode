package org.apache.ode.bpel.compiler;

import org.apache.ode.utils.StreamUtils;
import java.net.URI;
import java.io.IOException;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class DefaultXsltFinder implements XsltFinder {

  private static final Log __log = LogFactory.getLog(DefaultXsltFinder.class);

  private URI _base;

  public DefaultXsltFinder() {
    // no base URL
  }

  public DefaultXsltFinder(URI u) {
    setBaseURI(u);
  }

  public void setBaseURI(URI u) {
    File f = new File(u);
    if (f.exists() && f.isFile()) {
      _base = f.getParentFile().toURI();
    } else {
      _base = u;
    }
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
