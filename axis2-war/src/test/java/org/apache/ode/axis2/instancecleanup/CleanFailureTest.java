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

package org.apache.ode.axis2.instancecleanup;

import org.apache.ode.axis2.DummyService;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.testng.annotations.Test;

public class CleanFailureTest extends CleanTestBase {
    @Test(dataProvider="configs")
    public void testCleanAll() throws Exception {
        String bundleName = "TestCleanFailure";
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);
        try {
            initialLargeDataCount = getLargeDataCount(0);
            server.sendRequestFile("http://localhost:8888/processes/helloWorld", bundleName, "testRequest.soap");
        } finally {
            ProcessDAO process = getProcess();
            server.undeployProcess(bundleName);
            assertProcessCleanup(process);
        }
    }

    public String getODEConfigDir() {
        return JPA_DERBY_CONF_DIR;
    }

    protected ProcessInstanceDAO getInstance() {
        return JpaDaoConnectionFactoryImpl.getInstance();
    }

    protected ProcessDAO getProcess() {
        return JpaDaoConnectionFactoryImpl.getProcess();
    }

    @Override
    protected int getLargeDataCount(int echoCount) throws Exception {
        return echoCount;
    }
}