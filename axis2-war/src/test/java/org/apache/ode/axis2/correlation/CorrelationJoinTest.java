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

package org.apache.ode.axis2.correlation;

import org.apache.ode.axis2.Axis2TestBase;
import org.apache.ode.axis2.DummyService;
import org.apache.ode.axis2.ODEConfigDirAware;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CorrelationJoinTest extends Axis2TestBase implements ODEConfigDirAware {
    /**
     * Tests rendezvous
     *
     * @throws Exception
     */
    @Test(dataProvider="configs")
    public void testCorrelationJoin() throws Exception {
        final String bundleName = "TestCorrelationJoin";

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
            String response = server.sendRequestFile("http://localhost:8888/processes/correlationMultiTest",
                    bundleName, "testRequest.soap");
            System.out.println("=>\n" + response);
            assertTrue(response.contains(">1;2;3;<"));
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            server.undeployProcess(bundleName);
        }
    }

    public String getODEConfigDir() {
        return getClass().getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.jpa-derby";
    }
}