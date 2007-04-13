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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.wsdl.extensions.schema.SchemaImpl;

public class WSDLFlattener {

    private static Log __log = LogFactory.getLog(WSDLFlattener.class);
    
    private Definition _definition;
    private SchemaCollection _schemas;
    private Map<QName, Definition> _flattened;
    private boolean _initialized;
    

    public WSDLFlattener(Definition definition) {
        this(definition, null);
    }
        
    public WSDLFlattener(Definition definition, SchemaCollection schemas) {
        if (definition == null)
            throw new NullPointerException("Null definition!");
        this._definition = definition;
        this._flattened = new ConcurrentHashMap<QName, Definition>();
        this._schemas = schemas;
    }
    
    /**
     * Parse the schemas referenced by the definition.
     * @throws Exception if an error occurs
     */
    public void initialize() throws Exception {
        if (!_initialized) {
            if (_schemas == null) {
                this._schemas = new SchemaCollection(getUri(_definition.getDocumentBaseURI()));
            }
            parseSchemas(this._definition);
            _initialized = true;
        }
    }
    
    /**
     * Retrieve a flattened definition for a given port type name.
     * @param portType the port type to create a flat definition for
     * @return a flat definition for the port type
     * @throws Exception if an error occurs
     */
    public Definition getDefinition(QName portType) throws Exception {
        Definition def = (Definition) _flattened.get(portType);
        if (def == null) {
            def = flattenDefinition(portType);
            _flattened.put(portType, def);
        }
        return def;
    }

    /**
     * @return Returns the definition.
     */
    public Definition getDefinition() {
        return _definition;
    }

    /**
     * @param definition The definition to set.
     */
    public void setDefinition(Definition definition) {
        this._definition = definition;
    }

    /**
     * @return Returns the schemas.
     */
    public SchemaCollection getSchemas() throws Exception {
        return _schemas;
    }

    /**
     * @param schemas The schemas to set.
     */
    public void setSchemas(SchemaCollection schemas) {
        this._schemas = schemas;
    }
    
    private Definition flattenDefinition(QName name) throws Exception {
        // Check that schemas have been loaded
        initialize();
        // Create new definition
        Definition flat = WSDLFactory.newInstance().newDefinition();
        flat.setTargetNamespace(name.getNamespaceURI());
        addNamespaces(flat, _definition);
        // Create port type
        PortType defPort = _definition.getPortType(name);
        PortType flatPort = flat.createPortType();
        flatPort.setQName(defPort.getQName());
        flatPort.setUndefined(false);
        // Import all operations and related messages
        for (Iterator itOper = defPort.getOperations().iterator(); itOper.hasNext();) {
            Operation defOper = (Operation) itOper.next();
            Operation flatOper = flat.createOperation();
            flatOper.setName(defOper.getName());
            flatOper.setStyle(defOper.getStyle());
            flatOper.setUndefined(false);
            if (defOper.getInput() != null) {
                Input flatInput = flat.createInput();
                flatInput.setName(defOper.getInput().getName());
                if (defOper.getInput().getMessage() != null) {
                    Message flatInputMsg = copyMessage(defOper.getInput().getMessage(), flat);
                    flatInput.setMessage(flatInputMsg);
                    flat.addMessage(flatInputMsg);
                }
                flatOper.setInput(flatInput);
            }
            if (defOper.getOutput() != null) {
                Output flatOutput = flat.createOutput();
                flatOutput.setName(defOper.getOutput().getName());
                if (defOper.getOutput().getMessage() != null) {
                    Message flatOutputMsg = copyMessage(defOper.getOutput().getMessage(), flat);
                    flatOutput.setMessage(flatOutputMsg);
                    flat.addMessage(flatOutputMsg);
                }
                flatOper.setOutput(flatOutput);
            }
            for (Iterator itFault = defOper.getFaults().values().iterator(); itFault.hasNext();) {
                Fault defFault = (Fault) itFault.next();
                Fault flatFault = flat.createFault();
                flatFault.setName(defFault.getName());
                if (defFault.getMessage() != null) {
                    Message flatFaultMsg = copyMessage(defFault.getMessage(), flat);
                    flatFault.setMessage(flatFaultMsg);
                    flat.addMessage(flatFaultMsg);
                }
                flatOper.addFault(flatFault);
            }
            flatPort.addOperation(flatOper);
        }
        
        // Import schemas in definition
        if (_schemas.getSize() > 0) {
           Types types = flat.createTypes();
           for (Iterator it = _schemas.getSchemas().iterator(); it.hasNext();) {
              javax.wsdl.extensions.schema.Schema imp = new SchemaImpl();
              imp.setElement(((Schema)it.next()).getRoot());
              imp.setElementType(new QName("http://www.w3.org/2001/XMLSchema", "schema"));
              types.addExtensibilityElement(imp);
           }
           flat.setTypes(types);
        }
        
        flat.addPortType(flatPort);
        return flat;
    }
    
