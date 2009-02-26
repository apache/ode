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
package org.apache.ode.bpel.compiler.wsdl;

import org.apache.ode.bpel.compiler.bom.PartnerLinkType;
import org.apache.ode.bpel.compiler.bom.Property;
import org.apache.ode.bpel.compiler.bom.PropertyAlias;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;
import org.w3c.dom.Element;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link Definition4BPEL} wrapper.
 */
class Definition4BPELImpl implements Definition4BPEL {
    private static final long serialVersionUID = 1L;
    Definition _def;
    private String _bpwsNS;
    private String _plnkNS;
    private String _propNS;

    Definition4BPELImpl(Definition wsdlDef, String bpwsNS, String plnkNS, String propNS) {
        _def = wsdlDef;
        _bpwsNS = bpwsNS;
        _plnkNS = plnkNS;
        _propNS = propNS;
    }

    /**
     * Get a list of the defined {@link PartnerLinkType}s.
     *
     * @return {@link List} of {@link PartnerLinkType} objects
     */
    public List<PartnerLinkType> getPartnerLinkTypes() {
        return getElementsForType(new QName(_plnkNS, "partnerLinkType"), PartnerLinkType.class);
    }

    /**
     * Get a list of the defined {@link PartnerLinkType}s.
     *
     * @return {@link List} of {@link PartnerLinkType} objects
     */
    public List<Property> getProperties() {
        return getElementsForType(new QName(_propNS, "property"), Property.class);
    }

    /**
     * Get a list of the defined {@link PropertyAlias}es.
     *
     * @return {@link List} of {@link PropertyAlias} objects
     */
    public List<PropertyAlias> getPropertyAliases() {
        return getElementsForType(new QName(_propNS, "propertyAlias"), PropertyAlias.class);
    }

    public Property getProperty(final QName name) {
        return CollectionsX.find_if(getProperties(), new MemberOfFunction<Property>() {
            public boolean isMember(Property o) {
                return o.getName().equals(name);
            }
        });
    }

    public PartnerLinkType getPartnerLinkType(final QName partnerLinkTypeName) {
        return CollectionsX.find_if(getPartnerLinkTypes(), new MemberOfFunction<PartnerLinkType>() {
            public boolean isMember(PartnerLinkType o) {
                return o.getName().equals(partnerLinkTypeName);
            }
        });
    }

    public PropertyAlias getPropertyAlias(final QName propertyName, final QName messageType) {
        return CollectionsX.find_if(getPropertyAliases(), new MemberOfFunction<PropertyAlias>() {
            public boolean isMember(PropertyAlias o) {
                return o.getPropertyName().equals(propertyName) && o.getMessageType().equals(messageType);
            }
        });
    }



    /**
     * Get a list of the schema types defined in-line.
     *
     * @return {@link List} of {@link XMLSchemaType} objects
     */
    @SuppressWarnings("unchecked")
    public List <XMLSchemaType> getSchemas() {
        return (List<XMLSchemaType>)getTypes().getExtensibilityElements();
    }

    public void addBinding(Binding binding) {
        _def.addBinding(binding);
    }

    public void addExtensibilityElement(ExtensibilityElement extensibilityElement) {
        _def.addExtensibilityElement(extensibilityElement);
    }

    public void addImport(Import anImport) {
        _def.addImport(anImport);
    }

    public void addMessage(Message message) {
        _def.addMessage(message);
    }

    public void addNamespace(String s, String s1) {
        _def.addNamespace(s,s1);
    }

    public void addPortType(PortType portType) {
        _def.addPortType(portType);
    }

    public void addService(Service service) {
        _def.addService(service);
    }

    public Binding createBinding() {
        return _def.createBinding();
    }

    public BindingFault createBindingFault() {
        return _def.createBindingFault();
    }

    public BindingInput createBindingInput() {
        return _def.createBindingInput();
    }

    public BindingOperation createBindingOperation() {
        return _def.createBindingOperation();
    }

    public BindingOutput createBindingOutput() {
        return _def.createBindingOutput();
    }

    public Fault createFault() {
        return _def.createFault();
    }

    public Import createImport() {
        return _def.createImport();
    }

    public Input createInput() {
        return _def.createInput();
    }

    public Message createMessage() {
        return _def.createMessage();
    }

    public Operation createOperation() {
        return _def.createOperation();
    }

