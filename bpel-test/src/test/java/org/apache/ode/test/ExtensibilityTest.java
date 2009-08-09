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

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.v2.CompilerContext;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.rtrep.common.extension.AbstractExtensionBundle;
import org.apache.ode.bpel.extension.ExtensionOperation;
import org.apache.ode.bpel.rtrep.common.extension.ExtensionContext;
import org.apache.ode.bpel.rtrep.common.extension.AbstractAsyncExtensionOperation;
import org.apache.ode.bpel.extension.ExtensibleElement;
import org.apache.ode.utils.DOMUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Test ODE's extensibility
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class ExtensibilityTest extends BPELTestAbstract {
	
	private TestExtensionBundle teb;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		teb = new TestExtensionBundle();
		//BasicConfigurator.configure();
	}
	
	@Test public void testExtensionActivityWOMustUnderstandWOBundle() throws Throwable {
		go("/bpel/2.0/TestExtensionActivity");
	}

	@Test public void testExtensionActivityWithMustUnderstandWOBundle() throws Throwable {
        Deployment deployment = new Deployment(makeDeployDir("/bpel/2.0/TestExtensionActivityMustUnderstand"));
        deployment.expectedException = BpelEngineException.class;
        doDeployment(deployment);
	}

	@Test public void testExtensionActivityWOMustUnderstandWithBundle() throws Throwable {
		_server.registerExtensionBundle(teb);
		Assert.assertFalse(teb.wasExecuted());
		go("/bpel/2.0/TestExtensionActivity");
		Assert.assertTrue(teb.wasExecuted());
		_server.unregisterExtensionBundle(teb.getNamespaceURI());
		teb.recycle();
	}
	
	@Test public void testExtensionActivityWithMustUnderstandWithBundle() throws Throwable {
		_server.registerExtensionBundle(teb);
		Assert.assertFalse(teb.wasExecuted());
		go("/bpel/2.0/TestExtensionActivityMustUnderstand");
		Assert.assertTrue("ExtensionActivity has not been executed", teb.wasExecuted());
		_server.unregisterExtensionBundle(teb.getNamespaceURI());
		teb.recycle();
	}

	@Test public void testExtensionAssignOperation() throws Throwable {
		_server.registerExtensionBundle(teb);
		go("/bpel/2.0/TestExtensionAssignOperation");
		_server.unregisterExtensionBundle(teb.getNamespaceURI());
		teb.recycle();
	}

	@Test public void testExtensionActivityCompilerError() throws Throwable {
		_server.registerExtensionBundle(teb);
		TestExtensionBundle.cmpString = "error";
		go("/bpel/2.0/TestExtensionActivity");
        Deployment deployment = new Deployment(makeDeployDir("/bpel/2.0/TestExtensionActivityMustUnderstand"));
        deployment.expectedException = CompilationException.class;
        doDeployment(deployment);
		_server.unregisterExtensionBundle(teb.getNamespaceURI());
		teb.recycle();
	}
	
	private static class TestExtensionBundle extends AbstractExtensionBundle {
		private static boolean wasExecuted = false;
		private static String cmpString = "test";
		
		public String getNamespaceURI() {
			return "urn:ode:test-extension-bundle";
		}

		public void registerExtensionActivities() {
			registerExtensionOperation("doIt", TestExtensionActivity.class);
			registerExtensionOperation("doIt2", TestExtensionValidatorActivity.class);
			registerExtensionOperation("doAssign", TestExtensionAssignOperation.class);
		}
		
		public boolean wasExecuted() {
			return wasExecuted;
		}
		
		public void recycle() {
			wasExecuted = false;
			cmpString = "test";
		}
	}

	public static class TestExtensionActivity implements ExtensionOperation {
		private static final long serialVersionUID = 1L;

		public void run(Object context, String cid,
				Element element) throws FaultException {
			TestExtensionBundle.wasExecuted = true;
			((ExtensionContext)context).complete(cid);
		}
	}
	
	public static class TestExtensionAssignOperation implements ExtensionOperation {
		private static final long serialVersionUID = 1L;

		public void run(Object contexto, String cid, Element element)
				throws FaultException {
            ExtensionContext context = (ExtensionContext) contexto;
			//Node val = context.readVariable("myVar");
			StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			sb.append("<message><TestPart>Small</TestPart></message>");
			try {
				context.writeVariable("tmpVar", DOMUtils.stringToDOM(sb.toString()));
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail();
			} finally {
				context.complete(cid);
			}
		}
	}

	public static class TestExtensionValidatorActivity extends AbstractAsyncExtensionOperation {
		private static final long serialVersionUID = 1L;

		public void run(Object context, String cid,
				Element element) throws FaultException {
			TestExtensionBundle.wasExecuted = true;
			((ExtensionContext)context).complete(cid);
		}

		public void validate(CompilerContext context, ExtensibleElement element)
				throws CompilationException {
			if (element.getNestedElement().getTextContent().trim().equals(TestExtensionBundle.cmpString)) {
				throw new CompilationException(new CompilationMessage());
			}
		}
	}

}
