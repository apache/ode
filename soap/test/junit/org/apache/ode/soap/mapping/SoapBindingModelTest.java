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

import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

/**
 * TestCase for {@link SoapBindingModel}.
 */
public class SoapBindingModelTest extends TestCase {
  private URL wsdlURL;
  private Definition wsdl;
  private Service wsdlService;
  private Port wsdlPort;
  private SoapBindingModel model;
  private QName op1QName;

  public void setUp() throws Exception {
    wsdlURL = getClass().getResource("SoapWriteReadTest.wsdl");
    assertNotNull("SoapWriteReadTest.wsdl not found", wsdlURL);
    WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
    wsdlReader.setFeature("javax.wsdl.verbose", false);
    wsdl = wsdlReader.readWSDL(null, new InputSource(wsdlURL.openStream()));
    wsdlService = wsdl.getService(new QName(wsdl.getTargetNamespace(), "Service1"));
    wsdlPort = wsdlService.getPort("RpcPort1");
    model = new SoapBindingModel(wsdlPort);

    op1QName = new QName("uri:SoapReadWriteTest", "op1");
  }

  public void testGetTransportURI() throws Exception {
    assertEquals("http://schemas.xmlsoap.org/soap/http", model.getTransportURI());
  }

  public void testGetOperationByName() {
    assertNotNull(model.getOperation("op1"));
    assertEquals("op1", model.getOperation("op1").getOperation().getName());
    assertNull(model.getOperation("fooOp1"));
  }

  public void testGetOperationBySig() throws Exception {
    assertNotNull(model.findOperationBindingModel("", op1QName));
  }
}
