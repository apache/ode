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

package org.apache.ode.axis2.hydration;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;

import org.apache.ode.axis2.Axis2TestBase;
import org.apache.ode.axis2.DummyService;
import org.apache.ode.axis2.service.ServiceClientUtil;
import org.apache.ode.tools.sendsoap.cline.HttpSoapSender;
import org.apache.ode.utils.Namespaces;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.net.URL;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.namespace.QName;


/**
 * Test the limit on the number of process instances. 
 *
 * @author $author$
 * @version $Revision$
  */
public class InstanceCountTest extends Axis2TestBase {
    private OMFactory _factory;
    private DateFormat xsdDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    private ServiceClientUtil _client;
    private String _deployedName;

    /**
     * test case set up
     *
     * @throws Exception Exception 
     */
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();

        // Create a factory
        _factory = OMAbstractFactory.getOMFactory();
        _client = new ServiceClientUtil();

        // Just making sure the instance starts
        Thread.sleep(1000);
    }

    /**
     * test case tear down
     *
     * @throws Exception Exception 
     */
    @AfterMethod
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * Tests rendezvous
     * 
     * @throws Exception
     */
    String firstResponse, secondResponse;
    boolean secondStarted;
    
    @Test(dataProvider="configs")
    public void testCorrelationJoin() throws Exception {
        final String bundleName = "TestCorrelationJoin";
        
        firstResponse = secondResponse = null;
        secondStarted = true;
        
        server.getODEServer().getBpelServer().setInstanceThrottledMaximumCount(1);
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(3000);
                    server.sendRequestFile("http://localhost:8888/processes/correlationMultiTest",
                            bundleName, "testRequest2.soap");
                } catch( Exception e ) {
                    fail(e.getMessage());
                }
            }
        }.start();        
        
        Thread processOne = new Thread() {
        	public void run() {
                try {
                    firstResponse = server.sendRequestFile("http://localhost:8888/processes/correlationMultiTest",
                            bundleName, "testRequest.soap");
                    System.out.println("=>\n" + firstResponse);
                } catch (Exception e) {
                    fail(e.getMessage());
                }
        	}
        };
        processOne.start();
        
        Thread processTwo = new Thread() {
            public void run() {
                try {
                    Thread.sleep(3000);
                    secondResponse = server.sendRequestFile("http://localhost:8888/processes/correlationMultiTest",
                            bundleName, "testRequest.soap");
                } catch( Exception e ) {
                    fail(e.getMessage());
                }
            }
        };
        processTwo.start();
        processTwo.join();

        try {
	        assertTrue(secondResponse.contains("tooManyInstances"));
        } catch (Exception e) {
            server.undeployProcess(bundleName);
			fail("The second instance was allowed to start");
        }
		
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(6000);
                    server.sendRequestFile("http://localhost:8888/processes/correlationMultiTest",
                            bundleName, "testRequest3.soap");
                } catch( Exception e ) {
                    fail(e.getMessage());
                }
            }
        }.start();

        try {
	        processOne.join();        
	        assertTrue(firstResponse.contains(">1;2;3;<"));
        } finally {
	        server.undeployProcess(bundleName);
        }
    }

    public String getODEConfigDir() {
        return getClass().getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.jpa-derby"; 
    }    
}
