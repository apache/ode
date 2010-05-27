/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.jbi.util;

import java.net.URI;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.XMLParserUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Collection of schemas.
 *
 * @author gnodet
 */
public class SchemaCollection {

    private static Log log = LogFactory.getLog(SchemaCollection.class);

    private Map schemas;
    private URI baseUri;

    public SchemaCollection() {
        this(null);
    }

    public SchemaCollection(URI baseUri) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing schema collection with baseUri: " + baseUri);
        }
        this.baseUri = baseUri;
        this.schemas = new HashMap();
    }

    public Schema getSchema(String namespaceURI) {
        return (Schema) schemas.get(namespaceURI);
    }

    public void read(Element elem, URI sourceUri) throws Exception {
        Schema schema = new Schema();
        schema.setSourceUri(sourceUri);
        schema.setRoot(elem);
        schema.setNamespace(elem.getAttribute("targetNamespace"));
        schemas.put(schema.getNamespace(), schema);
        handleImports(schema);
    }

    public void read(String location, URI baseUri) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Reading schema at '" + location + "' with baseUri '" + baseUri + "'");
        }
        if (baseUri == null) {
            baseUri = this.baseUri;
        }
        URI loc;
        if (baseUri != null) {
            loc = resolve(baseUri, location);
            if (!loc.isAbsolute()) {
                throw new IllegalArgumentException("Unable to resolve '" + loc.toString() + "' relative to '" + baseUri + "'");
            }
        } else {
            loc = new URI(location);
            if (!loc.isAbsolute()) {
                throw new IllegalArgumentException("Location '" + loc.toString() + "' is not absolute and no baseUri specified");
            }
        }
        InputSource inputSource = new InputSource();
        inputSource.setByteStream(loc.toURL().openStream());
        inputSource.setSystemId(loc.toString());
        read(inputSource);
    }

    public void read(InputSource inputSource) throws Exception {
        DocumentBuilderFactory docFac = XMLParserUtils.getDocumentBuilderFactory(); // don't trust system provided parser!
        docFac.setNamespaceAware(true);
        DocumentBuilder builder = docFac.newDocumentBuilder();
        Document doc = builder.parse(inputSource);
        read(doc.getDocumentElement(),
             inputSource.getSystemId() != null ? new URI(inputSource.getSystemId()) : null);
    }

    protected void handleImports(Schema schema) throws Exception {
        NodeList children = schema.getRoot().getChildNodes();
        List imports = new ArrayList();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                Element ce = (Element) child;
                if ("http://www.w3.org/2001/XMLSchema".equals(ce.getNamespaceURI()) &&
                    "import".equals(ce.getLocalName())) {
                    imports.add(ce);
                }
            }
        }
        for (Iterator iter = imports.iterator(); iter.hasNext();) {
            Element ce = (Element) iter.next();
            String namespace = ce.getAttribute("namespace");
            if (schemas.get(namespace) == null) {
                String location = ce.getAttribute("schemaLocation");
                if (location != null && !"".equals(location)) {
                    read(location, schema.getSourceUri());
                }
            }
            schema.addImport(namespace);
            schema.getRoot().removeChild(ce);
        }
    }

    protected static URI resolve(URI base, String location) {
        if ("jar".equals(base.getScheme())) {
            String str = base.toString();
            String[] parts = str.split("!");
            parts[1] = URI.create(parts[1]).resolve(location).toString();
            return URI.create(parts[0] + "!" + parts[1]);
        }
        return base.resolve(location);
    }

    public int getSize() {
        if (schemas != null) {
           return schemas.size();
        } else {
           return 0;
        }
     }

     public Collection getSchemas() {
        if (schemas != null) {
           return schemas.values();
        } else {
           return java.util.Collections.EMPTY_SET;
        }
     }
}
