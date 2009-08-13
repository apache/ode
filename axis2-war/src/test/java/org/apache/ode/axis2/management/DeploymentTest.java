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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.ode.axis2.Axis2TestBase;
import org.apache.ode.axis2.service.ServiceClientUtil;
import org.apache.ode.utils.Namespaces;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class DeploymentTest extends Axis2TestBase {
    private static final Log __log = LogFactory.getLog(DeploymentTest.class);

    private OMFactory _factory;
    private ServiceClientUtil _client;

    private ArrayList<QName> _deployed = new ArrayList<QName>();
    private String _package;

  @Test
    public void testDeployUndeploy() throws Exception {
        // Setup and tear down are doing ost of the job here, just checking in the middle

        // Check deployment
        OMElement listRoot = _client.buildMessage("listProcesses", new String[0], new String[0]);
        OMElement result = sendToPM(listRoot);

        // look for DynPartnerMain-xxx
        listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name="+_deployed.get(0).getLocalPart(), ""});
        result = sendToPM(listRoot);
        result = result.getFirstElement();

        assertEquals("process-info-list", result.getLocalName());
        OMElement child = result.getFirstElement();
        assertNotNull("Missing deployed process", child);
        assertEquals("process-info", child.getLocalName());
        OMElement pid = child.getFirstElement();
        assertEquals(_deployed.get(0).toString(), pid.getText());

        // look for DynPartnerResponder-xxx
        listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name="+_deployed.get(1).getLocalPart(), ""});
        result = sendToPM(listRoot);
        result = result.getFirstElement();
        assertEquals("process-info-list", result.getLocalName());
        child = result.getFirstElement();
        assertNotNull("Missing deployed process", child);
        assertEquals("process-info", child.getLocalName());
        assertEquals("process-info", child.getLocalName());
        pid = child.getFirstElement();
        assertEquals(_deployed.get(1).toString(), pid.getText());
    }

  @Test
    public void testListDeployedPackages() throws Exception {
        OMElement root = _client.buildMessage("listDeployedPackages", new String[] {}, new String[] {});
        OMElement result = sendToDeployment(root);
        assertTrue(result.toString().indexOf(_package) > 0);
    }

  @Test
    public void testListProcesses() throws Exception {
        OMElement root = _client.buildMessage("listProcesses", new String[] {"packagesNames"},
                new String[] {_package});
        OMElement result = sendToDeployment(root);
        assertTrue(result.toString().indexOf("http://ode/bpel/unit-test")>=0);
        assertTrue(result.toString().indexOf("DynPartnerMain")>=0);
        assertTrue(result.toString().indexOf("http://ode/bpel/responder")>=0);
        assertTrue(result.toString().indexOf("DynPartnerResponder")>=0);
    }

  @Test
    public void testGetProcessPackage() throws Exception {
        OMElement root = _client.buildMessage("getProcessPackage", new String[] {"processId"},
                new Object[] { _deployed.get(0) } );
        OMElement result = sendToDeployment(root);
        assertEquals(_package, result.getFirstElement().getText());

        OMElement root2 = _client.buildMessage("getProcessPackage", new String[] {"processId"},
                new Object[] { _deployed.get(1) } );
        OMElement result2 = sendToDeployment(root2);
        assertEquals(_package, result2.getFirstElement().getText());
    }

  @Test
    public void testMultipleDeployUndeployVersion() throws Exception {
        ArrayList<String> deployed = new ArrayList<String>();
        // Testing that versions are monotonically increased
        int lastVer = Integer.parseInt(_package.substring(_package.lastIndexOf("-") + 1, _package.length()));
        for (int m = 1; m <= 3; m++) {
            String depPack = deploy();
            int ver = Integer.parseInt(depPack.substring(depPack.lastIndexOf("-") + 1, depPack.length()));
            assertEquals(lastVer + m, ver);
            deployed.add(depPack);
        }
        // Deploying a couple of "tagged" versions
        String depPack = deploy("foo");
        int ver = Integer.parseInt(depPack.substring(depPack.lastIndexOf("-") + 1, depPack.length()));
        assertTrue(lastVer + 4 <= ver);
        deployed.add(depPack);

        depPack = deploy("bar");
        ver = Integer.parseInt(depPack.substring(depPack.lastIndexOf("-") + 1, depPack.length()));
        assertTrue(lastVer + 5 <= ver);
        deployed.add(depPack);

        // Cleaning up
        for (String aDeployed : deployed) {
            undeploy(aDeployed);
        }
    }

    @Test
    public void undeployBlankPackage(){
        try {
            undeploy(null);
            fail("Undeploying a null or empty package must fail");
        } catch (Exception e) {
            __log.debug("got exception from deployment" + e.getMessage());
            String expectedMsg = "Invalid bundle name";
            assertTrue("Unexpected exception received", e.getMessage().contains("operation undeploy") && e.getMessage().contains(expectedMsg));
        }
    }

  @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
        // Create a factory
        _factory = OMAbstractFactory.getOMFactory();
        _client = new ServiceClientUtil();

        _package = deploy();

        assertNotNull(_package);
        assertEquals(2, _deployed.size());

        Thread.sleep(1000);
    }

  @AfterMethod
    protected void tearDown() throws Exception {
        undeploy(_package);
        super.tearDown();
    }

    private String deploy(String packageName) throws Exception {
        // Use the factory to create three elements
        OMNamespace depns = _factory.createOMNamespace(Namespaces.ODE_DEPLOYAPI_NS, "deployapi");
        OMElement root = _factory.createOMElement("deploy", depns);
        OMElement namePart = _factory.createOMElement("name", null);
        namePart.setText(packageName);
        OMElement zipPart = _factory.createOMElement("package", null);
        OMElement zipElmt = _factory.createOMElement("zip", depns);

        // Add the zip to deploy
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("DynPartner.zip");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while((len = is.read(buffer)) >= 0) {
            outputStream.write(buffer, 0, len);
        }
        String base64Enc = Base64.encode(outputStream.toByteArray());
        OMText zipContent = _factory.createOMText(base64Enc, "application/zip", true);
        root.addChild(namePart);
        root.addChild(zipPart);
        zipPart.addChild(zipElmt);
        zipElmt.addChild(zipContent);

        // Deploy
        OMElement result = sendToDeployment(root);

        _deployed.clear();
        String pakage = null;
        Iterator iter = result.getFirstElement().getChildElements();
        while (iter.hasNext()) {
            OMElement e = (OMElement) iter.next();
            if (e.getLocalName().equals("name")) {
                pakage = e.getText();
            }
            if (e.getLocalName().equals("id")) {
                _deployed.add(e.getTextAsQName());
            }
        }
        return pakage;
    }

    private String deploy() throws Exception {
        return deploy("DynPartner");
    }

    private void undeploy(String pakage) throws Exception {
        // Prepare undeploy message
        OMNamespace depns = _factory.createOMNamespace(Namespaces.ODE_DEPLOYAPI_NS, "deployapi");
        OMElement root = _factory.createOMElement("undeploy", depns);
        OMElement part = _factory.createOMElement("packageName", null);
        part.setText(pakage);
        root.addChild(part);

        // Undeploy
        sendToDeployment(root);

        OMElement listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerMain", ""});
        OMElement result = sendToPM(listRoot);
    }

    private OMElement sendToPM(OMElement msg) throws AxisFault {
        return _client.send(msg, "http://localhost:8888/processes/ProcessManagement");
    }

    private OMElement sendToDeployment(OMElement msg) throws AxisFault {
        return _client.send(msg, "http://localhost:8888/processes/DeploymentService");
    }

}
