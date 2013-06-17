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

package org.apache.ode.axis2.rampart.policy;

import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.il.OMUtils;
import org.apache.ode.axis2.Axis2TestBase;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.Policy;
import org.apache.rampart.RampartMessageData;

import java.io.File;
import java.io.FileFilter;

/**
 *
 *
 */
public class SecuredProcessesTest extends Axis2TestBase {

    private String testDir = "TestRampartPolicy/secured-processes";
    private String clientRepo = getClass().getClassLoader().getResource(testDir).getFile();


    @DataProvider(name = "secured-processes-bundles")
    public Object[][] testPolicySamples() throws Exception {
        File[] samples = new File(getClass().getClassLoader().getResource(testDir).getFile()).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().matches("process-sample\\d*");
            }
        });
        Object[][] bundles = new Object[samples.length][];
        for (int i = 0; i < samples.length; i++) {
            String sampleIndex = samples[i].getName().replace("process-", "");
            String policyFile = clientRepo + "/" + sampleIndex + "-policy.xml";
            bundles[i] = new Object[]{testDir + "/" + samples[i].getName(), clientRepo, policyFile};
        }
//        bundles = new Object[][]{new Object[]{testDir+"/process-sample04", clientRepo, clientRepo+"/sample04-policy.xml"}};
        return bundles;
    }


    @BeforeMethod
    protected void setUp() throws Exception {
        // mind the annotation above: start the server only once for all tests
        startServer(testDir, "webapp/WEB-INF/conf/axis2.xml");
    }

    @AfterMethod
    protected void tearDown() throws Exception {
        // mind the annotation above: start the server only once for all tests
        super.tearDown();
    }

    @Test(dataProvider = "secured-processes-bundles")
    public void invokeSecuredProcesses(String bundleName, String clientRepo, String policyFile) throws Exception {
        if (server.isDeployed(new File(bundleName).getName())) {
            server.undeployProcess(bundleName);
        }
        server.deployProcess(bundleName);
        try {
            ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(clientRepo, null);
            ServiceClient client = new ServiceClient(ctx, null);
            Options options = new Options();
            // Rampart SymetricBinding (sample04) blows up if not provided with a soap action
            options.setAction("");
            options.setTo(new EndpointReference("http://localhost:"+getTestPort(0)+"/processes/helloWorld"));
            options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, loadPolicy(policyFile));
            client.setOptions(options);

            client.engageModule("rampart");
            client.engageModule("rahas");

            OMElement responseElement = client.sendReceive(getPayload(bundleName));
            String response = DOMUtils.domToString(OMUtils.toDOM(responseElement));
            System.out.println(response);

            System.out.println(response);
            assertTrue("'" + response + "' didn't contain expected content.", response.contains("helloResponse") && response.contains("Hello " + bundleName + "!"));
        } finally {
            server.undeployProcess(bundleName);
        }
    }

    @Test
    public void standAlonePolicy() throws Exception {
        invokeSecuredProcesses(testDir+"/process-sample02_standalone_policy", clientRepo, clientRepo+"/sample02-policy.xml");
    }

    private static Policy loadPolicy(String xmlPath) throws Exception {
        StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }

    private static OMElement getPayload(String value) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://ode/bpel/unit-test.wsdl", "ns1");
        OMElement elem = factory.createOMElement("hello", ns);
        OMElement childElem = factory.createOMElement("TestPart", null);
        childElem.setText(value);
        elem.addChild(childElem);

        return elem;
    }
}
