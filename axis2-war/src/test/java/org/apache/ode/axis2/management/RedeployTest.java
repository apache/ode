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

package org.apache.ode.axis2.management;

import org.apache.ode.axis2.Axis2TestBase;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

/**
 * @author Anurag Aggarwal <anurag@intalio.com>
 */
public class RedeployTest extends Axis2TestBase {

    // @Test(dataProvider="configs")
    // not supposed to work, user should not rm .deploy folder maybe ?
    public void testRedeploy() throws Exception {
        String bundleName = "TestInstanceRetire";
        System.out.println("=> " + server.getODEServer().getProcessStore().getPackages());
        if (server.isDeployed("withVar")) server.undeployProcess(bundleName + "/withVar");

        QName deployedQName = server.deployProcess(bundleName + "/withVar").iterator().next();

        server.sendRequestFile("http://localhost:8888/processes/testretire",
                bundleName + "/1", "testRequest1.soap");

        server.undeployProcess(bundleName + "/withVar");
        server.deployProcess(bundleName + "/withVar").iterator().next();

        String response = server.sendRequestFile("http://localhost:8888/processes/testretire",
                bundleName + "/1", "testRequest2.soap");
    }
}
