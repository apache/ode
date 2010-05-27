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
package org.apache.ode.utils;

import org.apache.ode.utils.TestResources;
import junit.framework.TestCase;
import org.apache.ode.utils.sax.FailOnErrorErrorHandler;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.net.URL;

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
