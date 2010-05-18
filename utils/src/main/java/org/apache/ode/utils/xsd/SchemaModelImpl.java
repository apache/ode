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
package org.apache.ode.utils.xsd;

import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xs.LSInputList;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMError;
import org.w3c.dom.ls.LSInput;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Xerces based schema model.
 */
public class SchemaModelImpl implements SchemaModel {
    private static final Log __log = LogFactory.getLog(SchemaModelImpl.class);
    private XSModel _model;

    private SchemaModelImpl(XSModel model) {
        if (model == null) throw new IllegalArgumentException("Null model.");
        _model = model;
    }

    /**
     * Generate a schema model from a collection of schemas.
     * @param schemas collection of schemas (indexed by systemId)
     *
     * @return a {@link SchemaModel}
     */
    public static final SchemaModel newModel(Map<URI, byte[]> schemas) {
        XMLSchemaLoader schemaLoader = new XMLSchemaLoader();
        InternalSchemaResolver resolver = new InternalSchemaResolver();
        schemaLoader.setEntityResolver(resolver);
        schemaLoader.setParameter(Constants.DOM_ERROR_HANDLER, new SchemaErrorHandler());


        final String[] uris = new String[schemas.size()];
        final byte[][] content = new byte[schemas.size()][];

        int idx = 0;
        for (Iterator<Map.Entry<URI,byte[]>> i = schemas.entrySet().iterator();i.hasNext();) {
            Map.Entry<URI, byte[]> me = i.next();
            uris[idx] = me.getKey().toASCIIString();
            content[idx] = me.getValue();
            resolver.put(me.getKey(), me.getValue());
            ++idx;
        }

        LSInputList list = new LSInputList() {
            public LSInput item(int index) {
                DOMInputImpl input = new DOMInputImpl();
                input.setSystemId(uris[index]);
                input.setByteStream(new ByteArrayInputStream(content[index]));
                return input;
            }

            public int getLength() {
                return uris.length;
            }
        };

        XSModel xsm = schemaLoader.loadInputList(list);
        return new SchemaModelImpl(xsm);
    }

    private static class SchemaErrorHandler implements DOMErrorHandler {
        public boolean handleError(DOMError error) {
            boolean isWarning = (error.getSeverity() == DOMError.SEVERITY_WARNING);
            __log.warn("Schema error", ((Exception)error.getRelatedException()));
            __log.warn(error.getLocation().getUri() + ":" + error.getLocation().getLineNumber());
            __log.warn(error.getRelatedData());
            __log.warn(error.getRelatedException());
            return isWarning;
        }
    }

    /**
     * @see org.apache.ode.utils.xsd.SchemaModel#isCompatible(javax.xml.namespace.QName,
     *      javax.xml.namespace.QName)
     */
    public boolean isCompatible(QName type1, QName type2) {
        XSTypeDefinition typeDef1;
        XSTypeDefinition typeDef2;

        if (knowsElementType(type1)) {
            typeDef1 = _model.getElementDeclaration(type1.getLocalPart(),
                    type1.getNamespaceURI())
                    .getTypeDefinition();
        } else if (knowsSchemaType(type1)) {
            typeDef1 = _model.getTypeDefinition(type1.getLocalPart(),
                    type1.getNamespaceURI());
        } else {
            throw new IllegalArgumentException("unknown schema type: " + type1);
        }

        if (knowsElementType(type2)) {
            typeDef2 = _model.getElementDeclaration(type2.getLocalPart(),
                    type2.getNamespaceURI())
                    .getTypeDefinition();
        } else if (knowsSchemaType(type2)) {
            typeDef2 = _model.getTypeDefinition(type2.getLocalPart(),
                    type2.getNamespaceURI());
        } else {
            throw new IllegalArgumentException("unknown schema type: " + type2);
        }

        return typeDef1.derivedFromType(typeDef2, (short)0)
                || typeDef2.derivedFromType(typeDef1, (short)0);
    }

    /**
     * @see org.apache.ode.utils.xsd.SchemaModel#isSimpleType(javax.xml.namespace.QName)
     */
    public boolean isSimpleType(QName type) {
        if (type == null)
            throw new NullPointerException("Null type argument!");

        XSTypeDefinition typeDef = _model.getTypeDefinition(type.getLocalPart(),
                type.getNamespaceURI());

        return (typeDef != null)
                && (typeDef.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE);
    }

    /**
     * @see org.apache.ode.utils.xsd.SchemaModel#knowsElementType(javax.xml.namespace.QName)
     */
    public boolean knowsElementType(QName elementType) {
        if (elementType == null)
            throw new NullPointerException("Null type argument!");

        return _model.getElementDeclaration(elementType.getLocalPart(),
                elementType.getNamespaceURI()) != null;
    }

    /**
     * @see org.apache.ode.utils.xsd.SchemaModel#knowsSchemaType(javax.xml.namespace.QName)
     */
    public boolean knowsSchemaType(QName schemaType) {
        if (schemaType == null)
            throw new NullPointerException("Null type argument!");

        return _model.getTypeDefinition(schemaType.getLocalPart(),
                schemaType.getNamespaceURI()) != null;
    }


    public static class InternalSchemaResolver implements XMLEntityResolver {
        private Map<String, byte[]> _schemas = new HashMap<String, byte[]>();
        public void put(URI uri, byte[] bytes) {
            _schemas.put(uri.toASCIIString(), bytes);
        }
        
        public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
            XMLInputSource src = new XMLInputSource(resourceIdentifier);
            String location = "";
            if (resourceIdentifier.getNamespace() != null && _schemas.get(resourceIdentifier.getNamespace()) != null)
                location = resourceIdentifier.getNamespace();
            else if (resourceIdentifier.getLiteralSystemId() != null && _schemas.get(resourceIdentifier.getLiteralSystemId()) != null)
                location = resourceIdentifier.getLiteralSystemId();
            else if (resourceIdentifier.getExpandedSystemId() != null && _schemas.get(resourceIdentifier.getExpandedSystemId()) != null)
                location = resourceIdentifier.getExpandedSystemId();
            else {
                if (__log.isDebugEnabled()) {
                    __log.debug("Available schemas " + _schemas.keySet());
                }
                throw new IllegalStateException("Schema " + resourceIdentifier + " not captured");
            }

            src.setByteStream(new ByteArrayInputStream(_schemas.get(location)));
            return src;
        }
    }
}
