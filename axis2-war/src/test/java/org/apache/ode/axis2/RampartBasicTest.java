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
import org.testng.annotations.Test;
import org.testng.annotations.Factory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import static org.testng.AssertJUnit.assertTrue;

import java.net.URL;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileFilter;
import java.util.Arrays;

/**
 *
 *
 */
public class RampartBasicTest extends Axis2TestBase {

    @DataProvider(name = "bundles")
    public Object[][] testPolicySamples() throws Exception {
        File[] policies = new File(getClass().getClassLoader().getResource("TestRampartBasic").getFile()).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().matches("process-basic-sample\\d*");
            }
        });
        Object[][] bundles = new Object[policies.length][];
        for (int i = 0; i < policies.length; i++) {
            bundles[i] = new Object[]{"TestRampartBasic/" + policies[i].getName()};
        }
//        bundles = new Object[][]{new Object[]{"TestRampartBasic/process-basic-sample02"}};
        return bundles;
    }


    @BeforeClass
    protected void setUp() throws Exception {
        // mind the annotation above also
        startServer("TestRampartBasic", "webapp/WEB-INF/conf/axis2.xml");
//        try{
//            while(true){
//                synchronized (this){
//                    wait(50);
//                }
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
    }

    @AfterClass
    protected void tearDown() throws Exception {
        // simply change the annotation, see above
        super.tearDown();
    }

    @Test(dataProvider = "bundles")
    public void executeProcess(String bundleName) throws Exception {
        if (server.isDeployed(new File(bundleName).getName())){
            server.undeployProcess(bundleName);
        }
        server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8888/processes/helloWorld",
                    bundleName, "testRequest.soap");
            System.out.println(response);
            assertTrue(response.contains("helloResponse") && response.contains("Hello World"));
        } finally {
            server.undeployProcess(bundleName);
        }
    }
}