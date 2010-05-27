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

import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.engine.cron.SystemSchedulesConfig;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SystemCronCleanupTest extends CleanTestBase {
    static {
        String customSchedulesFilePath = SystemCronCleanupTest.class.getClassLoader().getResource("webapp").getFile() + "/WEB-INF/test-schedules.xml";
        System.setProperty(SystemSchedulesConfig.SCHEDULE_CONFIG_FILE_PROP_KEY, customSchedulesFilePath);
    }

//    @Test(dataProvider="configs")
    public void _testCleanAll() throws Exception {
        go("TestSystemCronCleanup", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

//    @Test(dataProvider="configs")
    public void _testCleanExclude() throws Exception {
        go("TestSystemCronCleanup_exclude", 1, 0, 0, 0, 3, 0, 6, 2, 3, 6, 59, 76);
    }

    @BeforeClass
    public void setCustomCronSchedules() {
        String customSchedulesFilePath = SystemCronCleanupTest.class.getClassLoader().getResource("webapp").getFile() + "/WEB-INF/test-schedules.xml";
        System.setProperty(SystemSchedulesConfig.SCHEDULE_CONFIG_FILE_PROP_KEY, customSchedulesFilePath);
    }
    
    @AfterClass
    public void resetCustomCronSchedules() {
        System.getProperties().remove(SystemSchedulesConfig.SCHEDULE_CONFIG_FILE_PROP_KEY);
    }
    
    protected void go(String bundleName, int instances, int activityRecoveries, int correlationSets, int faults, int exchanges, int routes, int messsages, int partnerLinks, int scopes, int variables, int events, int largeData) throws Exception {
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);
        ProcessDAO process = null;
        try {
            initialLargeDataCount = getLargeDataCount(0);
            
            server.sendRequestFile("http://localhost:8888/processes/FirstProcess/FirstProcess/FirstProcess/Client", bundleName, "testRequest.soap");
            // every second, clean up cron job kicks in
            Thread.sleep(2000);
            LogFactory.getLog(SystemCronCleanupTest.class).debug("============ASSERT INSTANCE CLEANUP===============");
            process = assertInstanceCleanup(instances, activityRecoveries, correlationSets, faults, exchanges, routes, messsages, partnerLinks, scopes, variables, events, largeData);
        } finally {
            server.undeployProcess(bundleName);
            assertProcessCleanup(process);
        }
    }

    public String getODEConfigDir() {
        return HIB_DERBY_CONF_DIR;
    }
    
    protected ProcessInstanceDAO getInstance() {
        return HibDaoConnectionFactoryImpl.getInstance();
    }

}
