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

package org.apache.ode.axis2.httpbinding;

import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HttpMethodConverterTest extends TestCase {

    protected Definition definition;

    protected HttpMethodConverter deliciousBuilder;
    protected Binding deliciousBinding;
    protected Port deliciousPort;

    protected HttpMethodConverter dummyBuilder;
    protected Port dummyPort;
    protected Binding dummyBinding;

    protected void setUp() throws Exception {
        super.setUp();

        URL wsdlURL = getClass().getResource("/http-method-builder.wsdl");
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        definition = wsdlReader.readWSDL(wsdlURL.toURI().toString());

        Service deliciousService = definition.getService(new QName("http://ode/bpel/unit-test.wsdl", "DeliciousService"));
        deliciousPort = deliciousService.getPort("TagHttpPort");
        deliciousBinding = deliciousPort.getBinding();
        deliciousBuilder = new HttpMethodConverter(definition, deliciousService.getQName(), deliciousPort.getName());

        Service dummyService = definition.getService(new QName("http://ode/bpel/unit-test.wsdl", "DummyService"));
        dummyPort = dummyService.getPort("DummyServiceHttpport");
        dummyBinding = dummyPort.getBinding();
        dummyBuilder = new HttpMethodConverter(definition, dummyService.getQName(), dummyPort.getName());

    }

    public void testGetTag() throws Exception {
        String uri = ((HTTPAddress) deliciousPort.getExtensibilityElements().get(0)).getLocationURI();
        String expectedUri = uri + "/tag/java";
        Element msgEl;
        {
            Document odeMsg = DOMUtils.newDocument();
            msgEl = odeMsg.createElementNS(null, "message");
            Element partEl = odeMsg.createElementNS(null, "TagPart");
            partEl.setTextContent("java");
            odeMsg.appendChild(msgEl);
            msgEl.appendChild(partEl);
        }

        MockMessageExchange odeMex = new MockMessageExchange();
        odeMex.op = deliciousBinding.getBindingOperation("getTag", null, null).getOperation();
        odeMex.req = new MockMessage(msgEl);
        odeMex.epr = new MockEPR(uri);
        HttpMethod httpMethod = deliciousBuilder.createHttpRequest(odeMex, new DefaultHttpParams());


        assertTrue("GET".equalsIgnoreCase(httpMethod.getName()));
        assertTrue(expectedUri.equalsIgnoreCase(httpMethod.getURI().toString()));
    }

    public void testGetTagWithNoPart() throws Exception {
        String uri = ((HTTPAddress) deliciousPort.getExtensibilityElements().get(0)).getLocationURI();
        Element msgEl;
        {
            Document odeMsg = DOMUtils.newDocument();
            msgEl = odeMsg.createElementNS(null, "message");
            odeMsg.appendChild(msgEl);
        }

        MockMessageExchange odeMex = new MockMessageExchange();
        odeMex.op = deliciousBinding.getBindingOperation("getTag", null, null).getOperation();
        odeMex.req = new MockMessage(msgEl);
        odeMex.epr = new MockEPR(uri);
        try {
            HttpMethod httpMethod = deliciousBuilder.createHttpRequest(odeMex, new DefaultHttpParams());
            fail("IllegalArgumentException expected because message element is empty.");
        } catch (IllegalArgumentException e) {
            // expected behavior
        }
    }

    public void testHello() throws Exception {
        String uri = ((HTTPAddress) dummyPort.getExtensibilityElements().get(0)).getLocationURI();
        String expectedUri = uri + "/" + "DummyService/hello";
        Element msgEl, helloEl;
        {
            Document odeMsg = DOMUtils.newDocument();
            msgEl = odeMsg.createElementNS(null, "message");
            Element partEl = odeMsg.createElementNS(null, "parameters");
            odeMsg.appendChild(msgEl);
            msgEl.appendChild(partEl);
            helloEl = odeMsg.createElementNS(null, "hello");
            helloEl.setTextContent("This is a test. How is it going so far?");
            partEl.appendChild(helloEl);
        }

        MockMessageExchange odeMex = new MockMessageExchange();
        odeMex.op = dummyBinding.getBindingOperation("hello", null, null).getOperation();
        odeMex.req = new MockMessage(msgEl);
        odeMex.epr = new MockEPR(uri);
        HttpMethod httpMethod = dummyBuilder.createHttpRequest(odeMex, new DefaultHttpParams());
        assertTrue("POST".equalsIgnoreCase(httpMethod.getName()));
        assertEquals("Generated URI does not match", expectedUri, httpMethod.getURI().toString());

        String b =  ((StringRequestEntity) ((PostMethod) httpMethod).getRequestEntity()).getContent();
        assertEquals("Invalid body in generated http query", DOMUtils.domToString(helloEl), b);
    }


    class MockEPR implements EndpointReference, MutableEndpoint {
        String url;

        MockEPR(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        // other useless methods
        public Document toXML() {
            return null;
        }

        public boolean accept(Node node) {
            return false;
        }

        public void fromMap(Map eprMap) {

        }

        public void set(Node node) {

        }

        public Map toMap() {
            return null;
        }
    }

    class MockMessage implements Message {
        Element elt;

        MockMessage(Element elt) {
            this.elt = elt;
        }

        public Element getMessage() {
            return elt;
        }

        // other useless methods
        public Element getHeaderPart(String partName) {
            return null;
        }

        public Map<String, Node> getHeaderParts() {
            return Collections.EMPTY_MAP;
        }

        public Element getPart(String partName) {
            return null;
        }

        public List<String> getParts() {
            return null;
        }

        public QName getType() {
            return null;
        }

        public void setHeaderPart(String name, Element content) {

        }

        public void setHeaderPart(String name, String content) {

        }

        public void setMessage(Element msg) {

        }

        public void setPart(String partName, Element content) {

        }
    }

    class MockMessageExchange implements PartnerRoleMessageExchange {
        Operation op;
        Message req;
        EndpointReference epr;

        public Operation getOperation() {
            return op;
        }

        public Message getRequest() {
            return req;
        }

        public EndpointReference getEndpointReference() throws BpelEngineException {
            return epr;
        }

        // other useless methods
        public QName getCaller() {
            return null;
        }

        public PartnerRoleChannel getChannel() {
            return null;
        }

        public EndpointReference getMyRoleEndpointReference() {
            return null;
        }

        public void reply(Message response) throws BpelEngineException {

        }

        public void replyAsync() {

        }

        public void replyOneWayOk() {

        }

        public void replyWithFailure(FailureType type, String description, Element details) throws BpelEngineException {

        }

        public void replyWithFault(QName faultType, Message outputFaultMessage) throws BpelEngineException {

        }

        public Message createMessage(QName msgType) {
            return null;
        }

        public QName getFault() {
            return null;
        }

        public String getFaultExplanation() {
            return null;
        }

        public Message getFaultResponse() {
            return null;
        }

        public String getMessageExchangeId() throws BpelEngineException {
            return null;
        }

        public MessageExchangePattern getMessageExchangePattern() {
            return null;
        }

        public String getOperationName() throws BpelEngineException {
            return null;
        }

        public PortType getPortType() {
            return null;
        }

        public String getProperty(String key) {
            return null;
        }

        public Set<String> getPropertyNames() {
            return null;
        }

        public Message getResponse() {
            return null;
        }

        public Status getStatus() {
            return null;
        }

        public boolean isTransactionPropagated() throws BpelEngineException {
            return false;
        }

        public void release() {

        }

        public void setProperty(String key, String value) {

        }

        public int getSubscriberCount() {
            return 0;
        }

        public void setSubscriberCount(int subscriberCount) {
        }
    }
}
