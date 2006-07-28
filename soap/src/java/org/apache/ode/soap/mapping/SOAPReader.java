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

import org.apache.ode.utils.DOMUtils;

import java.util.*;

import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SOAPReader {
  private final Map<String,PartReader> _partReaderMap = new HashMap<String,PartReader>();

  /** Construct a fault response reader. */
  SOAPReader(SoapOperationBindingModel soapOperationBindingModel, SOAPFault soapFault) {
    Fault fault = soapOperationBindingModel.getOperation().getFault(soapFault.getName());
    if (fault == null)
      throw new IllegalArgumentException("Unknown fault (has binding, but no abstract decl.): " + soapFault.getName());
    Message wsdlMessage = fault.getMessage();
    if (wsdlMessage.getParts().size() != 1)
      throw new IllegalArgumentException("Fault message must have exactly one part!");
    Part part = (Part) wsdlMessage.getParts().values().iterator().next();
    if (part.getElementName() == null)
      throw new IllegalArgumentException("Fault message must have element-typed part!");
    _partReaderMap.put(part.getName(), new FaultDetailPart(part.getElementName()));
  }

  /**
   * Construct a (non-fault) request/response reeader.
   */
  SOAPReader(SoapOperationBindingModel soapOperationBindingModel,  boolean request) {

    Message wsdlMessage = request ? soapOperationBindingModel.getRequestMessage() : soapOperationBindingModel.getResponseMessage();

    // One way operations don't do much parsing on the reverse route...
    if (wsdlMessage == null)
      return;

    for (Iterator i = wsdlMessage.getParts().values().iterator(); i.hasNext();) {
      Part part = (Part) i.next();
      ExtensibilityElement soapPartBinding = request
              ? soapOperationBindingModel.getRequestPartBinding(part.getName())
              : soapOperationBindingModel.getResponsePartBinding(part.getName());

      // The part could be mapped using HTTP or something non-SOAPy.
      if (soapPartBinding == null)
        continue;

      if (soapPartBinding instanceof SOAPBody) {
        if (soapOperationBindingModel.isRPCStyle()) {
          _partReaderMap.put(part.getName(), new RpcBodyPart(part.getName()));
        } else /* doc-literal */ {
          _partReaderMap.put(part.getName(), new DocLitBodyPart(part.getName()));
        }
      } else if (soapPartBinding instanceof SOAPHeader) {
        _partReaderMap.put(part.getName(), new HeaderPart(part.getName(), part.getElementName()));
      }
    }
  }

  public Element readPart(SoapMessage soapMessage, String part) throws SoapFormatException {
    PartReader partReader = _partReaderMap.get(part);
    if (partReader == null)
      throw new SoapFormatException("No such part: " + part);
    return partReader.readPart(soapMessage);
  }

  public Element readPart(Document soapMessage, String part) throws SoapFormatException {
    SoapMessage msg = new SoapMessage(soapMessage);
    return readPart(msg, part);
  }

  /** Get the part names that can  be read by this reader. */
  public Set<String> getParts() {
    return Collections.unmodifiableSet(_partReaderMap.keySet());
  }

  interface PartReader {
    Element readPart(SoapMessage msg) throws SoapFormatException;
  }

  /** Find doc-lit body part. */
  class DocLitBodyPart implements PartReader {
    // TODO still required?
    private String _partName;

    DocLitBodyPart(String partName) {
      _partName = partName;
    }

    public Element readPart(SoapMessage soapMsg) {
      Element body = soapMsg.getSoapBody();
      Element part = DOMUtils.getFirstChildElement(body);
      return part;
    }
  }

  /** Find RPC-lit body part. */
  class RpcBodyPart implements PartReader {
    private String _partName;

    RpcBodyPart(String partName) {
      _partName = partName;
    }

    public Element readPart(SoapMessage soapMsg) {
      Element body = soapMsg.getSoapBody();
      Element op = DOMUtils.getFirstChildElement(body);
      Element part = DOMUtils.findChildByName(op, new QName(null, _partName));
      return part;
    }
  }

  /** Find SOAP-header part. */
  class HeaderPart implements PartReader {
    private QName _elQName;

    HeaderPart(String partName, QName elementQName) {
      _elQName = elementQName;
    }

    public Element readPart(SoapMessage soapMsg) {
      Element header = soapMsg.getSoapHeader();
      Element part = DOMUtils.findChildByName(header,_elQName);
      return part;
    }
  }

  /** Find fault-detail part. */
  class FaultDetailPart implements PartReader {
    private QName _elQName;

    FaultDetailPart(QName elQName) {
      _elQName = elQName;
    }

    public Element readPart(SoapMessage soapMsg) {
      return soapMsg.getFaultDetail(_elQName);
    }
  }

}
