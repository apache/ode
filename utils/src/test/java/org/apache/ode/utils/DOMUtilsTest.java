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

import net.sf.saxon.dom.DocumentBuilderFactoryImpl;
import net.sf.saxon.xqj.SaxonXQDataSource;

import org.apache.ode.utils.TestResources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Test the {@link DOMUtils} class.
 */
public class DOMUtilsTest extends TestCase {
    private static final String SAXON_DOM_DOCUMENT_BUILDER_FACTORY = "net.sf.saxon.dom.DocumentBuilderFactoryImpl";
    private static final String DOCUMENT_BUILDER_FACTORY = "javax.xml.parsers.DocumentBuilderFactory";
    String defaultBuilderFactory = null;

  @Override
  protected void setUp() throws Exception {
        super.setUp();
        defaultBuilderFactory = System.getProperty(DOCUMENT_BUILDER_FACTORY, "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
  }
  
  @Override
  protected void tearDown() throws Exception {
        super.tearDown();
        
        System.setProperty(DOCUMENT_BUILDER_FACTORY, defaultBuilderFactory);
  }
  
  public void testParseInputStream() throws Exception {
    Document doc = DOMUtils.parse(TestResources.getLoanApprovalProcess().openStream());
    assertEquals("process", doc.getDocumentElement().getLocalName());
    assertEquals("http://schemas.xmlsoap.org/ws/2003/03/business-process/",
        doc.getDocumentElement().getNamespaceURI());
  }

  public void testParseInputSource() throws Exception {
    Document doc = DOMUtils.parse(TestResources.getLoanApprovalProcess().openStream());
    assertEquals("process", doc.getDocumentElement().getLocalName());
    assertEquals("http://schemas.xmlsoap.org/ws/2003/03/business-process/",
        doc.getDocumentElement().getNamespaceURI());
  }

  public void testNewDocument() throws Exception {
    assertNotNull(DOMUtils.newDocument());
  }

  public void testSerializeDom() throws Exception {
  	Document doc = DOMUtils.newDocument();
  	Element foo = doc.createElement("foo");
  	Element bar = doc.createElement("bar");
  	doc.appendChild(foo);
  	foo.appendChild(bar);

    assertEquals(foo.toString(), DOMUtils.stringToDOM(DOMUtils.domToString(foo)).toString());
    assertEquals(bar.toString(), DOMUtils.stringToDOM(DOMUtils.domToString(bar)).toString());
    // TODO check the document itself
  }

  public void testConcurrentParse() throws Exception {
    final int SIZE = 100;
    Thread[] threads = new Thread[SIZE];
    for (int i = 0; i < SIZE; ++i) {
      threads[i] = new Thread() {
        public void run() {
          try {
            testParseInputSource();
          }
          catch (Exception ex) {
            fail("Exception: " + ex);
          }
        }
      };

    }

    for (int i = 0; i < SIZE; ++i) {
      threads[i].start();
    }

    for (int i = 0; i < SIZE; ++i) {
      threads[i].join();
    }

  }

  public void testIsEmptyElement() throws Exception {
    InputStream is = TestResources.getDummyXML().openStream();
    assertTrue(is != null);
    Document doc = DOMUtils.parse(is);
    Element test = doc.getDocumentElement();
    NodeList nl = test.getChildNodes();
    int len = nl.getLength();
    for (int i = 0; i < len; ++i) {
      if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element)nl.item(i);
        if (el.getNodeName().equals("empty")) {
          assertTrue(DOMUtils.isEmptyElement(el));
        }
        else {
          assertTrue(!DOMUtils.isEmptyElement(el));
        }
      }
    }
  }
  
  public void testCloneNode() throws Exception {
    String testString = "<ns1:parent xmlns:ns1=\"abc\">\n" +
   "  <ns1:child xmlns=\"def\">\n" +
   "     <ns2:nestedChild xmlns:ns2=\"def\"/>\n" +
   "  </ns1:child>\n" +
   "</ns1:parent>";
    
    Document doc = DOMUtils.parse(new ByteArrayInputStream(testString.getBytes()));
    Node node = doc.getFirstChild();
    Node clonedNode = DOMUtils.cloneNode(doc, node);
    String actualString = DOMUtils.domToString(clonedNode).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "");
    assertEquals("XML Result", testString, actualString);
  }
  
  public void testCloneNodeNewDocument() throws Exception {
      String testString = "<ns1:parent xmlns:ns1=\"abc\">\n" +
     "  <ns1:child xmlns=\"def\">\n" +
     "     <ns2:nestedChild xmlns:ns2=\"def\"/>\n" +
     "  </ns1:child>\n" +
     "</ns1:parent>";
      
      Document doc = DOMUtils.parse(new ByteArrayInputStream(testString.getBytes()));
      Document doc2 = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Node node = doc.getFirstChild();
      Node clonedNode = DOMUtils.cloneNode(doc2, node);
      
      assertNotSame("DOM's are the same", doc, doc2);
      String actualString = DOMUtils.domToString(clonedNode).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "");
      assertEquals("XML Result", testString, actualString);
          
  }
  
  /**
   * A Saxon's DOM is read only. cloneNode should throw an UnsupportedOperationException.
   * 
   * @throws Exception
   */
  public void testCloneNodeSaxon() throws Exception {
      String testString = "<ns1:parent xmlns:ns1=\"abc\">\n" +
     "  <ns1:child xmlns=\"def\">\n" +
     "     <ns2:nestedChild xmlns:ns2=\"def\"/>\n" +
     "  </ns1:child>\n" +
     "</ns1:parent>";
      
      Document doc = createSaxonDOM(testString);
      
      Node node = doc.getFirstChild();
      try {
          Node clonedNode = DOMUtils.cloneNode(doc, node);
      } catch (UnsupportedOperationException ex) {
          
      }
      
    }
    
    public void testCloneNodeNewDocumentSaxon() throws Exception {
        String testString = "<ns1:parent xmlns:ns1=\"abc\">\n" +
       "  <ns1:child xmlns=\"def\">\n" +
       "     <ns2:nestedChild xmlns:ns2=\"def\"/>\n" +
       "  </ns1:child>\n" +
       "</ns1:parent>";

        String saxonString = "<ns1:parent xmlns:ns1=\"abc\">\n" +
        "  <ns1:child xmlns=\"def\">\n" +
        "     <nestedChild xmlns:ns2=\"def\"/>\n" +
        "  </ns1:child>\n" +
        "</ns1:parent>";
        
        Document doc = createSaxonDOM(testString);
        Document doc2 = DOMUtils.newDocument();
        Node node = doc.getFirstChild();
        Node clonedNode = DOMUtils.cloneNode(doc2, node);
        
        assertNotSame("DOM's are the same", doc, doc2);
        String actualString = DOMUtils.domToString(clonedNode).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "");
        assertEquals("XML Result", saxonString, actualString);
            
    }
    
    public void testSaxonXQueryResultValueClone() throws Exception {
       String testString = "<test:test1 xmlns:test=\"http://test.org\">\n" +
      "  <test:test2>asdf</test:test2>\n" +
      "</test:test1>";
       
       Document doc = DOMUtils.parse(new ByteArrayInputStream(testString.getBytes()));
       
       XQDataSource ds = new SaxonXQDataSource();
       XQConnection conn = ds.getConnection();
       XQPreparedExpression exp = conn.prepareExpression(testString);
       
       XQResultSequence rs = exp.executeQuery();
       rs.next();

       XQItem xqitem = rs.getItem();
       Node node = xqitem.getNode();
       Node clonedNode = DOMUtils.cloneNode(DOMUtils.newDocument(), node);
       assertNotNull(clonedNode);
       
    }
    
    

    private Document createSaxonDOM(String testString)
            throws ParserConfigurationException, SAXException, IOException {
        System.setProperty(DOCUMENT_BUILDER_FACTORY, SAXON_DOM_DOCUMENT_BUILDER_FACTORY);
        DocumentBuilderFactory factory = DocumentBuilderFactoryImpl.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder saxonBuilder = factory.newDocumentBuilder();
        Document doc = saxonBuilder.parse(new ByteArrayInputStream(testString.getBytes()));
        return doc;
    }
    
}
