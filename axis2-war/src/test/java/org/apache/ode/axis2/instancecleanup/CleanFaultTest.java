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

import static org.testng.AssertJUnit.fail;

import org.apache.ode.axis2.DummyService;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.iapi.ContextException;
import org.testng.annotations.Test;

public class CleanFaultTest extends CleanTestBase {
    @Test(dataProvider="configs")
    public void testCleanNone() throws Exception {
        go("TestCleanFault_None", 1, 0, 0, 1, 2, 0, 4, 2, 3, 2, 38, 47);
    }

    @Test(dataProvider="configs")
    public void testCleanInstance() throws Exception {
        try {
            go("TestCleanSuccess_Instance", 0, 0, 0, 0, 3, 0, 6, 2, 3, 6, 59, 70);
            fail("Shoud throw a runtime exception: you cannot use the instance category without the variables and correlations categories.");
        } catch(ContextException re) {}
    }

    @Test(dataProvider="configs")
    public void testCleanVariables() throws Exception {
        go("TestCleanFault_Variables", 1, 0, 0, 1, 2, 0, 4, 0, 0, 0, 38, 45);
    }

    @Test(dataProvider="configs")
    public void testCleanMessages() throws Exception {
        go("TestCleanFault_Messages", 1, 0, 0, 1, 0, 0, 0, 2, 3, 2, 38, 41);
    }

    @Test(dataProvider="configs")
    public void testCleanCorrelations() throws Exception {
        go("TestCleanFault_Correlations", 1, 0, 0, 1, 2, 0, 4, 2, 3, 2, 38, 47);
    }

    @Test(dataProvider="configs")
    public void testCleanEvents() throws Exception {
        go("TestCleanFault_Events", 1, 0, 0, 1, 2, 0, 4, 2, 3, 2, 0, 9);
    }

    @Test(dataProvider="configs")
    public void testCleanMessageCorrEvents() throws Exception {
        go("TestCleanFault_MessageCorrEvents", 1, 0, 0, 1, 0, 0, 0, 2, 3, 2, 0, 3);
    }

    @Test(dataProvider="configs")
    public void testCleanAll() throws Exception {
        go("TestCleanFault_All", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    protected void go(String bundleName, int instances, int activityRecoveries, int correlationSets, int faults, int exchanges, int routes, int messsages, int partnerLinks, int scopes, int variables, int events, int largeData) throws Exception {
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);
        ProcessDAO process = null;
        try {
            initialLargeDataCount = getLargeDataCount(0);

            String response = server.sendRequestFile("http://localhost:8888/processes/helloWorld", bundleName, "testRequest.soap");
            System.out.println(response);
            assertInstanceCleanup(instances, activityRecoveries, correlationSets, faults, exchanges, routes, messsages, partnerLinks, scopes, variables, events, largeData);
            process = assertInstanceCleanup(instances, activityRecoveries, correlationSets, faults, exchanges, routes, messsages, partnerLinks, scopes, variables, events, largeData);
        } finally {
            server.undeployProcess(bundleName);
            assertProcessCleanup(process);
        }
    }

    public String getODEConfigDir() {
        return JPA_DERBY_CONF_DIR; 
    }

    @Override
    protected ProcessInstanceDAO getInstance() {
        return JpaDaoConnectionFactoryImpl.getInstance();
    }

    @Override
    protected int getLargeDataCount(int echoCount) throws Exception {
        return echoCount;
    }
}