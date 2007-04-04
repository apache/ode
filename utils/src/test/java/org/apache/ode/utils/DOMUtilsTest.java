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

import java.io.InputStream;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Test the {@link DOMUtils} class.
 */
public class DOMUtilsTest extends TestCase {

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

}