    private void parseSchemas(Definition def) throws Exception {
        if (def.getTypes() != null && def.getTypes().getExtensibilityElements() != null) {
            for (Iterator iter = def.getTypes().getExtensibilityElements().iterator(); iter.hasNext();) {
                ExtensibilityElement element = (ExtensibilityElement) iter.next();
                if (element instanceof javax.wsdl.extensions.schema.Schema) {
                    javax.wsdl.extensions.schema.Schema schema = (javax.wsdl.extensions.schema.Schema) element;
                    if (schema.getElement() != null) {
                        _schemas.read(schema.getElement(), getUri(schema.getDocumentBaseURI()));
                    }
                    for (Iterator itImp = schema.getImports().values().iterator(); itImp.hasNext();) {
                        Collection imps = (Collection) itImp.next();
                        for (Iterator itSi = imps.iterator(); itSi.hasNext();) {
                            SchemaImport imp = (SchemaImport) itSi.next();
                            _schemas.read(imp.getSchemaLocationURI(), getUri(def.getDocumentBaseURI()));
                        }
                    }
                }
            }
        }
        if (def.getImports() != null) {
            for (Iterator itImp = def.getImports().values().iterator(); itImp.hasNext();) {
                Collection imps = (Collection) itImp.next();
                for (Iterator iter = imps.iterator(); iter.hasNext();) {
                    Import imp = (Import) iter.next();
                    parseSchemas(imp.getDefinition());
                }
            }
        }
    }

    private void addNamespaces(Definition flat, Definition def) {
        for (Iterator itImport = def.getImports().values().iterator(); itImport.hasNext();) {
            List defImports = (List) itImport.next();
            for (Iterator iter = defImports.iterator(); iter.hasNext();) {
                Import defImport = (Import) iter.next();
                addNamespaces(flat, defImport.getDefinition());
            }
        }
        for (Iterator itNs = def.getNamespaces().keySet().iterator(); itNs.hasNext();) {
            String key = (String) itNs.next();
            String val = def.getNamespace(key);
            flat.addNamespace(key, val);
        }
    }
    
    private Message copyMessage(Message defMessage, Definition flat) {
        Message flatMsg = flat.createMessage();
        flatMsg.setUndefined(false);
        if (defMessage.getQName() != null) {
            flatMsg.setQName(new QName(flat.getTargetNamespace(), defMessage.getQName().getLocalPart()));
        }
        for (Iterator itPart = defMessage.getParts().values().iterator(); itPart.hasNext();) {
            Part defPart = (Part) itPart.next();
            Part flatPart = flat.createPart();
            flatPart.setName(defPart.getName());
            flatPart.setElementName(defPart.getElementName());
            flatMsg.addPart(flatPart);
        }
        return flatMsg;
    }

    private URI getUri(String str) {
        if (str != null) {
            str = str.replaceAll(" ", "%20");
            return URI.create(str);
        }
        return null;
    }

}
