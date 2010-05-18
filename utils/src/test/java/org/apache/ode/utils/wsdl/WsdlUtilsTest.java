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

package org.apache.ode.utils.wsdl;

import junit.framework.TestCase;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class WsdlUtilsTest extends TestCase {
    private Definition definition;
    private Service dummyService;

    protected void setUp() throws Exception {
        super.setUp();

        URL wsdlURL = getClass().getResource("/wsdl-utils.wsdl");
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        definition = wsdlReader.readWSDL(wsdlURL.toURI().toString());
        dummyService = definition.getService(new QName("http://axis2.ode.apache.org", "DummyService"));
    }

    public void testNoBinding() {
        Port noBindingPort = dummyService.getPort("DummyService_port_with_no_binding");

        try {
            WsdlUtils.getBindingExtension(noBindingPort);
            fail("IllegalArgumentException expected!");
        } catch (IllegalArgumentException e) {
            // expected behavior
        }
    }


    public void testEmptyBinding() {
        Port noBindingPort = dummyService.getPort("DummyService_port_with_empty_binding");
        assertNull("should return null", WsdlUtils.getBindingExtension(noBindingPort));
    }

    public void testMultipleBinding() {
        // don't know how to test this edge case
        assertTrue(true);
    }

    public void testGetBindingExtension() {
        Port[] ports = new Port[]{
                dummyService.getPort("DummyServiceSOAP11port_http"),
                dummyService.getPort("DummyServiceHttpport")
        };
        for (Port port : ports) {
            try {
                ExtensibilityElement elt = WsdlUtils.getBindingExtension(port);
                assertNotNull("Non-null element expected!", elt);
            } catch (Exception e) {
                fail("No exception should be thrown!");
            }
        }
    }

    public void testUseSOAPBinding() {
        Port soapPort = dummyService.getPort("DummyServiceSOAP11port_http");
        Port httpPort = dummyService.getPort("DummyServiceHttpport");

        assertTrue(WsdlUtils.useSOAPBinding(soapPort));
        assertFalse(WsdlUtils.useSOAPBinding(httpPort));
    }

    public void testUseHTTPBinding() {
        Port soapPort = dummyService.getPort("DummyServiceSOAP11port_http");
        Port httpPort = dummyService.getPort("DummyServiceHttpport");

        assertTrue(WsdlUtils.useHTTPBinding(httpPort));
        assertFalse(WsdlUtils.useHTTPBinding(soapPort));
    }


    public void testGetOperationExtension() {
        Port[] ports = new Port[]{
                dummyService.getPort("DummyServiceSOAP11port_http"),
                dummyService.getPort("DummyServiceHttpport")
        };
        for (Port port : ports) {
            BindingOperation bindingOperation = port.getBinding().getBindingOperation("hello", null, null);
            ExtensibilityElement operationExtension = WsdlUtils.getOperationExtension(bindingOperation);
            assertNotNull("Operation Binding expected!", operationExtension);
        }
    }

    public void testUseUrlEncoded() {
        for (Object o : dummyService.getPorts().entrySet()) {
            Map.Entry e = (Map.Entry) o;
            String portName = (String) e.getKey();
            Port port = (Port) e.getValue();
            Binding binding = port.getBinding();
            if (binding == null) continue; // some bindings intentionally missing
            BindingOperation bindingOperation = binding.getBindingOperation("hello", null, null);
            if (bindingOperation == null) continue; // some bindings intentionally empty
            if ("DummyServiceHttpport_urlEncoded".equals(portName)) {
                assertTrue(WsdlUtils.useUrlEncoded(bindingOperation.getBindingInput()));
            } else {
                assertFalse(WsdlUtils.useUrlEncoded(bindingOperation.getBindingInput()));
            }
        }
    }

    public void testUseUrlReplacement() {
        for (Iterator it = dummyService.getPorts().entrySet().iterator(); it.hasNext();) {
            Map.Entry e = (Map.Entry) it.next();
            String portName = (String) e.getKey();
            Port port = (Port) e.getValue();
            Binding binding = port.getBinding();
            if (binding == null) continue; // some bindings intentionally missing
            BindingOperation bindingOperation = binding.getBindingOperation("hello", null, null);
            if (bindingOperation == null) continue; // some bindings intentionally empty
            if ("DummyServiceHttpport_urlReplacement".equals(portName)) {
                assertTrue(WsdlUtils.useUrlReplacement(bindingOperation.getBindingInput()));
            } else {
                assertFalse(WsdlUtils.useUrlReplacement(bindingOperation.getBindingInput()));
            }
        }
    }

    public void testUseMimeMultipartRelated() {
        for (Iterator it = dummyService.getPorts().values().iterator(); it.hasNext();) {
            Port port = (Port) it.next();
            Binding binding = port.getBinding();
            if (binding == null) continue; // some bindings intentionally missing
            BindingOperation bindingOperation = binding.getBindingOperation("hello", null, null);
            if (bindingOperation == null) continue; // some bindings intentionally empty
            for (int i = 0; i < binding.getBindingOperations().size(); i++) {
                BindingOperation operation = (BindingOperation) binding.getBindingOperations().get(i);
                assertFalse(WsdlUtils.useMimeMultipartRelated(operation.getBindingInput()));
            }
        }
    }

    public void testGetAddresExtgension() {
        for (Iterator it = dummyService.getPorts().entrySet().iterator(); it.hasNext();) {
            Map.Entry e = (Map.Entry) it.next();
            Port port = (Port) e.getValue();

            if ("DummyService_port_with_empty_binding".equals(port.getName())
                    || "DummyService_port_with_no_binding".equals(port.getName())) {
                continue;
            }

            if (WsdlUtils.useHTTPBinding(port)) {
                HTTPAddress add = (HTTPAddress) WsdlUtils.getAddressExtension(port);
                assertNotNull("Address expected", add);
                assertNotNull("Non-null Location expected", add.getLocationURI());
                assertTrue("Non-empty Location expected", add.getLocationURI().length() > 0);
            } else if (WsdlUtils.useHTTPBinding(port)) {
                SOAPAddress add = (SOAPAddress) WsdlUtils.getAddressExtension(port);
                assertNotNull("Address expected", add);
                assertNotNull("Non-null Location expected", add.getLocationURI());
                assertTrue("Non-empty Location expected", add.getLocationURI().length() > 0);
            }
        }
    }

    public void testGetMimeContentType() {
        Binding binding = definition.getBinding(new QName("http://axis2.ode.apache.org", "DummyServiceHttpBinding"));
        BindingOperation operation = binding.getBindingOperation("hello", null, null);
        MIMEContent mimeContent = WsdlUtils.getMimeContent(operation.getBindingInput().getExtensibilityElements());
        assertNotNull("A MIME Content is expected!", mimeContent);
        assertEquals("text/xml", mimeContent.getType());

        binding = definition.getBinding(new QName("http://axis2.ode.apache.org", "DummyServiceSOAP11Binding"));
        operation = binding.getBindingOperation("hello", null, null);
        mimeContent = WsdlUtils.getMimeContent(operation.getBindingInput().getExtensibilityElements());
        assertNull("No content-type expected here!", mimeContent);
    }

     public void testGetHeaders() {
        Binding binding = definition.getBinding(new QName("http://axis2.ode.apache.org", "DummyServiceHttpBinding"));
        BindingOperation operation = binding.getBindingOperation("hello", null, null);
        Collection headers = WsdlUtils.getHttpHeaders(operation.getBindingOutput().getExtensibilityElements());
        assertNotNull("A header is expected!", headers);
        assertTrue("A header is expected!", headers.size()==1);
    }
}
