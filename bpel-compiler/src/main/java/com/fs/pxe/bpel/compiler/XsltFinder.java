package com.fs.pxe.bpel.compiler;

import java.net.URI;

/**
 * Simple wrapper for XSLT location.
 */
public interface XsltFinder {

  /**
   * Set the base URL to compose relative URLs against.
   * @param base the base URL to resolve against or <code>null</code> if none exists.
   */
  public void setBaseURI(URI base);

  /**
   * Resolve a URI to a XSLT sheet.
   * @param uri of the xslt sheet.
   * @return the sheet content
   */
  public String loadXsltSheet(URI uri);
}
