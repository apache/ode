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

    private File _relativeDir;
    private File _absoluteDir;

    /**
     * Default constructor: resolve relative URIs against current working directory.
     */
    public DefaultResourceFinder() {
        _absoluteDir = new File("");
        _relativeDir = _absoluteDir;
    }

    /**
     * Constructor: resolve relative URIs against specified directory.
     * @param relativeDir base path for relative URLs.
     * @param absoluteDir base path for absolute URLs.
     */
    public DefaultResourceFinder(File relativeDir, File absoluteDir) {
        checkDir("relativeDir", relativeDir);
        checkDir("absoluteDir", absoluteDir);
        _relativeDir = relativeDir;
        _absoluteDir = absoluteDir;
    }

    private void checkDir(String arg, File dir) {
        if (dir == null) {
            throw new IllegalArgumentException("Argument '"+arg+"' is null");
        }
        if (!dir.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + dir);
        }
    }

    public InputStream openResource(URI uri) throws MalformedURLException, IOException {
        uri = relativize(uri);

        InputStream r = openFileResource(uri);
        if (r != null) {
            return r;
        }

        if (__log.isDebugEnabled()) {
            __log.debug("trying classpath resource for " + uri);
        }

        r = Thread.currentThread().getContextClassLoader().getResourceAsStream(uri.getPath());
        if (r != null) {
            return r;
        } else {
            if (__log.isDebugEnabled()) {
                __log.debug("classpath resource not found " + uri);
            }
            return null;
        }

    }

    private InputStream openFileResource(URI uri) throws MalformedURLException, IOException {
        URI absolute = _absoluteDir.toURI();
        if (__log.isDebugEnabled()) {
            __log.debug("openResource: uri="+uri+" relativeDir="+_relativeDir+" absoluteDir="+_absoluteDir);
        }

        if (uri.isAbsolute() && uri.getScheme().equals("file")) {
            try {
                return uri.toURL().openStream();
            } catch (Exception except) {
                __log.debug("openResource: unable to open file URL " + uri + "; " + except.toString());
                return null;
            }
        }

        // Note that if we get an absolute URI, the relativize operation will simply
        // return the absolute URI.
        URI relative = _relativeDir.toURI().relativize(uri);
        if (relative.isAbsolute() && !(relative.getScheme().equals("urn"))) {
           __log.fatal("openResource: invalid scheme (should be urn:)  " + uri);
           return null;
        }

        File f = new File(absolute.getPath(), relative.getPath());
        if (f.exists()) {
            return new FileInputStream(f);
        } else {
            if (__log.isDebugEnabled()) {
                __log.debug("fileNotFound: " + f);
            }
            return null;
        }
    }

    public URI getBaseResourceURI() {
        return _absoluteDir.toURI();
    }

    private URI relativize(URI u) {
        if (u.isAbsolute()) {
            return _absoluteDir.toURI().relativize(u);
        } else return u;
    }

    public URI resolve(URI parent, URI child) {
        parent = relativize(parent);
        child = relativize(child);
        URI result = parent.resolve(child);
        URI result2 = _absoluteDir.toURI().resolve(result);
        if (__log.isDebugEnabled()) {
            __log.debug("resolving URI: parent " + parent + " child " + child + " result " + result + " resultAbsolute:" + result2);
        }

        return result2;
    }

}
