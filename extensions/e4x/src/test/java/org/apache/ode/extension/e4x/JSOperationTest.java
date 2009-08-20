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
package org.apache.ode.extension.e4x;


import org.apache.log4j.BasicConfigurator;
import org.apache.ode.test.MockExtensionContext;
import org.apache.ode.utils.DOMUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class JSOperationTest {
	
	@Before public void setup() {
		BasicConfigurator.configure();
	}

	@Test public void testHelloWorld() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("var request = dom2js(_context.readVariable('request'));\n");
		s.append("request.TestPart += ' World';\n");
		s.append("_context.writeVariable('request', js2dom(request));\n");

		MockExtensionContext c = new MockExtensionContext();
		c.getVariables().put("request", DOMUtils.stringToDOM("<message><TestPart>Hello</TestPart></message>"));
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, null, e);
		String res = DOMUtils.domToString(c.getVariables().get("request"));
		Assert.assertTrue(c.completed);
		Assert.assertFalse(c.faulted);
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message><TestPart>Hello World</TestPart></message>", res);
	}
	
	@Test public void testHelloWorldDirect() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("request.TestPart += ' World';\n");

		MockExtensionContext c = new MockExtensionContext();
		c.getVariables().put("request", DOMUtils.stringToDOM("<message><TestPart>Hello</TestPart></message>"));
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, null, e);
		String res = DOMUtils.domToString(c.getVariables().get("request"));
		Assert.assertTrue(c.completed);
		Assert.assertFalse(c.faulted);
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message><TestPart>Hello World</TestPart></message>", res);
	}

	@Test public void testArrayCopy() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("var item = dom2js(_context.readVariable('item'));\n");
		s.append("var items = dom2js(_context.readVariable('items'));\n");
		s.append("items.TestPart.items.item += item.TestPart.item;\n");
		s.append("items.TestPart.items.item.(@hyped=='true').price *= 2;");
		s.append("_context.writeVariable('items', js2dom(items));\n");

		MockExtensionContext c = new MockExtensionContext();
		c.getVariables().put("item", DOMUtils.stringToDOM("<message><TestPart><item hyped=\"true\"><name>BPEL consulting</name><price>3000</price></item></TestPart></message>"));
		c.getVariables().put("items", DOMUtils.stringToDOM("<message><TestPart><items><item><name>WSDL consulting</name><price>2500</price></item></items></TestPart></message>"));
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, null, e);
		String res = DOMUtils.domToString(c.getVariables().get("items"));
		Assert.assertTrue(c.completed);
		Assert.assertFalse(c.faulted);
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message><TestPart><items><item><name>WSDL consulting</name><price>2500</price></item><item hyped=\"true\"><name>BPEL consulting</name><price>6000</price></item></items></TestPart></message>", res);
	}

	@Test public void testArrayCopyDirect() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("items.TestPart.items.item += item.TestPart.item;\n");
		s.append("items.TestPart.items.item.(@hyped=='true').price *= 2;");

		MockExtensionContext c = new MockExtensionContext();
		c.getVariables().put("item", DOMUtils.stringToDOM("<message><TestPart><item hyped=\"true\"><name>BPEL consulting</name><price>3000</price></item></TestPart></message>"));
		c.getVariables().put("items", DOMUtils.stringToDOM("<message><TestPart><items><item><name>WSDL consulting</name><price>2500</price></item></items></TestPart></message>"));
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, null, e);
		String res = DOMUtils.domToString(c.getVariables().get("items"));
		Assert.assertTrue(c.completed);
		Assert.assertFalse(c.faulted);
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message><TestPart><items><item><name>WSDL consulting</name><price>2500</price></item><item hyped=\"true\"><name>BPEL consulting</name><price>6000</price></item></items></TestPart></message>", res);
	}

	@Test public void testVariableNotFoundDirect() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("items1 = item;\n");

		MockExtensionContext c = new MockExtensionContext();
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, null, e);
		Assert.assertTrue(c.completed);
		Assert.assertTrue(c.faulted);
	}
	
	@Test public void testPrint() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("print('Hello World!');\n");

		MockExtensionContext c = new MockExtensionContext();
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, null, e);
		Assert.assertTrue(c.completed);
		Assert.assertFalse(c.faulted);
		Assert.assertEquals(1, c.msgs.size());
		Assert.assertEquals("Hello World!", c.msgs.get(0));
	}

	@Test public void testLoad() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("load('../resources/test.js');\n");
		s.append("print(myvar);\n");

		MockExtensionContext c = new MockExtensionContext();
		c.duDir = this.getClass().getResource("/").toURI();
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, null, e);
		Assert.assertTrue(c.completed);
		Assert.assertFalse(c.faulted);
		Assert.assertEquals(1, c.msgs.size());
		Assert.assertEquals("Hello Lib!", c.msgs.get(0));
	}

	@Test public void testPid() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("myvar = pid();\n");
		s.append("print(myvar);\n");

		MockExtensionContext c = new MockExtensionContext();
		c.duDir = this.getClass().getResource("/").toURI();
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, null, e);
		Assert.assertTrue(c.completed);
		Assert.assertFalse(c.faulted);
		Assert.assertEquals(1, c.msgs.size());
		Assert.assertEquals("4711", c.msgs.get(0));
	}

	@Test public void testActivityName() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("myvar = activityName();\n");
		s.append("print(myvar);\n");

		MockExtensionContext c = new MockExtensionContext();
		c.duDir = this.getClass().getResource("/").toURI();
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, null, e);
		Assert.assertTrue(c.completed);
		Assert.assertFalse(c.faulted);
		Assert.assertEquals(1, c.msgs.size());
		Assert.assertEquals("mockActivity", c.msgs.get(0));
	}

	@Test public void testThrowFault() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("throwFault('urn:test', 'myfault', 'Ohje');\n");
		s.append("print('unreachable');\n");

		MockExtensionContext c = new MockExtensionContext();
		c.duDir = this.getClass().getResource("/").toURI();
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, null, e);
		Assert.assertTrue(c.completed);
		Assert.assertTrue(c.faulted);
		Assert.assertEquals(0, c.msgs.size());
		Assert.assertEquals("myfault", c.fault.getQName().getLocalPart());
		Assert.assertEquals("urn:test", c.fault.getQName().getNamespaceURI());
		Assert.assertEquals("{urn:test}myfault: Ohje", c.fault.getMessage());
	}

}
