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
package org.apache.ode.soap.mapping;

import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Part;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.xml.namespace.QName;
import java.util.*;



/**
 * <p>A class for writing SOAP envelopes based on a WSDL description and a "normalized"
 * input message. Some of this functioanlity used to be implemented in terms of SAAJ;
 * however, because SAAJ is generally a pain in the ass with lots of problems, this
 * class has been re-written to do native SOAP generation.
 * </p>
 *
 * <p>This class relies on a list of "instructions" that are generated in the constructor
 * based on the WSDL description. These instruction are "executed" to affect the generastion
 * of the SOAP message.
 * </p>
 *
 */
public class SOAPWriter {

	private static final Messages MSGS = MessageBundle.getMessages(Messages.class);
	private static final Log __log = LogFactory.getLog(SOAPWriter.class);

	/** A list of {@link Instruction}s that will actually write out the SOAP message for us. */
  private final List<Instruction> _instructions = new ArrayList<Instruction>();

  private String _soapAction;

  /**
   * Construct a {@link SOAPWriter} for a given operation and in a given direction.
   * @param soapOperationBindingModel
   * @param request direction, <code>true</code> for request, <code>false</code> for response.
   */
  SOAPWriter(SoapOperationBindingModel soapOperationBindingModel,  boolean request)
  {

    // We expect that given a validated soapOperationBindingModel, any unexpected
    // conditions are a result of programming error that failed to validate WSDL
    // adequately.

    String opName = soapOperationBindingModel.getOperation().getName();
    _soapAction = soapOperationBindingModel.getSOAPAction();
    BindingOperation bindOp = soapOperationBindingModel.getBindingOperation();
    BindingInput bindInput = bindOp.getBindingInput();
    if (request && bindInput == null)
      throw new NullPointerException("No input binding!");

    BindingOutput bindOutput = bindOp.getBindingOutput();
    if (!request && bindOutput == null)
      throw new NullPointerException("No output binding!");

    javax.wsdl.Message wsdlMsg = request
            ? soapOperationBindingModel.getRequestMessage()
            : soapOperationBindingModel.getResponseMessage();

    if (wsdlMsg == null)
      throw new NullPointerException("No wsdl:message for input/output binding!");

    Collection extElements = request
            ? bindInput.getExtensibilityElements()
            : bindOutput.getExtensibilityElements();

    addOp(new PushElement(Soap11Constants.QNAME_ENVELOPE));

    Collection soapHeaders = CollectionsX.filter(extElements,SOAPHeader.class);

    // Write instructions for HEADER elements.
    addOp(new PushElement(Soap11Constants.QNAME_HEADER));
    addOp(new WriteProtocolPart("wsaTo"));
    addOp(new WriteProtocolPart("wsaAction"));
    addOp(new WriteProtocolPart("sessionId"));
    addOp(new WriteProtocolPart("fromEndpoint"));

    for (Iterator iter = soapHeaders.iterator(); iter.hasNext();) {
      SOAPHeader wsdlSoapHeader = (SOAPHeader) iter.next();
      String part = wsdlSoapHeader.getPart();
      Part wsdlPart = wsdlMsg.getPart(part);
      if (wsdlPart == null)
        throw new NullPointerException(wsdlSoapHeader + " has null wsdlPart");

      QName type = wsdlPart.getElementName() != null ? wsdlPart.getElementName() : wsdlPart.getTypeName();
      if (type == null)
        throw new NullPointerException("Missing element/type for part '" + wsdlPart + "'");

      addOp(new WritePart(wsdlPart));
    }

    // Write a POP instruction (pops SOAP-ENV:Header)
    addOp(new Pop());

    Collection soapBodies = CollectionsX.filter(extElements, SOAPBody.class);
    if (soapBodies.size() > 1)
      throw new IllegalArgumentException("More than one SOAP body binding.");
    else if (!soapBodies.isEmpty()) {
      addOp(new PushElement(Soap11Constants.QNAME_BODY));
      for (Iterator iter = soapBodies.iterator(); iter.hasNext();) {
        SOAPBody wsdlSoapBodyExt = (SOAPBody) iter.next();

        // First try to get part ordering from SOAP body description
        Collection partNames = wsdlSoapBodyExt.getParts();
        // If that does not work, get it from the Operation description
        if (partNames == null || partNames.size() == 0)
          partNames = soapOperationBindingModel.getOperation().getParameterOrdering();
        // Last resort, we simply put out all the parts in rather arbitrary order
        if (partNames == null || partNames.size() == 0)
          partNames = wsdlMsg.getParts().keySet();

        if (soapOperationBindingModel.isRPCStyle()) {
          // Now, for RPC style, we first construct the operation element:
          addOp(new PushElement(new QName(wsdlSoapBodyExt.getNamespaceURI(),request ? opName : opName + "Response")));

          // Then we interate over all the parts:
          for (Iterator pIter = partNames.iterator();pIter.hasNext();) {
            String partName = (String) pIter.next();
            Part wsdlPart = wsdlMsg.getPart(partName);
            if (wsdlPart == null)
              throw new NullPointerException("Binding references undeclared part!");
            QName type = wsdlPart.getElementName() != null ? wsdlPart.getElementName() : wsdlPart.getTypeName();
            if (type == null)
              throw new NullPointerException("Missing element/type for part '" + wsdlPart + "'");
            addOp(new WritePart(wsdlPart));
          }

          addOp(new Pop()); // the operation element
        } else if (soapOperationBindingModel.isDocumentStyle()) {
          // For doc-lit, we expect only one part.
          if (partNames.size() != 1) {          
            throw new IllegalArgumentException("doc-literal used with multiple parts in message " +
                    wsdlMsg.getQName() + " (could also be no part at all)!");
          }
          Part wsdlPart = wsdlMsg.getPart((String) partNames.iterator().next());
          addOp(new WritePart(wsdlPart));
        }
      }
      addOp(new Pop());  // pop SOAP-ENV:Body
    }
    addOp(new Pop());  // pop SOAP-ENV:Envelope
  }

