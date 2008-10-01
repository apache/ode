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

import junit.framework.TestCase;
import org.apache.ode.axis2.JettyWrapper;

import java.util.concurrent.CountDownLatch;

/**
 *
 *
 */
public class EndpointPropertiesTest extends Axis2TestBase {
    protected JettyWrapper jettyWrapper;


    protected void setUp() throws Exception {
        super.setUp();
        final CountDownLatch latch = new CountDownLatch(1);
        jettyWrapper = new JettyWrapper(7070);
        new Thread("HttpBindingJetty") {
            public void run() {
                try {
                    jettyWrapper.start();
                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        // wait for jetty to be ready
        latch.await();
    }

    public void testEndpointProperties(){
        String bundleName = "TestEndpointProperties";
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8080/processes/helloWorld",
                    bundleName, "testRequest.soap");
            assertTrue(response.contains("helloResponse") && response.contains("OK!!!"));
        } finally {
            server.undeployProcess(bundleName);
        }
    }

    protected void tearDown() throws Exception {
        jettyWrapper.stop();
        super.tearDown();
    }

}
