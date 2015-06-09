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

import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;

import org.w3c.dom.Element;

/**
 * Little hack to solve the disfunctional WSDL4J extension mechanism. Without this,
 * WSDL4J will attempt to do Class.forName to get the WSDLFactory, which will break
 * if WSDL4J is loaded from a parent class-loader (as it often is, e.g. in ServiceMix).
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
class WSDLReaderImpl extends com.ibm.wsdl.xml.WSDLReaderImpl {

    private WSDLFactory _localFactory;
    
    WSDLReaderImpl(WSDLFactory factory) {
        _localFactory = factory;
    }
    
    @Override
    protected WSDLFactory getWSDLFactory() throws WSDLException {
        return _localFactory;
    }

    @Override
    public Binding parseBinding(Element bindingEl, Definition def) throws WSDLException {
        Binding binding = super.parseBinding(bindingEl, def);
        binding.setDocumentationElement(null);
        return binding;
    }

    @Override
    public BindingFault parseBindingFault(Element bindingFaultEl, Definition def) throws WSDLException {
        BindingFault bindingFault = super.parseBindingFault(bindingFaultEl, def);
        bindingFault.setDocumentationElement(null);
        return bindingFault;
    }

    @Override
    public BindingInput parseBindingInput(Element bindingInputEl, Definition def) throws WSDLException {
        BindingInput bindingInput = super.parseBindingInput(bindingInputEl, def);
        bindingInput.setDocumentationElement(null);
        return bindingInput;
    }

    @Override
    public BindingOperation parseBindingOperation(Element bindingOperationEl, PortType portType, Definition def) throws WSDLException {
        BindingOperation bindingOperation = super.parseBindingOperation(bindingOperationEl, portType, def);
        bindingOperation.setDocumentationElement(null);
        return bindingOperation;
    }

    @Override
    public BindingOutput parseBindingOutput(Element bindingOutputEl, Definition def) throws WSDLException {
        BindingOutput BindingOutput = super.parseBindingOutput(bindingOutputEl, def);
        BindingOutput.setDocumentationElement(null);
        return BindingOutput;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Definition parseDefinitions(String documentBaseURI, Element defEl, Map importedDefs) throws WSDLException {
        Definition definition = super.parseDefinitions(documentBaseURI, defEl, importedDefs);
        definition.setDocumentationElement(null);
        return definition;
    }

    @Override
    public Fault parseFault(Element faultEl, Definition def) throws WSDLException {
        Fault fault = super.parseFault(faultEl, def);
        fault.setDocumentationElement(null);
        return fault;
    }

    @Override
    public Input parseInput(Element inputEl, Definition def) throws WSDLException {
        Input input = super.parseInput(inputEl, def);
        input.setDocumentationElement(null);
        return input;
    }

    @Override
    public Message parseMessage(Element msgEl, Definition def) throws WSDLException {
        Message message = super.parseMessage(msgEl, def);
        message.setDocumentationElement(null);
        return message;
    }

    @Override
    public Operation parseOperation(Element opEl, PortType portType, Definition def) throws WSDLException {
        Operation operation = super.parseOperation(opEl, portType, def);
        operation.setDocumentationElement(null);
        return operation;
    }

    @Override
    public Output parseOutput(Element outputEl, Definition def) throws WSDLException {
        Output output = super.parseOutput(outputEl, def);
        output.setDocumentationElement(null);
        return output;
    }

    @Override
    public Part parsePart(Element partEl, Definition def) throws WSDLException {
        Part part = super.parsePart(partEl, def);
        part.setDocumentationElement(null);
        return part;
    }

    @Override
    public Port parsePort(Element portEl, Definition def) throws WSDLException {
        Port Port = super.parsePort(portEl, def);
        Port.setDocumentationElement(null);
        return Port;
    }

    @Override
    public PortType parsePortType(Element portTypeEl, Definition def) throws WSDLException {
        PortType portType = super.parsePortType(portTypeEl, def);
        portType.setDocumentationElement(null);
        return portType;
    }

    @Override
    public Service parseService(Element serviceEl, Definition def) throws WSDLException {
        Service service = super.parseService(serviceEl, def);
        service.setDocumentationElement(null);
        return service;
    }

    @Override
    public Types parseTypes(Element typesEl, Definition def) throws WSDLException {
        Types types = super.parseTypes(typesEl, def);
        types.setDocumentationElement(null);
        return types;
    }
}
