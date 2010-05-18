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
import static org.testng.AssertJUnit.assertTrue;

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
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

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
public class ProcessCountTest extends Axis2TestBase {
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
    String nsAttr;
    
    @Test(dataProvider="configs")
    public void testCorrelationJoin() throws Exception {
        final String bundleOne = "TestCorrelationJoin", bundleTwo = "TestAttributeNamespaces";
        
        firstResponse = secondResponse = null;
        secondStarted = true;
        
        server.getODEServer().getBpelServer().setProcessThrottledMaximumCount(0);

        // deploy the first service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleOne)) server.undeployProcess(bundleOne);
        server.deployProcess(bundleOne);

        Thread processOne = new Thread() {
        	public void run() {
                try {
                    firstResponse = server.sendRequestFile("http://localhost:8888/processes/correlationMultiTest",
                            bundleOne, "testRequest.soap");
                    System.out.println("=>\n" + firstResponse);
                } catch (Exception e) {
                    fail(e.getMessage());
                }
        	}
        };
        processOne.start();
        processOne.join();
        
        try {
	        processOne.join();        
	        assertTrue(firstResponse.contains("tooManyProcesses"), firstResponse);
        } finally {
	        server.undeployProcess(bundleOne);
        }
        
    }

    public String getODEConfigDir() {
        return getClass().getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.jpa-derby"; 
    }    
}
