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
package org.apache.ode.bpel.context;

import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.ode.bpel.rapi.ContextData;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.NSContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ContextDataTest extends TestCase {

    public void testToXML() throws Exception {
        ContextData cdata = new ContextDataImpl();
        cdata.put("tracing", "id", "4711");
        cdata.put("foo", "bar", "baz");
        cdata.put("foo", "mykey", "myval");
        
        Node xml = cdata.toXML();
        
        NSContext nsContext = new NSContext();
        nsContext.register("ctx", "http://www.apache.org/ode/schemas/context/2009");
        
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nsContext);
        Assert.assertEquals("tracing", xpath.evaluate("/ctx:contexts/ctx:context[1]/@name", xml));
        Assert.assertEquals("foo", xpath.evaluate("/ctx:contexts/ctx:context[2]/@name", xml));
        
        Assert.assertEquals("id", xpath.evaluate("/ctx:contexts/ctx:context[1]/ctx:value/@key", xml));
        Assert.assertEquals("4711", xpath.evaluate("/ctx:contexts/ctx:context[1]/ctx:value", xml));
        Assert.assertEquals("baz", xpath.evaluate("/ctx:contexts/ctx:context[@name='foo']/ctx:value[@key='bar']", xml));
        Assert.assertEquals("myval", xpath.evaluate("/ctx:contexts/ctx:context[@name='foo']/ctx:value[@key='mykey']", xml));
    }
    
    public void testToXMLFiltered() throws Exception {
        ContextData cdata = new ContextDataImpl();
        cdata.put("tracing", "id", "4711");
        cdata.put("foo", "bar", "baz");
        cdata.put("foo", "mykey", "myval");
        
        Set<String> filter = new HashSet<String>();
        filter.add("tracing");
        
        Node xml = cdata.toXML(filter);

        NSContext nsContext = new NSContext();
        nsContext.register("ctx", "http://www.apache.org/ode/schemas/context/2009");
        
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nsContext);
        Assert.assertEquals("tracing", xpath.evaluate("/ctx:contexts/ctx:context[1]/@name", xml));
        Assert.assertEquals("", xpath.evaluate("/ctx:contexts/ctx:context[2]/@name", xml));
        
        Assert.assertEquals("id", xpath.evaluate("/ctx:contexts/ctx:context[1]/ctx:value/@key", xml));
        Assert.assertEquals("4711", xpath.evaluate("/ctx:contexts/ctx:context[1]/ctx:value", xml));
        Assert.assertEquals("", xpath.evaluate("/ctx:contexts/ctx:context[@name='foo']/ctx:value[@key='bar']", xml));
        Assert.assertEquals("", xpath.evaluate("/ctx:contexts/ctx:context[@name='foo']/ctx:value[@key='mykey']", xml));
    }

    public void testFromXML() throws Exception {
        String doc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ns:contexts xmlns:ns=\"http://www.apache.org/ode/schemas/context/2009\"><ns:context name=\"tracing\"><ns:value key=\"id\">4711</ns:value></ns:context><ns:context name=\"foo\"><ns:value key=\"bar\">baz</ns:value><ns:value key=\"mykey\">myval</ns:value></ns:context></ns:contexts>";
        Element el = DOMUtils.stringToDOM(doc);
        
        ContextData cdata = ContextDataImpl.fromXML(el);
        
        Assert.assertNotNull(cdata);
        Assert.assertEquals("4711", cdata.get("tracing", "id"));
        Assert.assertEquals("baz", cdata.get("foo", "bar"));
        Assert.assertEquals("myval", cdata.get("foo", "mykey"));
        Assert.assertNull(cdata.get("foo", "foo"));
    }
}
