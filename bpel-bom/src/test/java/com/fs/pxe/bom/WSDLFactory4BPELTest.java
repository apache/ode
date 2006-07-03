/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom;

import com.fs.pxe.bom.wsdl.Definition4BPEL;
import com.fs.pxe.bom.wsdl.PartnerLinkType;
import com.fs.pxe.bom.wsdl.WSDLFactoryBPEL20;

import javax.wsdl.Definition;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

/**
 * Test for the WSDL4BPEL extensions.
 */
public class WSDLFactory4BPELTest extends TestCase {
  private Definition4BPEL def4;

  public void setUp() throws Exception {
    super.setUp();
    def4 = null;
  }

  public void testFactoryCreation() throws Exception {
    WSDLFactoryBPEL20.newInstance();
  }

  public void testFactoryExtended() throws Exception {
    assertTrue(WSDLFactoryBPEL20.newInstance() instanceof WSDLFactoryBPEL20);
  }

  public void testNewDefinition() throws Exception {
    WSDLFactoryBPEL20 factory = (WSDLFactoryBPEL20) WSDLFactoryBPEL20.newInstance();
    assertTrue(factory.newDefinition() instanceof Definition4BPEL);
  }

  public void testReadDefinition() throws Exception {
    WSDLFactoryBPEL20 factory = (WSDLFactoryBPEL20) WSDLFactoryBPEL20.newInstance();
    WSDLReader reader = factory.newWSDLReader();
    Definition def = reader.readWSDL("foo", new InputSource(getClass().getResourceAsStream("WSDLFactory4BPELTest.wsdl")));
    assertTrue(def instanceof Definition4BPEL);
    def4 = (Definition4BPEL) def;
  }

  public void testPartnerLinkType() throws Exception {
    /*
    <plnk:partnerLinkType name="outcomePartnerType">
    <plnk:role name="outcomeRole">
    <plnk:portType name="foo:testPort"/>
    </plnk:role>
    </plnk:partnerLinkType>
    */
    testReadDefinition();
    
    assertEquals(1, def4.getPartnerLinkTypes().size());
    assertTrue(def4.getPartnerLinkTypes().get(0) instanceof PartnerLinkType);
    PartnerLinkType plt = def4.getPartnerLinkTypes().get(0);
    assertEquals("outcomePartnerType", plt.getName().getLocalPart());
    assertEquals(def4.getTargetNamespace(), plt.getName().getNamespaceURI());
    assertEquals(1, plt.getRoles().size());
    assertNotNull(plt.getRole("outcomeRole"));
    assertEquals(plt.getRole("outcomeRole"), plt.getRoles().iterator().next());
    PartnerLinkType.Role role = plt.getRoles().iterator().next();
    assertEquals("outcomeRole", role.getName());
    assertEquals(new QName("http://test.ns", "testPort"), role.getPortType());
  }

  public void testProperties() throws Exception {
    testReadDefinition();
    assertEquals(3,def4.getProperties().size());

    assertEquals(def4.getTargetNamespace(), (def4.getProperties().get(0)).getName().getNamespaceURI());
    assertEquals("x", (def4.getProperties().get(0)).getName().getLocalPart());
    assertEquals("int", (def4.getProperties().get(0)).getPropertyType().getLocalPart());
    assertEquals("http://www.w3.org/2001/XMLSchema", (def4.getProperties().get(0)).getPropertyType().getNamespaceURI());

    assertEquals(def4.getTargetNamespace(), (def4.getProperties().get(1)).getName().getNamespaceURI());
    assertEquals("y", (def4.getProperties().get(1)).getName().getLocalPart());
    assertEquals("string", (def4.getProperties().get(1)).getPropertyType().getLocalPart());
    assertEquals("http://www.w3.org/2001/XMLSchema", (def4.getProperties().get(1)).getPropertyType().getNamespaceURI());

    assertEquals(def4.getTargetNamespace(), (def4.getProperties().get(2)).getName().getNamespaceURI());
    assertEquals("z", (def4.getProperties().get(2)).getName().getLocalPart());
    assertEquals("boolean", (def4.getProperties().get(2)).getPropertyType().getLocalPart());
    assertEquals("http://www.w3.org/2001/XMLSchema", (def4.getProperties().get(2)).getPropertyType().getNamespaceURI());
  }

  public void testPropertyAlias() throws Exception {
//    <bpws:propertyAlias propertyName="x" messageType="intWrapperMessage"
//      part="intPart"/>
//    <bpws:propertyAlias propertyName="y" messageType="stringWrapperMessage"
//      part="stringPart" />
//    <bpws:propertyAlias propertyName="z" messageType="booleanWrapperMessage"
//      part="result" />
    testReadDefinition();

    assertEquals(3, def4.getPropertyAliases().size());
    assertEquals(def4.getTargetNamespace(), (def4.getPropertyAliases().get(0)).getPropertyName().getNamespaceURI());
    assertNull((def4.getPropertyAliases().get(0)).getQuery());
    assertEquals(def4.getTargetNamespace(), (def4.getPropertyAliases().get(0)).getMessageType().getNamespaceURI());
  }

}
