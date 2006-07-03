package com.fs.pxe.tools.bpelc;

import com.fs.pxe.sfwk.rr.ResourceRepository;
import com.fs.pxe.bpel.compiler.XsltFinder;
import com.fs.utils.StreamUtils;

import java.net.URI;
import java.net.URL;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the {@link XsltFinder} interface that obtains WSDLs from
 * inside a {@link com.fs.pxe.sfwk.rr.ResourceRepository} object.
 */
public class RrXsltFinder implements XsltFinder {

  private static final Log __log = LogFactory.getLog(RrXsltFinder.class);
  private ResourceRepository _rr;
  private URI _base;

  public RrXsltFinder(ResourceRepository rr, URI base) {
    _rr = rr;
    _base = base;
  }

  public void setBaseURI(URI base) {
    _base = base;
  }

  public String loadXsltSheet(URI uri) {
    URL url = _rr.resolveURI(resolve(uri));
    if (url == null) {
      if (__log.isDebugEnabled())
        __log.debug("Resource repository does not contain resource: " + url);
      return null;
    }
    try {
      return new String(StreamUtils.read(url));
    } catch (IOException e) {
      if (__log.isDebugEnabled())
        __log.debug("error obtaining resource '" + uri + "' from repository.", e);
      return null;
    }
  }

  /**
   * Resolve the URI relative to the base.
   * @param uri
   * @return uri
   */
  private URI resolve(URI uri) {
    if (_base == null)
      return uri;
    return _base.resolve(uri);
  }

}
