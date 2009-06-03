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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.Axis2TestBase;
import org.apache.ode.axis2.JettyWrapper;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;

/**
 * <p/>
 * This unit test passes an integer to a BPEL. Then the BPEL invokes the 6 operations of Arithmetics.wsdl.
 * These operations are set up to use the various Http binding configurations.
 * <p/>
 * From a "business" standpoint:<br/>
 * Let N be the input number, stored in the testRequest1.soap file<br/>
 * This test will compute the Sum of the first (N + 5) positive integers.
 * <p/>
 * If N=10, the expected result is 15*(15+1)/2 = 120
 *
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HttpBindingTest extends Axis2TestBase {

    private static final Log log = LogFactory.getLog(HttpBindingTest.class);

    protected JettyWrapper jettyWrapper;


  @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
      jettyWrapper = new JettyWrapper(getTestPort(1));
      jettyWrapper.start();
  }

    @AfterMethod
    protected void tearDown() throws Exception {
        jettyWrapper.stop();
        super.tearDown();
    }

    @Test(dataProvider="configs")
    public void testHttpBinding() throws Exception {
        String bundleName = "TestHttpBinding";
        // deploy the required service
        if (!server.isDeployed(bundleName)) server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8888/processes/helloWorld",
                    bundleName, "testRequest.soap");
            if (log.isDebugEnabled()) log.debug(response);
            int valueInSoapRequest = 100;
            int n = 5 + valueInSoapRequest;
            String expectedResult = String.valueOf(n * (n + 1) / 2);
            assertTrue("Expected Result: " + expectedResult + ". Answer was " + response, response.indexOf(expectedResult) >= 0);
        } finally {
            server.undeployProcess(bundleName);
        }
    }

    @Test(dataProvider="configs")
    public void testHttpBindingExt_GET() throws Exception {
        String bundleName = "TestHttpBindingExt_GET";
        executeBundle(bundleName);

    }

    @Test(dataProvider="configs")
    public void testHttpBindingExt_DELETE() throws Exception {
        String bundleName = "TestHttpBindingExt_DELETE";
        executeBundle(bundleName);
    }

    @Test(dataProvider="configs")
    public void testHttpBindingExt_POST() throws Exception {
        String bundleName = "TestHttpBindingExt_POST";
        executeBundle(bundleName);
    }

    @Test(dataProvider="configs")
    public void testHttpBindingExt_PUT() throws Exception {
        String bundleName = "TestHttpBindingExt_PUT";
        executeBundle(bundleName);
    }

    private void executeBundle(String bundleName) throws InterruptedException {
        // wait for jetty to be ready
        // clean up everything first
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);

        // then deploy the required service
        server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8888/processes/helloWorld", bundleName, "testRequest.soap");
            System.out.println("Test Response Received: "+response);
            if (log.isDebugEnabled()) log.debug("Test Response Received: "+response);
            assertTrue("Test failed. Response is:"+response, response.indexOf("What a success!") >= 0);
        } finally {
            server.undeployProcess(bundleName);
        }
    }
}
