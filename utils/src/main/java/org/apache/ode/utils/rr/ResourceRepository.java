/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.rr;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;


/**
 * <p>
 * A store for byte streams keyed to URIs.  Implementations may back the repository
 * with various modes of storage, e.g., file, database, cache, or &quot;live&quot;
 * connections, although the expectation is that the store is static, i.e., a
 * resource obtained from an instance will be the same no matter when/how the
 * lookup is performed.
 * </p>
 */
public interface ResourceRepository extends Closeable {

  /**
   * <p>
   * Look up a resource as a URL.  The <code>URL</code> object that is returned is
   * not the original URL but a URL to the internal version of the resource.  For
   * example, if an implementation is backed by files on disk, the URL will point to
   * the file containing the resource.
   * </p>
   * @param uri the URI to look up.
   * @return a <code>URL</code> that can be dereferenced to obtain the resource,
   *         or <code>null</code> if the URI cannot be resolved. 
   */
  public URL resolveURI(URI uri);

  boolean containsResource(URI uri);

  InputStream resourceAsStream(URI uri) throws IOException;

  void close() throws IOException;

}