    public Output createOutput() {
        return _def.createOutput();
    }

    public Part createPart() {
        return _def.createPart();
    }

    public Port createPort() {
        return _def.createPort();
    }

    public PortType createPortType() {
        return _def.createPortType();
    }

    public Service createService() {
        return _def.createService();
    }

    public Types createTypes() {
        return _def.createTypes();
    }

    public Binding getBinding(QName qName) {
        return _def.getBinding(qName);
    }

    public Map getBindings() {
        return _def.getBindings();
    }

    public String getDocumentBaseURI() {
        return _def.getDocumentBaseURI();
    }

    public Element getDocumentationElement() {
        return _def.getDocumentationElement();
    }

    public List getExtensibilityElements() {
        return _def.getExtensibilityElements();
    }

    public ExtensionRegistry getExtensionRegistry() {
        return _def.getExtensionRegistry();
    }

    public Map getImports() {
        return _def.getImports();
    }

    public List getImports(String s) {
        return _def.getImports(s);
    }

    public Message getMessage(QName qName) {
        return _def.getMessage(qName);
    }

    public Map getMessages() {
        return _def.getMessages();
    }

    public String getNamespace(String s) {
        return _def.getNamespace(s);
    }

    public Map getNamespaces() {
        return _def.getNamespaces();
    }

    public PortType getPortType(QName qName) {
        return _def.getPortType(qName);
    }

    public Map getPortTypes() {
        return _def.getPortTypes();
    }

    public String getPrefix(String s) {
        return _def.getPrefix(s);
    }

    public QName getQName() {
        return _def.getQName();
    }

    public Service getService(QName qName) {
        return _def.getService(qName);
    }

    public Map getServices() {
        return _def.getServices();
    }

    public String getTargetNamespace() {
        return _def.getTargetNamespace();
    }

    public Types getTypes() {
        return _def.getTypes();
    }

    public Binding removeBinding(QName qName) {
        return _def.removeBinding(qName);
    }

    public Message removeMessage(QName qName) {
        return _def.removeMessage(qName);
    }

    public PortType removePortType(QName qName) {
        return _def.removePortType(qName);
    }

    public Service removeService(QName qName) {
        return _def.removeService(qName);
    }

    public void setDocumentBaseURI(String s) {
        _def.setDocumentBaseURI(s);
    }

    public void setDocumentationElement(Element element) {
        _def.setDocumentationElement(element);
    }

    public void setExtensionRegistry(ExtensionRegistry extensionRegistry) {
        _def.setExtensionRegistry(extensionRegistry);
    }

    public void setQName(QName qName) {
        _def.setQName(qName);
    }

    public void setTargetNamespace(String s) {
        _def.setTargetNamespace(s);
    }

    public void setTypes(Types types) {
        _def.setTypes(types);
    }

    public Definition getDefinition() {
        return _def;
    }

    public Map getAllServices() {
        return _def.getAllServices();
    }

    public Map getAllBindings() {
        return _def.getAllBindings();
    }

    public Map getAllPortTypes() {
        return _def.getAllPortTypes();
    }

    public Import removeImport(Import anImport) {
        return _def.removeImport(anImport);
    }

    public String removeNamespace(String string) {
        return _def.removeNamespace(string);
    }

    public Object getExtensionAttribute(QName qName) {
        return _def.getExtensionAttribute(qName);
    }

    public Map getExtensionAttributes() {
        return _def.getExtensionAttributes();
    }

    public List getNativeAttributeNames() {
        return _def.getNativeAttributeNames();
    }

    public void setExtensionAttribute(QName qName, Object object) {
        _def.setExtensionAttribute(qName, object);
    }

    public ExtensibilityElement removeExtensibilityElement(ExtensibilityElement e) {
        return _def.removeExtensibilityElement(e);
    }

    /**
     * Get all the extensibility elements of a certain name (element name that is).
     * @param type type of extensibility element
     * @return list of extensibility elements of the given type
     */
    @SuppressWarnings("unchecked")
    private <T extends ExtensibilityElement> List<T> getElementsForType(final QName type, Class<T> cls) {
        List<T> ret = new ArrayList<T>();
        CollectionsX.filter(ret, getExtensibilityElements(), new MemberOfFunction() {
            public boolean isMember(Object o) {
                return ((ExtensibilityElement)o).getElementType().equals(type);
            }
        });
        return ret;
    }

}
