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
package org.apache.ode.utils.xsd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.StreamUtils;
import org.apache.ode.utils.fs.FileUtils;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * A wrapper for {@link XMLEntityResolver}s that defers resolution to the
 * wrapped object and saves the byte stream of each resolved entity in a map.
 * The purpose of this object is to provide a mechanism for capturing the
 * "whole" of a schema document (including imports and other dependencies).
 */
public class CapturingXMLEntityResolver implements XMLEntityResolver {
  private static final Log __log = LogFactory
      .getLog(CapturingXMLEntityResolver.class);

  private XMLEntityResolver _resolver;

  private Map<URI, byte[]> _capture;

  public CapturingXMLEntityResolver(Map<URI, byte[]> capture,
      XMLEntityResolver resolver) {
    _resolver = resolver;
    _capture = capture;
  }

  public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
      throws XNIException, IOException {
    if (__log.isDebugEnabled()) {
      StringBuffer buf = new StringBuffer("resolveEntity: base=");
      buf.append(resourceIdentifier.getBaseSystemId());
      buf.append(", literal=");
      buf.append(resourceIdentifier.getLiteralSystemId());
      buf.append(", expanded=");
      buf.append(resourceIdentifier.getExpandedSystemId());
      buf.append(", ns=");
      buf.append(resourceIdentifier.getNamespace());
      buf.append(", publicId=");
      buf.append(resourceIdentifier.getPublicId());
      __log.debug(buf.toString());
    }

    XMLInputSource src = _resolver.resolveEntity(resourceIdentifier);
    InputStream is = src.getByteStream();

    if (is == null) {
      __log.debug("resolveEntity: stream not available for: " + src);
      throw new IOException("Unable to locate resource for namespace " + resourceIdentifier.getNamespace());
    }

    byte[] data;
    try {
      data = StreamUtils.read(is);
    } finally {
      is.close();
    }

    try {

      String identifier = null;
      if (resourceIdentifier.getNamespace() == null) {
          identifier = resourceIdentifier.getLiteralSystemId();
      } else {
          identifier = resourceIdentifier.getNamespace();
      }
      
      URI systemId = new URI(FileUtils.encodePath(identifier));

      if (__log.isDebugEnabled()) {
          __log.debug("Captured: "+systemId);
      }
      _capture.put(systemId, data);
    } catch (URISyntaxException use) {
      __log.error("Invalid URI: " + resourceIdentifier.getLiteralSystemId());
      throw new XNIException(use);
    }


    // re-create the InputSource since reading exhausted the XML stream
    return _resolver.resolveEntity(resourceIdentifier);
  }
}
