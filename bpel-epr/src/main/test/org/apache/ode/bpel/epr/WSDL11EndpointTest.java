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

package org.apache.ode.bpel.epr;

import junit.framework.TestCase;

import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A test for the WSDL11Endpoint class, to define how it works.
 *
 *
 * @author <a href="mailto:atoulme@intalio.com">Antoine Toulme</a>
 */
public class WSDL11EndpointTest extends TestCase {

    public void testAcceptServiceElement() {
        WSDL11Endpoint endpoint = new WSDL11Endpoint();
        Document doc = DOMUtils.newDocument();
        Node node = doc.createElementNS(Namespaces.WSDL_11, "service");
        node.appendChild(doc.createTextNode("http://example.com/hello"));
        assertTrue(endpoint.accept(node));
    }
    
    
    public void testAcceptServiceRefElement() {
        WSDL11Endpoint endpoint = new WSDL11Endpoint();
        Document doc = DOMUtils.newDocument();
        Node node =  doc.createElementNS(Namespaces.WS_BPEL_20_NS, "service-ref");
        Node child = doc.createElementNS(Namespaces.WSDL_11, "service");
        node.appendChild(child);
        child.appendChild(doc.createTextNode("http://example.com/hello"));
        assertTrue(endpoint.accept(node));
    }
    
    
    public void testAcceptServiceWithRandomElement() {
        WSDL11Endpoint endpoint = new WSDL11Endpoint();
        Document doc = DOMUtils.newDocument();
        Node child = doc.createElementNS("http://example.com/someNM", "helloHeloo");
        child.appendChild(doc.createTextNode("http://example.com/hello"));
        assertFalse("The endpoint should accept a random element", endpoint.accept(child));
    }
    
    public void testAcceptServiceWithTextNode() {
        WSDL11Endpoint endpoint = new WSDL11Endpoint();
        Document doc = DOMUtils.newDocument();
        assertFalse("The endpoint should accept a text node", 
                endpoint.accept(doc.createTextNode("http://example.com/hello")));
    }
}
