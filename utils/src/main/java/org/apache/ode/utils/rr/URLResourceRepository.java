/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.rr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;


/**
 * {@link ResourceRepository} implementation that uses a base URL and
 * a {@link Properties} file to locate resources.
 */
public class URLResourceRepository implements ResourceRepository {
  static final String RR_PROPERTIES = "rr.properties";

  private final Map<InputStream, Object> _streams = new WeakHashMap<InputStream, Object>();
  private final Properties _mappings = new Properties();
  private boolean _open = false;
  private URI _baseURI;

  public URLResourceRepository(URI baseURL) throws ResourceRepositoryException {
    _baseURI = baseURL;

    URI rrproperties = _baseURI.resolve(RR_PROPERTIES);
    InputStream is = null;
    try {
      is = rrproperties.toURL().openStream();
      _mappings.load(is);
      _open = true;
    }
    catch (IOException e) {
      throw new ResourceRepositoryException("error opening URL resource repository at " + baseURL, e);
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) {
          // ignore.
        }
      }
    }
  }

  protected void finalize() throws Throwable {
    this.close();
    super.finalize();
  }

  public synchronized void close() throws IOException {
    // close all registered and not-yet-GC'ed streams
    for (InputStream s : _streams.keySet()) {
      s.close();
    }
    _streams.clear();
    _open = false;
  }

  public URL resolveURI(URI uri) {
    this.checkOpen();

    String resource = _mappings.getProperty(uri.toASCIIString());
    if (resource == null) {
      return null;
    }

    try {
      URI resolved = _baseURI.resolve(resource);
      return resolved.toURL();
    } catch (IOException e) {
      return null;
    }
  }

  public InputStream resourceAsStream(URI uri) throws IOException {
    this.checkOpen();

    // check resource availability
    URL url = this.resolveURI(uri);
    if (url == null) {
      return null;
    }

    // if it exists, open a stream for reading
    InputStream stream = url.openStream();

    // ...and register the stream for later closing
    synchronized (_streams) {
      _streams.put(stream, null);
    }

    return stream;
  }

  public boolean containsResource(URI uri) {
    this.checkOpen();
    return _mappings.containsKey(uri.toASCIIString());
  }
  
  public URI getBaseURL() {
    this.checkOpen();
    return _baseURI;
  }

  public Map<URI, String> getTableOfContents() {
    this.checkOpen();

    HashMap<URI, String> ret = new HashMap<URI, String>();
  	for (Iterator<Entry<Object, Object>> i = _mappings.entrySet().iterator();i.hasNext();) {
  		Map.Entry me = i.next();
  		URI uri;
			try {
				uri = new URI((String)me.getKey());
			} catch (URISyntaxException ex) {
				// This is rather unexpected. 
				throw new IllegalStateException(ex);
			}
  		ret.put(uri, (String)me.getValue());
  	}
  	return ret;
  }

  public String toString() {
    return "{RR on " + _baseURI + "}";
  }

  private void checkOpen() {
    if (!_open) {
      throw new IllegalStateException("ResourceRepository is not opened!");
    }
  }

}
