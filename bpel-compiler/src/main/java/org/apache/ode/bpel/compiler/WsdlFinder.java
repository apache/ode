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

import org.apache.ode.bpel.compiler.wsdl.Definition4BPEL;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;

/**
 * Simple wrapper for WSDL location.
 */
public interface WsdlFinder {
  
    /**
     * Set the base URL to compose relative URLs against.
     * @param base the base URL to resolve against or <code>null</code> if none exists.
     */
    void setBaseURI(URI base);
    
    /**
     * Resolve a URI to a definition with BPEL-specific additions.
     * @param f the WSDLReader to use.
     * @param uri the URI of the definition
     * @return the definition
     * @throws WSDLException if one occurs while reading the WSDL or its imports.
     */
    Definition4BPEL loadDefinition(WSDLReader f, File importFrom, URI uri) throws WSDLException;

    InputStream openResource(URI uri) throws MalformedURLException, IOException;
}

