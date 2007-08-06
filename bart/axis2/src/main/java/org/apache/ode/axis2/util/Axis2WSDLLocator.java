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
package org.apache.ode.axis2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.wsdl.xml.WSDLLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

public class Axis2WSDLLocator implements WSDLLocator {
    private static final Log LOG = LogFactory.getLog(Axis2WSDLLocator.class);
    private URI _baseUri;
    private String _latest;

    public Axis2WSDLLocator(URI baseUri) throws URISyntaxException {
        _baseUri = baseUri;
    }

    public InputSource getBaseInputSource() {
        try {
            InputSource is = new InputSource();
            is.setByteStream(openResource(_baseUri));
            is.setSystemId(_baseUri.toString());
            return is;
        } catch (IOException e) {
            LOG.error("Unable to create InputSource for " + _baseUri, e);
            return null;
        }
    }

    public InputSource getImportInputSource(String parent, String imprt) {
        URI uri;
        try {
            uri = parent == null ? _baseUri.resolve(imprt) : new URI(parent).resolve(imprt);
        } catch (URISyntaxException e1) {
            LOG.error("URI syntax error: parent="+parent+" error="+e1);
            return null;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get import:  import=" + imprt + " parent=" + parent);
        }

        InputSource is = new InputSource();
        try {
            is.setByteStream(openResource(uri));
        } catch (Exception e) {
            LOG.error("Unable to open import resource: " + uri, e);
            return null;
        }
        is.setSystemId(uri.toString());
        _latest = uri.toString();
        return is;
    }

    public String getBaseURI() {
        return _baseUri.toString();
    }

    public String getLatestImportURI() {
        return _latest;
    }

    public void close() {
    }

    public InputStream openResource(URI uri) throws IOException {
        if (uri.isAbsolute() && uri.getScheme().equals("file")) {
            try {
                return uri.toURL().openStream();
            } catch (Exception except) {
                LOG.error("openResource: unable to open file URL " + uri + "; " + except.toString());
                return null;
            }
        }

        // Note that if we get an absolute URI, the relativize operation will simply
        // return the absolute URI.
        URI relative = _baseUri.relativize(uri);
        if (relative.isAbsolute() && !relative.getScheme().equals("urn")) {
            LOG.error("openResource: invalid scheme (should be urn:)  " + uri);
            return null;
        }

        File f = new File(_baseUri.getPath(), relative.getPath());
        if (!f.exists()) {
            LOG.error("openResource: file not found " + f);
            return null;
        }
        return new FileInputStream(f);
    }
}
