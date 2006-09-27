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

import junit.framework.TestCase;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.test.scheduler.TestScheduler;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Properties;
import java.util.regex.Pattern;

public class BPELTest extends TestCase {
	
	private BpelServerImpl server;
	private MessageExchangeContextImpl mexContext; 

	@Override
	protected void setUp() throws Exception {
		server = new BpelServerImpl();
		mexContext = new MessageExchangeContextImpl();
		server.setDaoConnectionFactory(new BpelDAOConnectionFactoryImpl());
		server.setScheduler(new TestScheduler());
		server.setBindingContext(new BindingContextImpl());
		server.setMessageExchangeContext(mexContext);
		server.setDeploymentManager(new DeploymentManagerImpl());
		server.init();
		server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		server.stop();
	}
	
	private void go(String deployDir) throws Exception {
		
		/**
		 * The deploy directory must contain at least one
		 * "test.properties" file. 
		 * 
		 * The test.properties file identifies the service, 
		 * operation and messages to be sent to the BPEL engine.
		 * 
		 * The deploy directory may contain more than
		 * one file in the form of "testN.properties" where N
		 * represents a monotonic integer beginning with 1.
		 *   
		 */
		
		int propsFileCnt = 0;
		File testPropsFile = new File(deployDir+"/test.properties");
		
		if ( !testPropsFile.exists()) {
			propsFileCnt++;
			testPropsFile = new File(deployDir+"/test"+propsFileCnt+".properties");
			if ( !testPropsFile.exists()) {
				System.err.println("can't find "+ testPropsFile.toString());
			}
		}
		
		server.getDeploymentService().deploy(new File(deployDir));
		
		while ( testPropsFile.exists() ) {
		
			Properties testProps = new Properties();
			testProps.load(testPropsFile.toURL().openStream());
			
			QName serviceId = new QName(testProps.getProperty("namespace"),
					testProps.getProperty("service"));
			String operation = testProps.getProperty("operation");
	
			MyRoleMessageExchange mex = server.getEngine().createMessageExchange("",serviceId,operation);
	
	
			/**
			 * Each property file must contain at least one 
			 * request/response property tuple.
			 * 
			 * The request/response tuple should be in the form
			 * 
			 * requestN=<message>some XML input message</message>
			 * responseN=.*some response message.*
			 * 
			 * Where N is a monotonic integer beginning with 1.
			 * 
			 * If no response is expected use:
			 * responseN=null
			 * 
			 */
			
			for (int i=1; testProps.getProperty("request"+i) != null; i++) {
	
				String in = testProps.getProperty("request"+i);
				String responsePattern = testProps.getProperty("response"+i);
	
				mexContext.clearCurrentResponse();
				
				Message request = mex.createMessage(null);
	
				Element elem = DOMUtils.stringToDOM(in);
				request.setMessage(elem);
	
				mex.invoke(request);
				
				switch (mex.getStatus()) {
				case RESPONSE:
					Message response = mex.getResponse();
					String resp = DOMUtils.domToString(response.getMessage());
					System.out.println(resp);
					assertTrue(Pattern.compile(responsePattern,Pattern.DOTALL).matcher(resp).matches());
					// TODO: test for response fault
					break;
				case ASYNC:
					// TODO: handle Async
					if ( !responsePattern.equals("null")) 
						assertTrue(false);
					break;
				case FAULT:
					// TODO: handle Fault
					assertTrue(false);
					break;
				default:
					assertTrue(false);
					break;
				} 

			}
			propsFileCnt++;
			testPropsFile = new File(deployDir+"/test"+propsFileCnt+".properties");
		}
	}

	public void testHelloWorld2() throws Exception {
		go("target/test-classes/bpel/2.0/HelloWorld2");
	}
	public void testFlowActivity1() throws Exception {
		// Test Flow with XPath20
		go("target/test-classes/bpel/2.0/TestFlowActivity1");
	}
	public void testFlowActivity2() throws Exception {
		// Test Flow with XPath10
		go("target/test-classes/bpel/2.0/TestFlowActivity2");
	}
	public void testFaultHandlers() throws Exception {
		go("target/test-classes/bpel/2.0/TestFaultHandlers");
	}
    public void testAssignActivity1() throws Exception {
        go("target/test-classes/bpel/2.0/TestAssignActivity1");
    }
    public void testAssignActivity2() throws Exception {
        go("target/test-classes/bpel/2.0/TestAssignActivity2");
    }
    public void testInstPick() throws Exception {
        go("target/test-classes/bpel/2.0/TestInstantiatingPick");
    }
	public void testCorrelation() throws Exception {
		go("target/test-classes/bpel/2.0/testCorrelation");
	}
    
    /** These tests compile however they fail at runtime */
  
//	public void testCompensationHandlers() throws Exception {
//		go("target/test-classes/bpel/2.0/TestCompensationHandlers");
//	}
//	public void testTimer() throws Exception {
//		go("target/test-classes/bpel/2.0/TestTimer");
//	} 


}