  SOAPWriter(SOAPFault soapFault, Part faultPart) {

    addOp(new PushElement(Soap11Constants.QNAME_ENVELOPE));
    addOp(new PushElement(Soap11Constants.QNAME_BODY));
    addOp(new PushElement(Soap11Constants.QNAME_FAULT));

    addOp(new PushElement(new QName(null, "faultcode")));
    addOp(new WriteText(Soap11Constants.QNAME_FAULT.getPrefix() + soapFault.getName()));
    addOp(new Pop());
    addOp(new PushElement(new QName(null, "faultstring")));
    addOp(new Pop());
    addOp(new PushElement(new QName(null, "detail")));
    addOp(new WritePart(faultPart));
    addOp(new Pop());

    addOp(new Pop());  // pop SOAP-ENV:Fault
    addOp(new Pop());  // pop SOAP-ENV:Body
    addOp(new Pop());  // pop SOAP-ENV:Envelope
  }

  /**
   * Write a framework message into SOAP format.
   */
  public void write(Document dest, Map<String, Element> partVals)
          throws SoapFormatException
  {
    Stack<Node> elStack = new Stack<Node>();
    elStack.push(dest);
    for (Iterator<Instruction> i = _instructions.iterator();i.hasNext() ; ) {
      i.next().write(dest, elStack, partVals);
    }

    elStack.pop();
    if (!elStack.isEmpty()) {
      throw new SoapFormatException("Stack not empty!");
    }
  }

  public String getSoapAction() {
    return _soapAction;
  }

  private void addOp(Instruction instruction) {
    _instructions.add(instruction);
  }

  interface Instruction {
    public void write(Document dest, Stack<Node> elStack, Map<String, Element> partVals)
        throws SoapFormatException;
  }

  private class Pop implements Instruction {
    public void write(Document dest, Stack<Node> elStack, Map<String, Element> partVals)
        throws SoapFormatException {
      elStack.pop();
    }
  }

  private class PushElement implements Instruction {
    private QName _elQName;

    PushElement(QName elName) {
      _elQName = elName;
    }

    public void write(Document dest, Stack<Node> elStack, Map<String, Element> partVals) throws SoapFormatException {
      Node top = elStack.peek();
      Element newEl = dest.createElementNS(_elQName.getNamespaceURI(),_elQName.getLocalPart());
      newEl.setPrefix(_elQName.getPrefix());
      top.appendChild(newEl);
      elStack.push(newEl);
    }

  }

  private class WriteText implements Instruction {
    private String _text;

    WriteText(String text) {
      _text = text;
    }

    public void write(Document dest, Stack<Node> elStack, Map<String, Element> partVals) throws SoapFormatException {
      Node top = elStack.peek();
      top.appendChild(dest.createTextNode(_text));
    }

  }

  private class WritePart implements Instruction {
    private Part _part;

    WritePart(Part part) {
      _part = part;
    }

    public void write(Document dest, Stack<Node> elementStack, Map<String, Element> partVals) throws SoapFormatException {
      Node top = elementStack.peek();
      Element partVal = partVals.get(_part.getName());

      // If we do not have a value, (i.e. null values) we /omit/ the accessor per SOAP spec.
      if (partVal == null) {
        return;
      }

      // Make sure the non-element (typed) parts have the proper wrapper element.
      if (_part.getTypeName() != null) {
      	if (partVal.getNamespaceURI() != null || !partVal.getLocalName().equals(_part.getName())) {
          String errmsg = MSGS.msgInvalidWrapperForNonElementPart(_part.getName(),_part.getTypeName(),
          		new QName(partVal.getNamespaceURI(),partVal.getLocalName()));
          __log.error(errmsg);
      		throw new SoapFormatException(errmsg);
      	}
      }
      top.appendChild(dest.importNode(partVal, true));
    }
  }

  private class WriteProtocolPart implements Instruction {
    private String _partName;

    WriteProtocolPart(String partname) {
      _partName = partname;
    }

    public void write(Document dest, Stack<Node> elementStack, Map<String, Element> partVals) throws SoapFormatException {
      Node top = elementStack.peek();
      Element partVal = partVals.get(_partName);

      // If we do not have a value, (i.e. null values) we /omit/ the accessor per SOAP spec.
      if (partVal == null) {
        return;
      }
      top.appendChild(dest.importNode(partVal, true));
    }

  }

}


