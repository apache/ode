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
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Contains informations related to a schema.
 *  
 * @author gnodet
 */
public class Schema {

    private Element root;
    private String namespace;
    private List imports;
    private URI sourceUri;
    
    /**
     * Add a reference to an imported namespace.
     * @param namespace the namespace to reference
     */
    public void addImport(String namespace) {
        if (imports == null) {
            imports = new ArrayList();
        }
        imports.add(namespace);
    }
    
    /**
     * @return Returns the imports.
     */
    public List getImports() {
        return imports;
    }

    /**
     * @param imports The imports to set.
     */
    public void setImports(List imports) {
        this.imports = imports;
    }

    /**
     * @return Returns the root.
     */
    public Element getRoot() {
        return root;
    }

    /**
     * @param root The root to set.
     */
    public void setRoot(Element root) {
        this.root = root;
    }

    /**
     * @return Returns the namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace The namespace to set.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @return Returns the sourceUri.
     */
    public URI getSourceUri() {
        return sourceUri;
    }

    /**
     * @param sourceUri The sourceUri to set.
     */
    public void setSourceUri(URI sourceUri) {
        this.sourceUri = sourceUri;
    }

}
