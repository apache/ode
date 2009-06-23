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

package org.apache.ode.axis2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.Axis2TestBase;
import org.apache.ode.axis2.JettyWrapper;
import org.apache.ode.axis2.httpbinding.HttpBindingTest;
import org.apache.ode.utils.DOMUtils;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.StringReader;

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
public class SoapHeader2Test extends Axis2TestBase {

    private static final Log log = LogFactory.getLog(SoapHeader2Test.class);

    protected JettyWrapper jettyWrapper;


    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
        ContextHandler contextHandler = new ContextHandler();
        contextHandler.setContextPath("/TestSoapHeader2");
        contextHandler.setHandler(new AbstractHandler() {
            public void handle(String s, HttpServletRequest request, HttpServletResponse response, int i) throws IOException, ServletException {
                boolean header1found = false, header2found = false;
                String line;
                while ((line = request.getReader().readLine()) != null && (!header1found || !header2found)) {
                    header1found = header1found || line.matches(".*Hello from TestSoapHeader2</header1-field1>.*");
                    header2found = header2found || line.matches(".*Hello from TestSoapHeader2</header2-field1>.*");
                }
                response.getOutputStream().print("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:unit=\"http://ode/bpel/unit-test.wsdl\">\n" +
                        "   <soapenv:Header/>\n" +
                        "   <soapenv:Body>\n" +
                        "      <unit:body>\n");
                if (header1found && header2found) {
                    response.getOutputStream().print("         <unit:message>TestSoapHeader2 passed</unit:message>\n");
                } else {
                    response.getOutputStream().print("         <unit:message>Some soap headers are missing!</unit:message>\n");
                }
                response.getOutputStream().print("      </unit:body>\n" +
                        "   </soapenv:Body>\n" +
                        "</soapenv:Envelope>");
                response.getOutputStream().close();
                response.setStatus(200);
                ((Request) request).setHandled(true);
            }
        });

        jettyWrapper = new JettyWrapper(contextHandler);
        jettyWrapper.start();
    }

    @AfterMethod
    protected void tearDown() throws Exception {
        jettyWrapper.stop();
        super.tearDown();
    }

    @Test
    public void testSoapHeaders() throws Exception {
        String bundleName = "TestSoapHeader2";
        // deploy the required service
        if (!server.isDeployed(bundleName)) server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8888/processes/hello/hello/process/client",
                    bundleName, "testRequest.soap");
            if (log.isDebugEnabled()) log.debug(response);
            assertTrue("Soap headers missing!", response.contains("TestSoapHeader2 passed"));
        } finally {
            server.undeployProcess(bundleName);
        }
    }

}
