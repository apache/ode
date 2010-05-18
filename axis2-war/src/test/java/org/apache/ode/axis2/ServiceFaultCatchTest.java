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

import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests that a fault thrown by a called service can be caught and is properly
 * structured so that an assign on a fault sub-element will succeed.
 *
 * @author Matthieu Riou <mriou@apache.org>
 */
public class ServiceFaultCatchTest extends Axis2TestBase {
    @Test(dataProvider="configs")
    public void testSimpleFaultCatch() throws Exception {
        String bundleName = "TestStructuredFault";
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8888/processes/helloWorld",
                    bundleName, "testRequest.soap");
            assertTrue(response.contains("helloResponse") && response.contains("Something went wrong. Fortunately, it was meant to be."));
        } finally {
            server.undeployProcess(bundleName);
        }
    }
}
