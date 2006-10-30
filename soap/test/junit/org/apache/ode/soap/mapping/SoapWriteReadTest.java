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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class SoapWriteReadTest extends TestCase {
  public static final String WSDLURL = "http://soap.systinet.net/ws/WSI/RetailerService/wsdl";
  Document tpart1;
  Document tpart2;
  Document elpart1;
  Document elpart2;
  Map<String, Element> parts;
  URL wsdlURL;
  URL soapMsgURL;
  Definition wsdl;
  Service wsdlService;
  Port wsdlPort;

  protected void setUp() throws Exception {
    wsdlURL = getClass().getResource("SoapWriteReadTest.wsdl");
    assertNotNull("SoapWriteReadTest.wsdl not found", wsdlURL);
    soapMsgURL = getClass().getResource("testRequest.soap");
    WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
    wsdlReader.setFeature("javax.wsdl.verbose", false);
    wsdl = wsdlReader.readWSDL(null, new InputSource(wsdlURL.openStream()));
    wsdlService = wsdl.getService(new QName("uri:SoapReadWriteTest", "Service1"));
    wsdlPort = wsdlService.getPort("RpcPort1");
    tpart1 = DOMUtils.newDocument();
    tpart1.appendChild(tpart1.createElementNS(null, "TypePart1"));
    tpart1.getDocumentElement().appendChild(tpart1.createTextNode("abc"));
    tpart2 = DOMUtils.newDocument();
    tpart2.appendChild(tpart2.createElementNS(null, "TypePart2"));
    tpart2.getDocumentElement().appendChild(tpart2.createTextNode("123"));
    elpart1 = DOMUtils.parse(wsdlURL.openStream());
    elpart2 = DOMUtils.parse(wsdlURL.openStream());

    parts = new HashMap<String, Element>();
    parts.put("TypePart1", tpart1.getDocumentElement());
    parts.put("TypePart2", tpart2.getDocumentElement());
    parts.put("ElementPart1", elpart1.getDocumentElement());
    parts.put("ElementPart2", elpart2.getDocumentElement());
  }

  public void testSoapWrite() throws Exception {
    SoapBindingModel soapBindingModel = new SoapBindingModel(wsdlPort);
    SoapOperationBindingModel soapOperationBindingModel = soapBindingModel.getOperation("op1");
    SOAPWriter writer = new SOAPWriter(soapOperationBindingModel, true);
    Document soap = DOMUtils.newDocument();
    writer.write(soap, parts);
  }

  public void testSoapRead() throws Exception {
    SoapBindingModel soapBindingModel = new SoapBindingModel(wsdlPort);
    SoapOperationBindingModel soapOperationBindingModel = soapBindingModel.getOperation("op1");
    Document soap = DOMUtils.parse(soapMsgURL.openStream());
    soapOperationBindingModel = soapBindingModel.findOperationBindingModel("", new SoapMessage(soap)
        .getPayloadQName());
    assertNotNull(soapOperationBindingModel);
    SOAPReader reader = new SOAPReader(soapOperationBindingModel, true);
    Element inTpart1 = reader.readPart(soap, "TypePart1");
    assertNotNull(inTpart1);
    Element inTpart2 = reader.readPart(soap, "TypePart2");
    assertNotNull(inTpart2);

  }

  public void testSoapWriteRead() throws Exception {
    SoapBindingModel soapBindingModel = new SoapBindingModel(wsdlPort);
    SoapOperationBindingModel soapOperationBindingModel = soapBindingModel.getOperation("op1");
    SOAPWriter writer = new SOAPWriter(soapOperationBindingModel, true);
    Document soap = DOMUtils.newDocument();
    writer.write(soap, parts);
    System.err.println(DOMUtils.domToString(soap));
    soapOperationBindingModel = soapBindingModel.findOperationBindingModel("", new SoapMessage(soap)
        .getPayloadQName());
    assertNotNull(soapOperationBindingModel);
    SOAPReader reader = new SOAPReader(soapOperationBindingModel, true);
    Element inTpart1 = reader.readPart(soap, "TypePart1");
    assertNotNull(inTpart1);
    Element inTpart2 = reader.readPart(soap, "TypePart2");
    assertNotNull(inTpart2);
    Element inElPart1 = reader.readPart(soap, "ElementPart1");
    assertNotNull(inElPart1);
  }
}
