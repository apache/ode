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

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.wsdl.Definition4BPEL;

/**
 * A parsed collection of WSDL definitions, including BPEL-specific extensions.
 */
public class DocumentRegistry {
    private static final Log __log = LogFactory.getLog(DocumentRegistry.class);

    private final ArrayList<Definition4BPEL> _definitions = new ArrayList<Definition4BPEL>();


    /**
     * Obtains an WSDL definition based on its target namespace.
     *
     * @param serviceName
     *
     * @return WSDL definition or <code>null</code> if unavailable.
     */
    public Definition4BPEL getDefinition(QName serviceName) {
        for (Definition4BPEL definition4BPEL : _definitions) {
            if (definition4BPEL.getTargetNamespace().equals(serviceName.getNamespaceURI())) {
                if (definition4BPEL.getService(serviceName) != null)
                    return definition4BPEL;
            }
        }
        return null;
    }

    /**
     * Obtains an WSDL definition based on its target namespace.
     *
     * @param serviceName
     *
     * @return WSDL definition or <code>null</code> if unavailable.
     */
    public Definition4BPEL getDefinitionForPortType(QName serviceName) {
        for (Definition4BPEL definition4BPEL : _definitions) {
            if (definition4BPEL.getTargetNamespace().equals(serviceName.getNamespaceURI())) {
                if (definition4BPEL.getPortType(serviceName) != null)
                    return definition4BPEL;
            }
        }
        return null;
    }

    public Definition4BPEL[] getDefinitions(){
        return _definitions.toArray(new Definition4BPEL[_definitions.size()]);
    }

    /**
     * Adds a WSDL definition for use in resolving MessageType, PortType,
     * Operation and BPEL properties and property aliases
     * @param def WSDL definition
     */
    @SuppressWarnings("unchecked")
    public void addDefinition(Definition4BPEL def) throws CompilationException {
        if (def == null)
            throw new NullPointerException("def=null");

        if (DocumentRegistry.__log.isDebugEnabled()) {
            DocumentRegistry.__log.debug("addDefinition(" + def.getTargetNamespace() + " from " + def.getDocumentBaseURI() + ")");
        }

        _definitions.add(def);
    }

}
