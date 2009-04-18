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

import org.apache.axis2.AxisFault;
import org.testng.annotations.Test;

/**
 * This test requires very specific timing values to work which is why it's set to ignored, it
 * probably wouldn't work on most machines. BpelRuntimeContextImpl.scheduleInvokeCheck also has to
 * use a timer value of 5s instead of 180s (a bit too long for testing).
 */
public class FailureInvokeTest extends Axis2TestBase {

  @Test(enabled = false)
    public void testSimpleFaultCatch() throws Exception {
        String bundleName = "TestFailureInInvoke";
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);

        try {
            new Thread(new Killer(this)).start();
            try {
                String response = server.sendRequestFile("http://localhost:8888/processes/invokeFailureTest",
                        bundleName, "testRequest.soap");
                System.out.println("=> " + response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            startServer();
            Thread.sleep(15000);
        } finally {
            server.undeployProcess(bundleName);
        }
    }

    private class Killer implements Runnable {
        private FailureInvokeTest test;

        private Killer(FailureInvokeTest test) {
            this.test = test;
        }

        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                test.stopServer();
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();
            }
        }
    }

}
