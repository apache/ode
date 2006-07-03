/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
//package com.fs.pxe.httpsoap;
//
//
//import com.fs.pxe.ra.PxeConnection;
//import com.fs.pxe.ra.PxeConnectionFactory;
//import com.fs.pxe.sfwk.bapi.ServiceProviderConfigImpl;
//import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
//import com.fs.pxe.sfwk.deployment.som.sax.SystemDescriptorFactory;
//import com.fs.pxe.sfwk.impl.mock.padapt.MockProtocolAdapterInteraction;
//import com.fs.pxe.sfwk.mngmt.SystemAdminMBean;
//import com.fs.pxe.sfwk.spi.Message;
//import com.fs.utils.DOMUtils;
//import junit.framework.TestCase;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//
//import javax.management.ObjectName;
//import java.net.URI;
//import java.net.URL;
//import java.util.Properties;
//
///**
// * Base case for testing against a WS-I BP 1.0 Retail Application.
// */
//public class RetailerBaseTCase extends TestCase {
//  /** WSDL URL of the Retailer. */
//  private String _wsdlUrl;
////  private InMemServer _binding;
//  private SimpleSystemDeploymentBundle sdd;
//  private PxeConnectionFactory pcf;
//  private PxeConnection conn;
//  private MockProtocolAdapterInteraction interaction;
//
//  private Document doc_getElementRequest;
//  private Document doc_getElementRequest_Invalid;
//  private Document doc_submitOrderRequest_Fault;
//
//
//	private Document doc_submitOrderRequest_Error;
//
//
//  public RetailerBaseTCase(String wsdlUrl) {
//    _wsdlUrl = wsdlUrl;
//  }
//
//  public void setUp() throws Exception {
////    com.fs.pxe.daomem.InMemDAOStoreConnectionFactoryImpl.clear();
////    _binding = new InMemServer("HttpSoapAdapterTestDomain");
////    _binding.installServiceProvider(new ServiceProviderConfigImpl("uri:SaajConnector",
////            com.fs.pxe.httpsoap.HttpSoapAdapter.class,
////            new Properties()));
////
////
////    _binding.installServiceProvider(new ServiceProviderConfigImpl("uri:MockProtocolAdapter",
////            com.fs.pxe.sfwk.impl.mock.padapt.MockProtocolAdapter.class,
////            new Properties()));
////
////    _binding.start();
//
//    sdd = new SimpleSystemDeploymentBundle();
//    SystemDescriptor sd = SystemDescriptorFactory.parseDescriptor(getClass().getResource("pxe-system1.xml"),
//            null, null, true);
//    sd.setWsdlUri(new URI(new URL(_wsdlUrl).toExternalForm()));
//    sdd.setSystemDescriptor(sd);
////    ObjectName systemON = _binding.getDomainNode().getDomainAdminMBean().deploySystemBundle(sdd);
////    SystemAdminMBean system = (SystemAdminMBean) _binding.resolve(systemON, SystemAdminMBean.class);
////    system.enable();
////    pcf = _binding.getPxeConnectionFactory();
//    conn = (PxeConnection) pcf.getConnection();
//    interaction = (MockProtocolAdapterInteraction) conn.createServiceProviderSession("uri:MockProtocolAdapter", MockProtocolAdapterInteraction.class);
//    interaction.setTargetService("MockAdapter");
//
//    doc_getElementRequest = loadXmlResource("getCatalogRequest.xml");
//    doc_getElementRequest_Invalid = loadXmlResource("getCatalogRequest_Invalid.xml");
//    doc_submitOrderRequest_Fault = loadXmlResource("submitOrderRequest_Fault.xml");
//    doc_submitOrderRequest_Error = loadXmlResource("submitOrderRequest_Error.xml");
//  }
//
//  private Document loadXmlResource(String s) throws Exception {
//    return DOMUtils.parse(getClass().getResourceAsStream(s));
//  }
//
//  public void testGetCatalogGood() throws Exception {
//    String result = interaction.sendMessage("getCatalog", DOMUtils.domToString(doc_getElementRequest.getDocumentElement()));
//    Element retVal = DOMUtils.stringToDOM(result);
//    assertNotNull(retVal);
//    assertEquals("message", retVal.getLocalName());
//    assertEquals(Message.PXE_MESSAGE_QNAME.getNamespaceURI(), retVal.getNamespaceURI());
//    Element firstChild = (Element)retVal.getFirstChild();
//    assertNotNull(firstChild);
//    assertEquals("return", firstChild.getLocalName()); // per WSDL
//    assertNull(firstChild.getNamespaceURI()); // this is a "type" part, so it has null NS.
//
//  }
//
//  public void testGetCatalogMT() throws Exception {
//    Thread[] threads = new Thread[50];
//
//    for (int i = 0; i < threads.length;++i) {
//      threads[i] = new Thread() {
//        public void run() {
//          try {
//            PxeConnection conn1 = (PxeConnection) pcf.getConnection();
//            MockProtocolAdapterInteraction interaction1 = (MockProtocolAdapterInteraction) conn.createServiceProviderSession("uri:MockProtocolAdapter", MockProtocolAdapterInteraction.class);
//            interaction1.setTargetService("MockAdapter");
//            String result = interaction1.sendMessage("getCatalog", DOMUtils.domToString(doc_getElementRequest.getDocumentElement()));
//            assertNotNull(result);
//            for (int j = 0; j < 10; ++j)
//              testGetCatalogGood();
//          } catch (Exception ex) {
//            ex.printStackTrace();
//            fail("error");
//          }
//        }
//      };
//    }
//
//    for (int i = 0; i < threads.length; ++i)
//      threads[i].start();
//    for (int i = 0; i < threads.length; ++i)
//      threads[i].join();
//  }
//
//  public void testFault() throws Exception {
//		Element retVal = DOMUtils.stringToDOM(interaction.sendMessage("submitOrder", DOMUtils.domToString(doc_submitOrderRequest_Fault.getDocumentElement())));
//		assertNotNull(retVal);
//    assertEquals("message", retVal.getLocalName());
//    assertEquals(Message.PXE_MESSAGE_QNAME.getNamespaceURI(), retVal.getNamespaceURI());
//    Element firstChild = (Element)retVal.getFirstChild();
//    assertNotNull(firstChild);
//    assertEquals("InvalidProductCode", firstChild.getLocalName()); // per WSDL
//    assertEquals("http://www.ws-i.org/SampleApplications/SupplyChainManagement/2002-08/RetailOrder.xsd", firstChild.getNamespaceURI()); // this is a "type" part, so it has null NS.
//  }
//
//  /**
//   * This test will cause a server error with a fault
//   * not described in WSDL; this will cause a soap mapping exception.
//   * @throws Exception
//   */
//  public void testServerError() throws Exception {
//    String result = interaction.sendMessage("submitOrder", DOMUtils.domToString(doc_submitOrderRequest_Error.getDocumentElement()));
//    assertEquals("FAILURE", result);
//  }
//
//
//  public void testGetCatalogBad() throws Exception {
//    try {
//      Element retVal = DOMUtils.stringToDOM(interaction.sendMessage("getCatalog", DOMUtils.domToString(doc_getElementRequest_Invalid.getDocumentElement())));
//      fail("Expected exception due to invalid message body. ");
//    } catch (Exception ex) {
//      assertNotNull(ex.getCause());
//    }
//
//  }
//}
