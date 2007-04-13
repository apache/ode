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
package org.apache.ode.store;

import org.apache.ode.utils.fs.FileUtils;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Resolves references inide the deployment unit.
 */
public class DocumentEntityResolver implements XMLEntityResolver {

    private File _docRoot;

    public DocumentEntityResolver(File docRoot) {
        _docRoot = docRoot;
    }

    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
        XMLInputSource src = new XMLInputSource(resourceIdentifier);
        String resourceName = resourceIdentifier.getLiteralSystemId();
        String base;
        try {
            base = new URI(FileUtils.encodePath(resourceIdentifier.getBaseSystemId())).toURL().getFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Base system id incorrect, parser error", e);
        }

        if (new File(new File(base).getParent(), resourceName).exists())
            src.setByteStream(new File(new File(base).getParent(), resourceName).toURL().openStream());
        else src.setByteStream(new File(_docRoot, resourceName).toURL().openStream());

        return src;
    }
}
