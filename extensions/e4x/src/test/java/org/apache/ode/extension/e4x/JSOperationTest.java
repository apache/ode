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

import java.util.HashMap;
import java.util.Map;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.eapi.ExtensionContext;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OProcess.OProperty;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.SerializableElement;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class JSOperationTest {

	public static class MockExtensionContext implements ExtensionContext {
		private Map<String, Node> variables = new HashMap<String, Node>();
		
		public Map<String, Node> getVariables() {
			return variables;
		}
		
		public Long getProcessId() {
			// TODO Auto-generated method stub
			return null;
		}

		public Map<String, Variable> getVisibleVariables()
				throws FaultException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isLinkActive(OLink olink) throws FaultException {
			// TODO Auto-generated method stub
			return false;
		}

		public String readMessageProperty(Variable variable, OProperty property)
				throws FaultException {
			// TODO Auto-generated method stub
			return null;
		}

		public Node readVariable(Variable variable) throws FaultException {
			// TODO Auto-generated method stub
			return null;
		}

		public Node readVariable(String variableName) throws FaultException {
			System.out.println("Reading " + variableName);
			return variables.get(variableName);
		}

		public void writeVariable(Variable variable, Node value)
				throws FaultException {
			// TODO Auto-generated method stub
			
		}

		public void writeVariable(String variableName, Node value)
				throws FaultException {
			variables.put(variableName, value);
			System.out.println("Storing in " + variableName + ": " + DOMUtils.domToString(value));
		}

		public String getActivityName() {
			return "mockActivity";
		}

		public OActivity getOActivity() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	@Test public void test() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("var request = context.readVariable('request');\n");
		//s.append("var str = '' + request.TestPart.toString();\n");
		//s.append("request.TestPart = str + ' World';\n");//
		s.append("request.TestPart += ' World';\n");
		s.append("context.writeVariable('request', request);\n");

		MockExtensionContext c = new MockExtensionContext();
		c.getVariables().put("request", DOMUtils.stringToDOM("<message><TestPart>Hello</TestPart></message>"));
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, new SerializableElement(e));
	}
}
