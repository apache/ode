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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.ode.bpel.context.AbstractContextInterceptor;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.rapi.ContextData;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.NSContext;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ContextTest extends BPELTestAbstract {

    @Test
    public void testContextAssign() throws Throwable {
        TestContextInterceptor tci = new TestContextInterceptor();
        _server.registerContextInterceptor(tci);
        go("/bpel/2.0/TestContextAssign");
        _server.unregisterContextInterceptor(tci);

        Assert.assertNull(tci.contextPartnerInvoked);
        Assert.assertNull(tci.contextPartnerReplied);
        Assert.assertNotNull(tci.contextProcessInvoked);
        Assert.assertNotNull(tci.contextProcessReplied);

        Node xmlpr = tci.contextProcessReplied.toXML();

        NSContext nsContext = new NSContext();
        nsContext.register("ctx",
                "http://www.apache.org/ode/schemas/context/2009");

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nsContext);
        Assert.assertEquals("baz", xpath.evaluate(
                "/ctx:contexts/ctx:context[@name='foo']/ctx:value[@key='bar']",
                xmlpr));
        Assert.assertEquals("", xpath.evaluate(
                "/ctx:contexts/ctx:context[@name='bar']/ctx:value[@key='foo']",
                xmlpr));

    }

    public class TestContextInterceptor extends AbstractContextInterceptor {

        ContextData contextPartnerInvoked = null;
        ContextData contextPartnerReplied = null;
        ContextData contextProcessInvoked = null;
        ContextData contextProcessReplied = null;

        public void configure(Element configuration) {
        }

        public void onPartnerInvoke(ContextData ctx, Message msg) {
            contextPartnerInvoked = ctx;
        }

        public void onPartnerReply(ContextData ctx, Message msg) {
            contextPartnerReplied = ctx;
        }

        public void onProcessInvoke(ContextData ctx, Message msg) {
            contextProcessInvoked = ctx;
        }

        public void onProcessReply(ContextData ctx, Message msg) {
            contextProcessReplied = ctx;
        }

    }
}
