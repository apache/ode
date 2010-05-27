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
package org.apache.ode.bpel.compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * Xerces {@link XMLEntityResolver} implementation that defers to  our own
 * {@link ResourceFinder} interface for loading resources. This class is
 * used for XSD-Schema capture which uses the Xerces schema model.
 *
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class WsdlFinderXMLEntityResolver implements XMLEntityResolver {

    private static final Log __log = LogFactory
            .getLog(WsdlFinderXMLEntityResolver.class);

    /**
     * Flag indicating whether the resolver should fail with an exception if the
     * requested resource is not found. The interface suggests that null should be
     * returned so that a "default" resolution mechanism can be used; however, we
     * don't necessarily want to use this default mechanism.
     */
    private boolean _failIfNotFound = true;

    private ResourceFinder _wsdlFinder;
    private Map<URI, byte[]> _internalSchemas = new HashMap<URI, byte[]>();
    private URI _baseURI;

    /**
     * Constructor.
     * @param finder {@link ResourceFinder} implementation.
     * @param baseURI the base URI against which all relative URIs are to be resolved;
     *                typically this is the system URI of the WSDL containing an
     *                embedded schema
     */
    public WsdlFinderXMLEntityResolver(ResourceFinder finder, URI baseURI, Map<URI, byte[]> internalSchemas,
            boolean failIfNotFound) {
        _wsdlFinder = finder;
        _baseURI = baseURI;
        _internalSchemas = internalSchemas;
        _failIfNotFound = failIfNotFound;
    }

    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
            throws XNIException, IOException {

        if (__log.isDebugEnabled())
            __log.debug("resolveEntity(" + resourceIdentifier + ")");

        XMLInputSource src = new XMLInputSource(resourceIdentifier);
        URI location;

        if (resourceIdentifier.getLiteralSystemId() == null) {
            // import without schemaLocation
            if (__log.isDebugEnabled()) __log.debug("resolveEntity: no schema location for "+resourceIdentifier.getNamespace());
            try {
                if (_internalSchemas.get(new URI(resourceIdentifier.getNamespace())) != null) {
                    src.setByteStream(new ByteArrayInputStream(_internalSchemas.get(new URI(resourceIdentifier.getNamespace()))));
                    return src;
                }
            } catch (URISyntaxException e) {
                __log.debug("resolveEntity: no schema location an no known namespace for: " + resourceIdentifier.getNamespace());
            }
            return null;
        } else {
            location = _wsdlFinder.resolve(URI.create(resourceIdentifier.getBaseSystemId()), URI.create(resourceIdentifier.getLiteralSystemId()));
        }

        if (__log.isDebugEnabled())
            __log.debug("resolveEntity: Expecting to find " + resourceIdentifier.getNamespace()
                    + " at " + location);

        if (_internalSchemas.get(location) != null) {
            src.setByteStream(new ByteArrayInputStream(_internalSchemas.get(location)));
            return src;
        }

        try {
            InputStream str = _wsdlFinder.openResource(location);
            if (str != null)
                src.setByteStream(str);
            else {
                __log.debug("resolveEntity: resource not found: " + location);
                throw new IOException("Resource not found: " + location);
            }
        } catch (IOException ioex) {
            __log.debug("resolveEntity: IOException opening " + location,ioex);

            if (_failIfNotFound) {
                __log.debug("resolveEntity: failIfNotFound is true, rethrowing...");
                throw ioex;
            }
            __log.debug("resolveEntity: failIfNotFound is false, returning null");
            return null;
        } catch (Exception ex) {
            __log.debug("resolveEntity: unexpected error: " + location);
            throw new IOException("Unexpected error loading resource: " + location);
        }
        return src;
    }

}
