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

import java.net.URI;
import java.io.File;

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
    public String loadXsltSheet(File importFrom, URI uri);
}
