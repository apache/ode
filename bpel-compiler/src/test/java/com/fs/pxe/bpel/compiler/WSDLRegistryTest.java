/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.wsdl.Definition4BPEL;
import com.fs.pxe.bom.wsdl.WSDLFactory4BPEL;
import com.fs.pxe.bom.wsdl.WSDLFactoryBPEL11;
import com.fs.utils.xsd.SchemaModel;

import java.net.URL;

import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class WSDLRegistryTest extends TestCase {

  private WSDLRegistry _registry;

  public WSDLRegistryTest() {
    super();
  }

  protected void setUp() throws Exception
  {
    super.setUp();
    _registry = new WSDLRegistry(null, null);
  }

  protected void tearDown() throws Exception
  {
    _registry = null;
    super.tearDown();
  }

  public void testInitialModel() throws Exception
  {
    // access initial dummy model
    SchemaModel m = _registry.getSchemaModel();
    assertNotNull(m);

    // verify dummy model
    QName type = new QName("http://fivesight.com/bogus/namespace", "__bogusType__");
    assertTrue(m.knowsSchemaType(type));
  }

  public void testAddWSDL() throws Exception {
    URL wsd = getClass().getResource("/1.1/good/test.wsdl");

    // load & register wsdl
    WSDLFactory4BPEL factory = (WSDLFactory4BPEL)WSDLFactoryBPEL11.newInstance();
    WSDLReader reader = factory.newWSDLReader();
    WsdlFinder finder = new DefaultWsdlFinder();
    Definition4BPEL def = finder.loadDefinition(reader, wsd.toURI());
    _registry.addDefinition(def);

    // access model
    SchemaModel m = _registry.getSchemaModel();
    assertNotNull(m);

    assertTrue("WSDL-Define type not visible.",m.knowsSchemaType(new QName("uri:testing", "TComplex1")));
    assertTrue("Type from import not visible.",m.knowsSchemaType(new QName("uri:test1", "TComplex2")));
    
    
  }
  
}
