/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils;

import com.fs.utils.sax.FailOnErrorErrorHandler;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import common.TestResources;

import java.net.URL;

import junit.framework.TestCase;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XMLParserUtilsTest extends TestCase {
  
  public void testXMLReaderConfigurationGoodValidation() throws Exception {
    doParse(TestResources.getRetailerWSDL(),DOMUtils.WSDL_NS,TestResources.getWsdlSchema(),true);
  }
  
  public void testMultiSchemaGoodValidation() throws Exception {
    doParse(TestResources.getBpelExampleWsdl1(),
        new String[] {DOMUtils.WSDL_NS,
        "http://schemas.xmlsoap.org/ws/2003/05/partner-link/",
        "http://schemas.xmlsoap.org/ws/2003/03/business-process/"},
        new URL[] {
          TestResources.getWsdlSchema(),
          TestResources.getBpelPartnerLinkSchema(),
          TestResources.getBpelPropertySchema()},
          true);
  }
  
  public void testMultiSchemaBadValidation() throws Exception {
    doParse(TestResources.getBpelExampleWsdl1BadPLink(),
        new String[] {DOMUtils.WSDL_NS,
        "http://schemas.xmlsoap.org/ws/2003/05/partner-link/",
        "http://schemas.xmlsoap.org/ws/2003/03/business-process/"},
        new URL[] {
          TestResources.getWsdlSchema(),
          TestResources.getBpelPartnerLinkSchema(),
          TestResources.getBpelPropertySchema()},
          false);  }
  
  public void testXMLReaderConfigurationBadValidation() throws Exception {
    doParse(TestResources.getInvalidButWellFormedWsdl(),DOMUtils.WSDL_NS,TestResources.getWsdlSchema(),
        false);
  }
  
  public void testXMLReaderConfigurationWrongDocumentType() throws Exception {
    doParse(TestResources.getWsdlSchema(),DOMUtils.WSDL_NS,TestResources.getWsdlSchema(),false);
  }
  
  public void testXMLReaderConfigurationNoNamespaceWrongDocumentType()
    throws Exception
  {
    doParse(TestResources.getPlainOldXmlDocument(),DOMUtils.WSDL_NS,TestResources.getWsdlSchema(),
        false);
  }
    
  private void doParse(URL doc, String ns, URL schema, boolean succeed) throws Exception {
    doParse(doc, new String[] {ns}, new URL[] {schema}, succeed);
  }
  
  private void doParse(URL doc, String[] ns, URL[] schema, boolean succeed) throws Exception {
    XMLReader xr = XMLParserUtils.getXMLReader();
    DOMParser dp = XMLParserUtils.getDOMParser();
    for (int i=0; i < schema.length; ++i) {
      XMLParserUtils.addExternalSchemaURL(xr,ns[i],schema[i].toExternalForm());
      XMLParserUtils.addExternalSchemaURL(dp,ns[i],schema[i].toExternalForm());
    }
    try {
      InputSource is = new InputSource(doc.openStream());
      is.setSystemId(doc.toExternalForm());
      xr.setErrorHandler(new FailOnErrorErrorHandler());
      xr.parse(is);
    } catch (SAXException se) {
      if (succeed) {
        fail("SAX validation of " + doc.toExternalForm() + " should have succeeded; " +
            "instead, got: " + se.getMessage());
      }
      return;
    }
    if (!succeed) {
      fail("SAX validation of " + doc.toExternalForm() + " should have failed.");
    }
    try {
      InputSource is = new InputSource(doc.openStream());
      is.setSystemId(doc.toExternalForm());
      dp.setErrorHandler(new FailOnErrorErrorHandler());
      dp.parse(is);
    } catch (SAXException se) {
      if (succeed) {
        fail("DOM validation of " + doc.toExternalForm() + " should have succeeded; " +
            "instead, got: " + se.getMessage());
      }
      return;
    }
    if (!succeed) {
      fail("DOM validation of " + doc.toExternalForm() + " should have failed.");
    }
  }
  
  
}
