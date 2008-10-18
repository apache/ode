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

import org.apache.axis2.transport.http.SimpleHTTPServer;

import java.net.URL;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileFilter;

/**
 *
 *
 */
public class RampartTest extends Axis2TestBase {

    protected void setUp() throws Exception {
        startServer("TestRampart", "webapp/WEB-INF/conf/axis2.xml");
    }

    public void testPolicySamples() throws Exception {
        File[] policies = new File(server.getBundleDir("TestRampart")).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().matches("process-policy-sample\\d*");
            }
        });
        for (int i = 0; i < policies.length; i++) {
            String prevPackage = i>0?policies[i-1].getName():null;
            String nextPackage = policies[i].getName();
            // make sure everything is clean to avoid side effects
            if (prevPackage!=null && server.isDeployed(prevPackage)) server.undeployProcess("TestRampart/"+prevPackage);
            if (server.isDeployed(nextPackage)) server.undeployProcess("TestRampart/"+nextPackage);

            executeTest("TestRampart/"+nextPackage);
        }
    }

    public void executeTest(String bundleName) throws Exception {
        server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8080/processes/helloWorld",
                    bundleName, "testRequest.soap");
            System.out.println(response);
            assertTrue(response.contains("helloResponse") && response.contains("Hello World"));
        } finally {
            server.undeployProcess(bundleName);
        }
    }
}
