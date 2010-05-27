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
import org.xml.sax.InputSource;

import javax.wsdl.xml.WSDLLocator;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class WSDLLocatorImpl implements WSDLLocator {

    private static final Log __log = LogFactory.getLog(WSDLLocatorImpl.class);

    private ResourceFinder _resourceFinder;
    private URI _base;
    private String _latest;

    public WSDLLocatorImpl(ResourceFinder resourceFinder, URI base) {
        _resourceFinder = resourceFinder;
        _base = base;
    }

    public InputSource getBaseInputSource() {
        try {
            InputSource is = new InputSource();
            is.setByteStream(_resourceFinder.openResource(_base));
            is.setSystemId(_base.toString());
            return is;
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public InputSource getImportInputSource(String parent, String imprt) {
        URI uri;
        try {
            uri = parent == null ? _base.resolve(imprt) : new URI(parent).resolve(imprt);
        } catch (URISyntaxException e1) {
            __log.error("URI syntax error: " + parent);
            return null;
        }
        __log.debug("getImportInputSource: parent=" + parent + ", imprt=" + imprt + ", uri=" + uri);

        InputSource is = new InputSource();
        try {
            is.setByteStream(_resourceFinder.openResource(uri));
        } catch (Exception e) {
            return null;
        }
        is.setSystemId(uri.toString());
        _latest = uri.toString();
        return is;
    }

    public String getBaseURI() {
        return _base.toString();
    }

    public String getLatestImportURI() {
        return _latest;
    }

    public void close() {
        _resourceFinder = null;
    }
}
