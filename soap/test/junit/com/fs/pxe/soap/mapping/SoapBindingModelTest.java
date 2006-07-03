/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.soap.mapping;

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
