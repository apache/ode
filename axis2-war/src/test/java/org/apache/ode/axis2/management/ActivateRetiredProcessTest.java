/* Licensed to the Apache Software Foundation (ASF) under one
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
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.pmapi.ProcessInfoDocument;
import org.apache.ode.bpel.pmapi.TProcessInfo;

import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;


public class ActivateRetiredProcessTest extends Axis2TestBase {
    /**
     *  Test case method for ODE-958
     * @throws Exception
     */
    @Test(dataProvider="configs")
    public void testActivateRetiredProcess() throws Exception {
        String bundleName = "TestInstanceRetire";
        System.out.println("=> " + server.getODEServer().getProcessStore().getPackages());
        if (server.isDeployed("1")) server.undeployProcess(bundleName + "/1");
        if (server.isDeployed("2")) server.undeployProcess(bundleName + "/2");

        QName pid1 = server.deployProcess(bundleName + "/1").iterator().next();

        server.getODEServer().getProcessManagement().setPackageRetired("1", true);

        QName pid2 = server.deployProcess(bundleName + "/2").iterator().next();

        try{
            server.getODEServer().getProcessManagement().setPackageRetired("1", false);
        }catch (ContextException e) {
        }

        TProcessInfo pInfo = getProcessDetails(pid1);
        assertTrue(ProcessState.RETIRED.name().equals(pInfo.getStatus().toString()));

        if (server.isDeployed("1")) server.undeployProcess(bundleName + "/1");
        if (server.isDeployed("2")) server.undeployProcess(bundleName + "/2");
    }

    public TProcessInfo getProcessDetails(QName pid){
        ProcessInfoDocument pDoc = server.getODEServer().getProcessManagement().getProcessInfo(pid);
        return pDoc.getProcessInfo();
    }
}
