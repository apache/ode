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

import static org.testng.Assert.fail;

import org.testng.annotations.Test;

public class BpelActivityTest extends Axis2TestBase implements ODEConfigDirAware {
    @Test(dataProvider="configs")
    public void testThrowOnEvent() throws Exception {
        final String bundleName = "TestThrowOnEvent";
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);

        new Thread("SECOND CLIENT") {
            public void run() {
                try {
                    Thread.sleep(3000);
                    String response = server.sendRequestFile("http://localhost:8888/ode/processes/OnEventCorrelation/",
                            bundleName, "testRequest.soap");
                } catch( Exception e ) {
                    fail(e.getMessage());
                }
            }
        }.start();

        new Thread("THIRD CLIENT") {
          public void run() {
              try {
                  Thread.sleep(6000);
                  server.sendRequestFile("http://localhost:8888/ode/processes/OnEventCorrelation/",
                            bundleName, "testRequest.soap");
              } catch( Exception e ) {
                  fail(e.getMessage());
              } finally {
                  try {
                      Thread.sleep(1000);
                  } catch( Exception e2 ) {
                  }
                  server.undeployProcess(bundleName);
              }
          }
        }.start();

        try {
            Thread.currentThread().setName("FIRST CLIENT");
            server.sendRequestFile("http://localhost:8888/ode/processes/OnEventCorrelation/",
                bundleName, "testRequest.soap");
            Thread.sleep(9000);
        } catch( Exception e ) {
            fail(e.getMessage());
        }
    }

    public String getODEConfigDir() {
        return HIB_DERBY_CONF_DIR;
    }
}
