/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.utils.rr;

import java.net.URI;
import java.net.URISyntaxException;

import javax.wsdl.xml.WSDLLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

/**
 * Base class providing basic "import" functionality required in
 * a {@link javax.wsdl.xml.WSDLLocator} object.
 */
abstract class ImportingWsdlLocator implements WSDLLocator {
  private static Log __log = LogFactory.getLog(ImportingWsdlLocator.class);

  private URI _baseURI;
  private URI _latestImportUri;

  ImportingWsdlLocator(URI baseURI) {
    if (baseURI == null)
      throw new IllegalArgumentException("baseURI must not be null!");

    _baseURI = baseURI;
  }

  /**
   * Implementation of {@link WSDLLocator#getBaseInputSource} that
   * simply defers to {@link #resolveURI(String, String, java.net.URI)} )}.
   * @return an {@link InputSource} to the <em>base</em> WSDL object
   */
  public InputSource getBaseInputSource() {
    // Note: WSDL4J 1.5.1 will not check for a null result, so if
    // a resource is not found, NullPointerException will occur.
    return resolveURI(null, _baseURI.toASCIIString(), _baseURI);
  }

  /**
   * @see WSDLLocator#getBaseURI
   */
  public String getBaseURI() {
    return _baseURI.toASCIIString();
  }

  /**
   * Implementation of {@link WSDLLocator#getImportInputSource} that
   * defers to {@link #resolveURI} after having first attempted
   * to compute an absolute URL for the requested URI. The absolute
   * URL is derived either from the requested URI (if it is a valid
   * URL) or from a concatenation of the URI of the requesting WSDL
   * object (sans any characters after the last '/') and the
   * (relative) URI of the requested WSDL object.
   * @param context the URI of the WSDL object requesting a
   *                             URI
   * @param location the (possibly relative) URI of the requested WSDL
   *                     object
   * @return an {@link InputSource} to the requested WSDL object
   */
  public InputSource getImportInputSource(String context, String location) {
    //String uri = context + location;
    // check if location is a full URL in it'context own right

    URI loc;
    try {
       loc = new URI(location);
    } catch (URISyntaxException e) {
      __log.error("Invalid location URI " + location, e);
      return null;
    }

    if (!loc.isAbsolute())
      try {
        loc = new URI(context).resolve(location);
      } catch (URISyntaxException e) {
        __log.error("Invalid context URI " + context,e);
        return null;
      }

    _latestImportUri = loc;
    return resolveURI(context, location, loc);
  }

  public String getLatestImportURI() {
    return _latestImportUri == null ? null : _latestImportUri.toASCIIString();
  }

  /**
   * Abstract method that does the actual resolving of URIs to the
   * corresponding {@link InputSource} objects. This mapping may be
   * completely transparrent (i.e. through the URL mechanism) or it
   * may rely on some sort of caching or URI re-writing scheme.
   * @param requestingURI the URI of the WSDL document that requested
   *                      (through an <code>import</code>) some URI
   *                      or <code>null</code> if this is a request
   *                      for the <em>base</em> WSDL.
   * @param requestedURI the (possibly relative) URI of the requested
   *                     document
   * @param uri the <code>requestedURI</code> only if it is a true
   *            absolute URI, otherwise a URI computed from the
   *            <code>requestingURI</code> and the <em>relative</em>
   *            <code>requestedURI</code>
   * @return {@link InputSource} for the requested URI, or
             <code>null</code> if the URI cannot be resolved.
   */
  public abstract InputSource resolveURI(String requestingURI, String requestedURI, URI uri);
}
