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
package org.apache.ode.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.extension.bpel4restlight.Bpel4RestLightExtensionBundle;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.utils.DOMUtils;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * This class runs some initial test cases for the BPEL REST extension bundle
 * 
 * @author Michael Hahn (mhahn.dev@gmail.com)
 *
 */
public class RestExtensionActivitiesTest extends BPELTestAbstract {

	private HttpServer httpServer;

	private int port = 8085;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		// Register the extension bundle at the ODE server
		_server.registerExtensionBundle(new Bpel4RestLightExtensionBundle());

		// Start a test HTTP server for executing the REST extension activities
		httpServer = HttpServer.create(new InetSocketAddress(port), 0);

		// Create handler to reply HTTP calls
		httpServer.createContext("/test", new HttpHandler() {
			public void handle(HttpExchange exchange) throws IOException {
				handleHttpRequest(exchange);
			}
		});

		httpServer.start();
	}

	/**
	 * Tests the "GET" REST extension activity.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testGetExtAct() throws Throwable {
		go("/bpel/2.0/TestRestGetExtAct");
	}

	/**
	 * Tests the "POST" REST extension activity.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testPostExtAct() throws Throwable {
		go("/bpel/2.0/TestRestPostExtAct");
	}

	/**
	 * Tests the "POST" REST extension activity with a static wrapped request
	 * message.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testPostExtActWithWrappedRequest() throws Throwable {
		go("/bpel/2.0/TestRestPostExtAct2");
	}

	/**
	 * Tests the "PUT" REST extension activity.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testPutExtAct() throws Throwable {
		go("/bpel/2.0/TestRestPutExtAct");
	}

	/**
	 * Tests the "DELETE" REST extension activity.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testDeleteExtAct() throws Throwable {
		go("/bpel/2.0/TestRestDeleteExtAct");
	}

	/**
	 * Tests the HTTP status code variable of a REST extension activity.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testRestExtActStatusCode() throws Throwable {
		go("/bpel/2.0/TestRestExtActStatusCode");
	}

	/**
	 * Tests a BPEL REST extension activity with complex type variables.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testRestExtActComplexTypeVar() throws Throwable {
		go("/bpel/2.0/TestRestExtActComplexTypeVar");
	}

	/**
	 * Tests the "POST" REST extension activity with an invalid URL value message.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testPostExtActWithWrongURL() throws Throwable {
		deploy("/bpel/2.0/TestPostExtActWithWrongURL");

		Invocation inv = addInvoke("REST-POST#1", new QName("http://ode/bpel/unit-test.wsdl", "HelloService"), "hello",
				"<message><TestPart>Hello</TestPart></message>", null);
		inv.expectedFinalStatus = MessageExchange.Status.FAULT;
		inv.expectedInvokeException = FaultException.class;

		go();
	}

	private void handleHttpRequest(HttpExchange exchange) throws IOException {
		String method = exchange.getRequestMethod();

		if (method.toUpperCase().equals("GET")) {
			String response = "<service:getResponse xmlns:service=\"http://www.example.org/restApi\">\n"
					+ "                        <service:result>GET response data</service:result>\n"
					+ "                    </service:getResponse>";

			byte[] bResponse = response.getBytes();

			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, bResponse.length);
			exchange.getResponseBody().write(bResponse);
			exchange.close();
		} else if (method.toUpperCase().equals("POST")) {
			String request = IOUtils.toString(exchange.getRequestBody());

			String requestValue = "";
			try {
				Node reqNode = DOMUtils.stringToDOM(request);

				NodeList list = reqNode.getChildNodes();
				int i = 0;
				while (i < list.getLength()) {
					Node node = list.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE && ((Element) node).getLocalName().equals("value")) {
						requestValue = node.getTextContent();
					}
					i++;
				}

				String response = "<service:postResponse xmlns:service=\"http://www.example.org/restApi\">\n"
						+ "                        <service:result>" + requestValue + " Result</service:result>\n"
						+ "                    </service:postResponse>";

				byte[] bResponse = response.getBytes();

				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, bResponse.length);
				exchange.getResponseBody().write(bResponse);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
			}

			exchange.close();
		} else if (method.toUpperCase().equals("PUT")) {
			String request = IOUtils.toString(exchange.getRequestBody());

			String requestValue = "";
			try {
				Node reqNode = DOMUtils.stringToDOM(request);

				NodeList list = reqNode.getChildNodes();
				int i = 0;
				while (i < list.getLength()) {
					Node node = list.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE && ((Element) node).getLocalName().equals("value")) {
						requestValue = node.getTextContent();
					}
					i++;
				}

				String response = "<service:putResponse xmlns:service=\"http://www.example.org/restApi\">\n"
						+ "                        <service:result>" + requestValue + " Result</service:result>\n"
						+ "                    </service:putResponse>";

				byte[] bResponse = response.getBytes();

				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, bResponse.length);
				exchange.getResponseBody().write(bResponse);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
			}

			exchange.close();
		} else if (method.toUpperCase().equals("DELETE")) {
			if (exchange.getRequestURI().getPath().endsWith("/test")) {
				String response = "<service:deleteResponse xmlns:service=\"http://www.example.org/restApi\">\n"
						+ "                        <service:result>DELETE Method Test</service:result>\n"
						+ "                    </service:deleteResponse>";

				byte[] bResponse = response.getBytes();

				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, bResponse.length);
				exchange.getResponseBody().write(bResponse);
				exchange.close();
			} else {
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
				exchange.close();
			}
		} else {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
			exchange.close();
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();

		// Stop the server
		httpServer.stop(0);
	}
}
