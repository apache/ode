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

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.ode.axis2.service.ServiceClientUtil;
import org.apache.ode.utils.Namespaces;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class DeploymentTest extends TestCase {

    private OMFactory _factory;
    private ServiceClientUtil _client;

    public void testDeployUndeploy() throws Exception {
        // Setup and tear down are doing ost of the job here, just checking in the middle

        // Check deployment
        OMElement listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerMain", ""});
        OMElement result = sendToPM(listRoot);
        // Ensures that there's only 2 process-info string (ending and closing tags) and hence only one process
        assert(result.toString().split("process-info").length == 3);
        listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerResponder", ""});
        result = sendToPM(listRoot);
        assert(result.toString().split("process-info").length == 3);
    }

    public void testListDeployedPackages() throws Exception {
        OMElement root = _client.buildMessage("listDeployedPackages", new String[] {}, new String[] {});
        OMElement result = sendToDeployment(root);
        assert(result.getFirstElement().getFirstElement().getText().equals("DynPartner"));
    }

    public void testListProcesses() throws Exception {
        OMElement root = _client.buildMessage("listProcesses", new String[] {"packagesNames"},
                new String[] {"DynPartner"});
        OMElement result = sendToDeployment(root);
        assert(result.toString().indexOf("http://ode/bpel/unit-test")>=0);
        assert(result.toString().indexOf("DynPartnerMain")>=0);
        assert(result.toString().indexOf("http://ode/bpel/responder")>=0);
        assert(result.toString().indexOf("DynPartnerResponder")>=0);
        System.out.println(result);
    }

    public void testGetProcessPackage() throws Exception {
        OMElement root = _client.buildMessage("getProcessPackage", new String[] {"processId"},
                new Object[] { new QName("http://ode/bpel/unit-test", "DynPartnerMain") });
        OMElement result = sendToDeployment(root);
        assert(result.getText().equals("DynPartner"));
        System.out.println(result);
    }

    protected void setUp() throws Exception {
        // Create a factory
        _factory = OMAbstractFactory.getOMFactory();
        _client = new ServiceClientUtil();

        // Use the factory to create three elements
        OMNamespace depns = _factory.createOMNamespace(Namespaces.ODE_PMAPI, "deployapi");
        OMElement root = _factory.createOMElement("deploy", null);
        OMElement namePart = _factory.createOMElement("name", depns);
        namePart.setText("DynPartner");
        OMElement zipPart = _factory.createOMElement("package", depns);
        OMElement zipElmt = _factory.createOMElement("zip", depns);

        // Add the zip to deploy
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("DynPartner.zip");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) {
            outputStream.write((byte) b);
        }
        String base64Enc = Base64.encode(outputStream.toByteArray());
        OMText zipContent = _factory.createOMText(base64Enc, "application/zip", true);
        root.addChild(namePart);
        root.addChild(zipPart);
        zipPart.addChild(zipElmt);
        zipElmt.addChild(zipContent);

        // Deploy
        sendToDeployment(root);
    }

    protected void tearDown() throws Exception {
        // Prepare undeploy message
        OMNamespace depns = _factory.createOMNamespace(Namespaces.ODE_PMAPI, "deployapi");
        OMElement root = _factory.createOMElement("undeploy", depns);
        OMElement part = _factory.createOMElement("processName", null);
        part.setText("DynPartner");
        root.addChild(part);

        // Undeploy
        sendToDeployment(root);

        OMElement listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerMain", ""});
        OMElement result = sendToPM(listRoot);
        assert(result.toString().indexOf("process-info") < 0);
    }


    private OMElement sendToPM(OMElement msg) throws AxisFault {
        return _client.send(msg, "http://localhost:8080/ode/services/ProcessManagement");
    }

    private OMElement sendToDeployment(OMElement msg) throws AxisFault {
        return _client.send(msg, "http://localhost:8080/ode/services/DeploymentService");
    }

}
