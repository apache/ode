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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.fs.FileUtils;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

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
    private HashMap<String,String> _internalResources = new HashMap<String, String>();

    public WsdlFinderXMLEntityResolver(ResourceFinder finder) {
        _wsdlFinder = finder;
    }

    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
            throws XNIException, IOException {

        if (__log.isDebugEnabled())
            __log.debug("resolveEntity(" + resourceIdentifier + ")");

        XMLInputSource src = new XMLInputSource(resourceIdentifier);
        URI location;

        try {
            if (resourceIdentifier.getLiteralSystemId() == null)
                location = new URI(resourceIdentifier.getNamespace());
            else if (resourceIdentifier.getExpandedSystemId() != null)
                location = new URI(FileUtils.encodePath(resourceIdentifier.getExpandedSystemId()));
            else
                location = new URI(FileUtils.encodePath(resourceIdentifier.getLiteralSystemId()));
        } catch (URISyntaxException e) {
            __log.debug("resolveEntity: URI syntax error", e);
            throw new IOException(e.getMessage());
        }

        if (__log.isDebugEnabled())
            __log.debug("resolveEntity: Expecting to find " + resourceIdentifier.getNamespace()
                    + " at " + location);

        if (_internalResources.get(location.toString()) != null) {
            src.setByteStream(new ByteArrayInputStream(_internalResources.get(location.toString()).getBytes()));
            return src;
        }

        try {
            src.setByteStream(_wsdlFinder.openResource(location));
        } catch (IOException ioex) {
            __log.debug("resolveEntity: IOException opening " + location,ioex);

            if (_failIfNotFound) {
                __log.debug("resolveEntity: failIfNotFound set, rethrowing...");
                throw ioex;
            }

            __log.debug("resolveEntity: failIfNotFound NOT set, returning NULL");
            return null;
        }

        return src;
    }

    public void addInternalResource(String ns, String source) {
        _internalResources.put(ns, source);
    }

}
