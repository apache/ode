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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic implementation of the {@link ResourceFinder} interface. Resolves
 * URIs relative to a base URI specified at the time of construction.
 *
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class DefaultResourceFinder implements ResourceFinder {
    private static final Log __log = LogFactory.getLog(DefaultResourceFinder.class);

    private File _suDir;

    /**
     * Default constructor: resolve relative URIs against current working directory.
     */
    public DefaultResourceFinder() {
        _suDir = new File("");
    }

    /**
     * Constructor: resolve relative URIs against specified directory.
     * @param suDir base path for relative URIs.
     */
    public DefaultResourceFinder(File suDir) {
        if (suDir == null) {
            throw new IllegalArgumentException("Argument 'suDir' is null");
        }
        if (!suDir.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + suDir);
        }
        _suDir = suDir;
    }


    public InputStream openResource(URI uri) throws MalformedURLException, IOException {
        URI suURI = _suDir.toURI();

        if (uri.isAbsolute() && uri.getScheme().equals("file")) {
            try {
                return uri.toURL().openStream();
            } catch (Exception except) {
                __log.fatal("openResource: unable to open file URL " + uri + "; " + except.toString());
                return null;
            }
        }

        // Note that if we get an absolute URI, the relativize operation will simply
        // return the absolute URI.
        URI relative = suURI.relativize(uri);
        if (relative.isAbsolute() && !relative.getScheme().equals("urn")) {
           __log.fatal("openResource: invalid scheme (should be urn:)  " + uri);
           return null;
        }

        File f = new File(suURI.getPath(),relative.getPath());
        if (!f.exists()) {
            __log.debug("fileNotFound: " + f);
            return null;
        }

        return new FileInputStream(f);
    }

}
