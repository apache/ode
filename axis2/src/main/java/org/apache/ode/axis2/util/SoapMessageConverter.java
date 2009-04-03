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

package org.apache.ode.axis2.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Document;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import java.util.*;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.namespace.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.OdeFault;
import org.apache.ode.il.OMUtils;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.wsdl.Messages;
import org.apache.ode.utils.wsdl.WsdlUtils;
import org.apache.ode.utils.stl.CollectionsX;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * SOAP/ODE Message converter. Uses WSDL binding information to convert the protocol-neutral ODE representation into a SOAP
 * representation and vice versa.
 */
public class SoapMessageConverter {

    private static final Messages __msgs = Messages.getMessages(Messages.class);

    private static final Log __log = LogFactory.getLog(SoapMessageConverter.class);

    SOAPFactory _soapFactory;

    Definition _def;

    QName _serviceName;

    String _portName;

    Service _serviceDef;

    Binding _binding;

    Port _port;

    boolean _isRPC;

    private SOAPBinding _soapBinding;

    public SoapMessageConverter(Definition def, QName serviceName, String portName) throws AxisFault {
        if (def == null)
            throw new NullPointerException("No WSDL definition was found for service "
                    + serviceName + " and port " + portName);

        _def = def;
        _serviceName = serviceName;
        _portName = portName;

        _serviceDef = _def.getService(serviceName);
        if (_serviceDef == null)
            throw new OdeFault(__msgs.msgServiceDefinitionNotFound(serviceName));
        _port = _serviceDef.getPort(portName);
        if (_port == null)
            throw new OdeFault(__msgs.msgPortDefinitionNotFound(serviceName, portName));
        _binding = _port.getBinding();
        if (_binding == null)
            throw new OdeFault(__msgs.msgBindingNotFound(portName));

        try {
            if (!WsdlUtils.useSOAPBinding(_port)) {
                throw new OdeFault(__msgs.msgNoSOAPBindingForPort(_portName));
            }
            _soapBinding = (SOAPBinding) WsdlUtils.getBindingExtension(_port);
        } catch (IllegalArgumentException iae) {
            throw new OdeFault(iae);
        }

        String style = _soapBinding.getStyle();
        _isRPC = style != null && style.equals("rpc");

        if (_soapBinding.getElementType().getNamespaceURI().equals(Constants.URI_WSDL11_SOAP)) {
            _soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else if (_soapBinding.getElementType().getNamespaceURI().equals(Constants.URI_WSDL12_SOAP)) {
            _soapFactory = OMAbstractFactory.getSOAP12Factory();
        } else {
            throw new IllegalStateException("Unsupported SOAP binding: " + _soapBinding.getElementType());
        }
    }

    @SuppressWarnings("unchecked")
    public void createSoapRequest(MessageContext msgCtx, org.apache.ode.bpel.iapi.Message message, Operation op) throws AxisFault {
        if (op == null)
            throw new NullPointerException("Null operation");
        // The message can be null if the input message has no part
        if (op.getInput().getMessage().getParts().size() > 0 && message == null)
            throw new NullPointerException("Null message.");
        if (msgCtx == null)
            throw new NullPointerException("Null msgCtx");

        BindingOperation bop = _binding.getBindingOperation(op.getName(), null, null);

        if (bop == null)
            throw new OdeFault(__msgs.msgBindingOperationNotFound(_serviceName, _portName, op.getName()));

        BindingInput bi = bop.getBindingInput();
        if (bi == null)
            throw new OdeFault(__msgs.msgBindingInputNotFound(_serviceName, _portName, op.getName()));

        SOAPEnvelope soapEnv = msgCtx.getEnvelope();
        if (soapEnv == null) {
            soapEnv = _soapFactory.getDefaultEnvelope();
            msgCtx.setEnvelope(soapEnv);
        }

        createSoapHeaders(soapEnv, getSOAPHeaders(bi), op.getInput().getMessage(), message.getHeaderParts());

        SOAPBody soapBody = getSOAPBody(bi);
        if (soapBody != null) {
            org.apache.axiom.soap.SOAPBody sb = soapEnv.getBody() == null ? _soapFactory.createSOAPBody(soapEnv) : soapEnv.getBody();
            createSoapBody(sb, soapBody, op.getInput().getMessage(), message.getMessage(), op.getName());
        }

    }

    public void createSoapResponse(MessageContext msgCtx, org.apache.ode.bpel.iapi.Message message, Operation op) throws AxisFault {
        if (op == null)
            throw new NullPointerException("Null operation");
        if (message == null)
            throw new NullPointerException("Null message.");
        if (msgCtx == null)
            throw new NullPointerException("Null msgCtx");

        BindingOperation bop = _binding.getBindingOperation(op.getName(),null,null);

        if (bop == null)
            throw new OdeFault(__msgs.msgBindingOperationNotFound(_serviceName, _portName, op.getName()));

        BindingOutput bo = bop.getBindingOutput();
        if (bo == null)
            throw new OdeFault(__msgs.msgBindingOutputNotFound(_serviceName, _portName, op.getName()));

        SOAPEnvelope soapEnv = msgCtx.getEnvelope();
        if (soapEnv == null) {
            soapEnv = _soapFactory.getDefaultEnvelope();
            msgCtx.setEnvelope(soapEnv);
        }

        if (message.getHeaderParts().size() > 0 || getSOAPHeaders(bo).size() > 0)
            createSoapHeaders(soapEnv, getSOAPHeaders(bo), op.getOutput().getMessage(), message.getHeaderParts());

        SOAPBody soapBody = getSOAPBody(bo);
        if (soapBody != null) {
            org.apache.axiom.soap.SOAPBody sb = soapEnv.getBody() == null ? _soapFactory.createSOAPBody(soapEnv) : soapEnv.getBody();
            createSoapBody(sb, soapBody, op.getOutput().getMessage(), message.getMessage(), op.getName() + "Response");
        }
    }

    public void createSoapHeaders(SOAPEnvelope soapEnv, List<SOAPHeader> headerDefs, Message msgdef, Map<String,Node> headers) throws AxisFault {
        for (SOAPHeader sh : headerDefs) handleSoapHeaderDef(soapEnv, sh, msgdef, headers);

        org.apache.axiom.soap.SOAPHeader soaphdr = soapEnv.getHeader();
        if (soaphdr == null) soaphdr = _soapFactory.createSOAPHeader(soapEnv);

        for (Node headerNode : headers.values())
            if (headerNode.getNodeType() == Node.ELEMENT_NODE) {
                if (soaphdr.getFirstChildWithName(new QName(headerNode.getNamespaceURI(), headerNode.getLocalName())) == null)
                    soaphdr.addChild(OMUtils.toOM((Element) headerNode, _soapFactory));
            } else {
                throw new OdeFault(__msgs.msgSoapHeaderMustBeAnElement(headerNode));
            }
    }

    @SuppressWarnings("unchecked")
    private void handleSoapHeaderDef(SOAPEnvelope soapEnv, SOAPHeader headerdef, Message msgdef, Map<String, Node> headers) throws AxisFault {
        boolean payloadMessageHeader = headerdef.getMessage() == null || headerdef.getMessage().equals(msgdef.getQName());

        if (headerdef.getPart() == null) return;

        if (payloadMessageHeader && msgdef.getPart(headerdef.getPart()) == null)
            throw new OdeFault(__msgs.msgSoapHeaderReferencesUnkownPart(headerdef.getPart()));

        Element srcPartEl = null;
        if (headers.size() > 0 && payloadMessageHeader){
            try {
                srcPartEl = (Element) headers.get(headerdef.getPart());
            } catch (ClassCastException e) {
                throw new OdeFault(__msgs.msgSoapHeaderMustBeAnElement(headers.get(headerdef.getPart())));
            }
        }

        // We don't complain about missing header data unless they are part of the message payload. This is
        // because AXIS may be providing these headers.
        if (srcPartEl == null && payloadMessageHeader)
            throw new OdeFault(__msgs.msgOdeMessageMissingRequiredPart(headerdef.getPart()));

        if (srcPartEl == null) return;

        org.apache.axiom.soap.SOAPHeader soaphdr = soapEnv.getHeader();
        if (soaphdr == null) {
            soaphdr = _soapFactory.createSOAPHeader(soapEnv);
        }

        OMElement omPart = OMUtils.toOM(srcPartEl, _soapFactory);
        for (Iterator<OMNode> i = omPart.getChildren(); i.hasNext();)
            soaphdr.addChild(i.next());
    }

    public SOAPFault createSoapFault(Element message, QName faultName, Operation op) throws AxisFault {
        OMElement detail = buildSoapDetail(message, faultName, op);

        SOAPFault fault = _soapFactory.createSOAPFault();
        SOAPFaultCode code = _soapFactory.createSOAPFaultCode(fault);
        code.setText(new QName(Namespaces.SOAP_ENV_NS, "Server"));
        SOAPFaultReason reason = _soapFactory.createSOAPFaultReason(fault);
        reason.setText(faultName);
        SOAPFaultDetail soapDetail = _soapFactory.createSOAPFaultDetail(fault);
        if (detail != null)
            soapDetail.addDetailEntry(detail);
        return fault;
    }

    private OMElement buildSoapDetail(Element message, QName faultName, Operation op) throws AxisFault {
        if (faultName.getNamespaceURI() == null)
            return toFaultDetail(faultName, message);
        if (op == null) {
            return toFaultDetail(faultName, message);
        }
        Fault f = op.getFault(faultName.getLocalPart());
        if (f == null)
            return toFaultDetail(faultName, message);

        // For faults, there will be exactly one part.
        Part p = (Part)f.getMessage().getParts().values().iterator().next();
        if (p == null)
            return toFaultDetail(faultName, message);
        Element partEl= DOMUtils.findChildByName(message,new QName(null,p.getName()));
        if (partEl == null)
            return toFaultDetail(faultName, message);
        Element detail = DOMUtils.findChildByName(partEl, p.getElementName());
        if (detail == null)
            return toFaultDetail(faultName, message);

        return OMUtils.toOM(detail, _soapFactory);
   }

    private OMElement toFaultDetail(QName fault, Element message) {
        if (message == null) return null;
        Element firstPart = DOMUtils.getFirstChildElement(message);
        if (firstPart == null) return null;
        Element detail = DOMUtils.getFirstChildElement(firstPart);
        if (detail == null) return OMUtils.toOM(firstPart, _soapFactory);
        return OMUtils.toOM(detail, _soapFactory);
    }

    public void parseSoapRequest(org.apache.ode.bpel.iapi.Message odeMessage, SOAPEnvelope envelope, Operation op) throws AxisFault {
        BindingOperation bop = _binding.getBindingOperation(op.getName(), null, null);

        if (bop == null)
            throw new OdeFault(__msgs.msgBindingOperationNotFound(_serviceName, _portName, op.getName()));

        BindingInput bi = bop.getBindingInput();
        if (bi == null)
            throw new OdeFault(__msgs.msgBindingInputNotFound(_serviceName, _portName, op.getName()));

        SOAPBody soapBody = getSOAPBody(bi);
        if (soapBody != null)
            extractSoapBodyParts(odeMessage, envelope.getBody(), soapBody, op.getInput().getMessage(), op.getName());

        if (envelope.getHeader() != null)
            extractSoapHeaderParts(odeMessage, envelope.getHeader(), getSOAPHeaders(bi), op.getInput().getMessage());
    }

    public void parseSoapResponse(org.apache.ode.bpel.iapi.Message odeMessage,
                                  SOAPEnvelope envelope, Operation op) throws AxisFault {
        BindingOperation bop = _binding.getBindingOperation(op.getName(), null, null);

        if (bop == null)
            throw new OdeFault(__msgs.msgBindingOperationNotFound(_serviceName, _portName, op.getName()));

        BindingOutput bo = bop.getBindingOutput();
        if (bo == null)
            throw new OdeFault(__msgs.msgBindingInputNotFound(_serviceName, _portName, op.getName()));

        SOAPBody soapBody = getSOAPBody(bo);
        if (soapBody != null)
            extractSoapBodyParts(odeMessage, envelope.getBody(), soapBody, op.getOutput().getMessage(), op.getName() + "Response");

        if (envelope.getHeader() != null)
            extractSoapHeaderParts(odeMessage, envelope.getHeader(), getSOAPHeaders(bo), op.getOutput().getMessage());
    }

    @SuppressWarnings("unchecked")
    public void createSoapBody(org.apache.axiom.soap.SOAPBody sb, SOAPBody soapBody, Message msgDef,
                               Element message, String rpcWrapper) throws AxisFault {
        OMElement partHolder = _isRPC ? _soapFactory
                .createOMElement(new QName(soapBody.getNamespaceURI(), rpcWrapper, "odens"), sb) : sb;

        List<Part> parts = msgDef.getOrderedParts(soapBody.getParts());
        for (Part part : parts) {
            Element srcPartEl = DOMUtils.findChildByName(message, new QName(null, part.getName()));
            if (srcPartEl == null)
                throw new OdeFault(__msgs.msgOdeMessageMissingRequiredPart(part.getName()));

            OMElement omPart = OMUtils.toOM(srcPartEl, _soapFactory);
            if (_isRPC) partHolder.addChild(omPart);
            else for (Iterator<OMNode> i = omPart.getChildren(); i.hasNext();) partHolder.addChild(i.next());
        }
    }

    // public Element createODEMessage(SOAPEnvelope soapEnv,Operation op) throws AxisFault {
    // }
    @SuppressWarnings("unchecked")
    public void extractSoapBodyParts(org.apache.ode.bpel.iapi.Message message, org.apache.axiom.soap.SOAPBody soapBody,
                                     SOAPBody bodyDef, Message msg,String rpcWrapper) throws AxisFault {

        List<Part> bodyParts = msg.getOrderedParts(bodyDef.getParts());

        if (_isRPC) {
            QName rpcWrapQName = new QName(bodyDef.getNamespaceURI(), rpcWrapper);
            OMElement partWrapper = soapBody.getFirstChildWithName(rpcWrapQName);
            if (partWrapper == null)
                throw new OdeFault(__msgs.msgSoapBodyDoesNotContainExpectedPartWrapper(_serviceName,_portName,rpcWrapQName));
            // In RPC the body element is the operation name, wrapping parts. Order doesn't really matter as far as
            // we're concerned. All we need to do is copy the soap:body children, since doc-lit rpc looks the same
            // in ode and soap.
            for (Part pdef : bodyParts) {
                OMElement srcPart = partWrapper.getFirstChildWithName(new QName(null, pdef.getName()));
                if (srcPart == null)
                    throw new OdeFault(__msgs.msgSOAPBodyDoesNotContainRequiredPart(pdef.getName()));
                message.setPart(srcPart.getLocalName(), OMUtils.toDOM(srcPart));
            }

        } else {
            // In doc-literal style, we expect the elements in the body to correspond (in order) to the
            // parts defined in the binding. All the parts should be element-typed, otherwise it is a mess.
            Iterator<OMElement> srcParts = soapBody.getChildElements();
            for (Part partDef : bodyParts) {
                if (!srcParts.hasNext())
                    throw new OdeFault(__msgs.msgSOAPBodyDoesNotContainRequiredPart(partDef.getName()));

                OMElement srcPart = srcParts.next();
                if (partDef.getElementName() == null)
                    throw new OdeFault(__msgs.msgBindingDefinesNonElementDocListParts());
                if (!srcPart.getQName().equals(partDef.getElementName()))
                    throw new OdeFault(__msgs.msgUnexpectedElementInSOAPBody(srcPart.getQName(), partDef.getElementName()));
                Document doc = DOMUtils.newDocument();
                Element destPart = doc.createElementNS(null, partDef.getName());
                destPart.appendChild(doc.importNode(OMUtils.toDOM(srcPart), true));
                message.setPart(partDef.getName(), destPart);
            }
        }
    }

    public void extractSoapHeaderParts(org.apache.ode.bpel.iapi.Message message,
                                       org.apache.axiom.soap.SOAPHeader soapHeader,
                                       List<SOAPHeader> headerDefs, Message msg) throws AxisFault {
        // Checking that the definitions we have are at least there
        for (SOAPHeader headerDef : headerDefs)
            handleSoapHeaderPartDef(message, soapHeader, headerDef, msg);

        // Extracting whatever header elements we find in the message, binding and abstract parts
        // aren't reliable enough given what people do out there.
        Iterator headersIter = soapHeader.getChildElements();
        while (headersIter.hasNext()) {
            OMElement header = (OMElement) headersIter.next();
            String partName = findHeaderPartName(headerDefs, header.getQName());
            message.setHeaderPart(partName, OMUtils.toDOM(header));
        }
    }

    private void handleSoapHeaderPartDef(org.apache.ode.bpel.iapi.Message odeMessage, org.apache.axiom.soap.SOAPHeader header, SOAPHeader headerdef,
            Message msgType) throws AxisFault {
        // Is this header part of the "payload" messsage?
        boolean payloadMessageHeader = headerdef.getMessage() == null || headerdef.getMessage().equals(msgType.getQName());
        boolean requiredHeader = payloadMessageHeader || (headerdef.getRequired() != null && headerdef.getRequired());

        if (requiredHeader && header == null)
            throw new OdeFault(__msgs.msgSoapHeaderMissingRequiredElement(headerdef.getElementType()));

        if (header == null)
            return;

        Message hdrMsg = _def.getMessage(headerdef.getMessage());
        if (hdrMsg == null)
            return;
        Part p = hdrMsg.getPart(headerdef.getPart());
        if (p == null || p.getElementName() == null)
            return;

        OMElement headerEl = header.getFirstChildWithName(p.getElementName());
        if (requiredHeader && headerEl == null)
            throw new OdeFault(__msgs.msgSoapHeaderMissingRequiredElement(headerdef.getElementType()));

        if (headerEl == null) return;

        odeMessage.setHeaderPart(p.getName(), OMUtils.toDOM(headerEl));
    }

    private String findHeaderPartName(List<SOAPHeader> headerDefs, QName elmtName) {
        for (SOAPHeader headerDef : headerDefs) {
            Message hdrMsg = _def.getMessage(headerDef.getMessage());
            for (Object o : hdrMsg.getParts().values()) {
                Part p = (Part) o;
                if (p.getElementName().equals(elmtName)) return p.getName();
            }
        }
        return elmtName.getLocalPart();
    }

    public static SOAPBody getSOAPBody(ElementExtensible ee) {
        return getFirstExtensibilityElement(ee, SOAPBody.class);
    }

    @SuppressWarnings("unchecked")
    public static List<SOAPHeader> getSOAPHeaders(ElementExtensible eee) {
        return CollectionsX.filter(new ArrayList<SOAPHeader>(), (Collection<Object>) eee.getExtensibilityElements(),
                SOAPHeader.class);
    }

    public static <T> T getFirstExtensibilityElement(ElementExtensible parent, Class<T> cls) {
        Collection<T> ee = CollectionsX.filter(parent.getExtensibilityElements(), cls);

        return ee.isEmpty() ? null : ee.iterator().next();

    }
    
    /**
     * Attempts to extract the WS-Addressing "Action" attribute value from the operation definition.
     * When WS-Addressing is being used by a service provider, the "Action" is specified in the 
     * portType->operation instead of the SOAP binding->operation.  
     * 
     * @param operation The name of the operation to extract the SOAP Action from
     * @return the SOAPAction value if one is specified, otherwise empty string
     */
    public String getWSAInputAction(String operation) {
      BindingOperation bop = _binding.getBindingOperation(operation, null, null);
      if (bop == null) return "";

      Input input = bop.getOperation().getInput();
      if (input != null) {
        Object actionQName = input.getExtensionAttribute(new QName(Namespaces.WS_ADDRESSING_NS, "Action"));
        if (actionQName != null && actionQName instanceof QName)
          return ((QName)actionQName).getLocalPart();
      }
      return "";
    }

    /**
     * Attempts to extract the SOAP Action is defined in the WSDL document.
     *
     * @param operation The name of the operation to extract the SOAP Action from
     * @return the SOAPAction value if one is specified, otherwise empty string
     */
    public String getSoapAction(String operation) {
        BindingOperation bop = _binding.getBindingOperation(operation, null, null);
        if (bop == null)
            return "";

        for (SOAPOperation soapOp : CollectionsX.filter(bop.getExtensibilityElements(), SOAPOperation.class))
            return soapOp.getSoapActionURI();

        return "";
    }
    
    public Fault parseSoapFault(Element odeMsgEl, SOAPEnvelope envelope, Operation operation) throws AxisFault {
        SOAPFault flt = envelope.getBody().getFault();
        SOAPFaultDetail detail = flt.getDetail();
        Fault fdef = inferFault(operation, flt);
        if (fdef == null)
            return null;

        Part pdef = (Part)fdef.getMessage().getParts().values().iterator().next();
        Element partel = odeMsgEl.getOwnerDocument().createElementNS(null,pdef.getName());
        odeMsgEl.appendChild(partel);

        if (detail.getFirstChildWithName(pdef.getElementName()) != null) {
            partel.appendChild(odeMsgEl.getOwnerDocument().importNode(
                    OMUtils.toDOM(detail.getFirstChildWithName(pdef.getElementName())), true));
        } else {
            partel.appendChild(odeMsgEl.getOwnerDocument().importNode(OMUtils.toDOM(detail),true));
        }

        return fdef;
    }

    @SuppressWarnings("unchecked")
    private Fault inferFault(Operation operation, SOAPFault flt) {
        if (flt.getDetail() == null || flt.getDetail().getFirstElement() == null)
            return null;

        // The detail is a dummy <detail> node containing the interesting fault element
        QName elName = flt.getDetail().getFirstElement().getQName();
        return WsdlUtils.inferFault(operation, elName);
    }

}
