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

import static org.testng.AssertJUnit.assertTrue;

import org.apache.ode.axis2.Axis2TestBase;
import org.apache.ode.axis2.ODEConfigDirAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class CorrelationMultiScopeEarlyMessageTest extends Axis2TestBase implements ODEConfigDirAware {
    private static final Logger log = LoggerFactory.getLogger(CorrelationMultiScopeEarlyMessageTest.class);

    @Test(dataProvider="configs")
    public void testEarlyMessageRouting() throws Exception {
        String bundleName = "TestCorrelationMultiScopeComplex";
        String endpoint = "http://localhost:8888/ode/processes/TestCorrelationMultiScopeComplexService";

        try{
            if (server.isDeployed(bundleName+"/1")) 
                server.undeployProcess(bundleName + "/1");
            if (server.isDeployed(bundleName+"/2")) 
                server.undeployProcess(bundleName + "/2");

            //deploy version 1
            server.deployProcess(bundleName + "/1");

            //create an instance of version 1
            String instance1Response = server.sendRequestFile(endpoint,bundleName + "/1", "initiate.soap");
            log.info("response from instance 1 {}",instance1Response);
            assertTrue(instance1Response.contains("iid"));

            String instance2Response = server.sendRequestFile(endpoint,bundleName + "/1", "second_initiate.soap");
            log.info("response from instance 2 {}",instance2Response);
            assertTrue(instance2Response.contains("iid"));

            String instance3Response = server.sendRequestFile(endpoint,bundleName + "/1", "third_initiate.soap");
            log.info("response from instance 3 {}",instance3Response);
            assertTrue(instance3Response.contains("iid"));

            //deploy version 2
            server.deployProcess(bundleName + "/2");

            //create and instance of version 2
            String instance4Response = server.sendRequestFile(endpoint,bundleName + "/2", "initiate.soap");
            log.info("response from instance 4 {}",instance4Response);
            assertTrue(instance4Response.contains("iid"));

            //early messages to instance of retired process version 1
            server.sendRequestFile(endpoint,bundleName + "/1", "receive3.soap");
            server.sendRequestFile(endpoint,bundleName + "/1", "second_receive3.soap");
            server.sendRequestFile(endpoint,bundleName + "/1", "third_receive3.soap");

            //early message to instance of active process version 2
            server.sendRequestFile(endpoint,bundleName + "/2", "receive3.soap");

            server.sendRequestFile(endpoint,bundleName + "/1", "receive2.soap");
            server.sendRequestFile(endpoint,bundleName + "/1", "second_receive2.soap");
            server.sendRequestFile(endpoint,bundleName + "/1", "third_receive2.soap");

            server.sendRequestFile(endpoint,bundleName + "/2", "receive2.soap");

            instance1Response = server.sendRequestFile(endpoint,bundleName + "/1", "complete.soap");
            log.info("response from instance 1 {}",instance1Response);
            assertTrue(instance1Response.contains("iid"));

            instance2Response = server.sendRequestFile(endpoint,bundleName + "/1", "second_complete.soap");
            log.info("response from instance 2 {}",instance2Response);
            assertTrue(instance2Response.contains("iid"));

            instance3Response = server.sendRequestFile(endpoint,bundleName + "/1", "third_complete.soap");
            log.info("response from instance 3 {}",instance3Response);
            assertTrue(instance3Response.contains("iid"));

            instance4Response = server.sendRequestFile(endpoint,bundleName + "/2", "complete.soap");
            log.info("response from instance 4 {}",instance4Response);
            assertTrue(instance4Response.contains("iid"));
        } finally {
            if (server.isDeployed(bundleName+"/1")) 
                server.undeployProcess(bundleName + "/1");
            if (server.isDeployed(bundleName+"/2")) 
                server.undeployProcess(bundleName + "/2");
        }
    }

    public String getODEConfigDir() {
        return HIB_H2_CONF_DIR;
    }
}
