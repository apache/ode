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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class DeploymentTest extends TestCase {

    private ServiceClientUtil _client;

    public void testDeployUndeploy() throws Exception {
        // Create a factory
        OMFactory factory = OMAbstractFactory.getOMFactory();

        // Use the factory to create three elements
        OMNamespace depns = factory.createOMNamespace(Namespaces.ODE_PMAPI,"deployapi");
        OMElement root = factory.createOMElement("deploy", null);
        OMElement namePart = factory.createOMElement("name", depns);
        namePart.setText("DynPartner");
        OMElement zipPart = factory.createOMElement("package", depns);
        OMElement zipElmt = factory.createOMElement("zip", depns);

        // Add the zip to deploy
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("DynPartner.zip");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) {
            outputStream.write((byte) b);
        }
        String base64Enc = Base64.encode(outputStream.toByteArray());
        OMText zipContent = factory.createOMText(base64Enc, "application/zip", true);
        root.addChild(namePart);
        root.addChild(zipPart);
        zipPart.addChild(zipElmt);
        zipElmt.addChild(zipContent);

        // Deploy
        sendToDeployment(root);

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

        // Prepare undeploy message
        root = factory.createOMElement("undeploy", depns);
        OMElement part = factory.createOMElement("processName", null);
        part.setText("DynPartner");
        root.addChild(part);

        // Undeploy
        sendToDeployment(root);

        listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerMain", ""});
        result = sendToPM(listRoot);
        assert(result.toString().indexOf("process-info") < 0);
    }

    protected void setUp() throws Exception {
        _client = new ServiceClientUtil();
    }

    private OMElement sendToPM(OMElement msg) throws AxisFault {
        return _client.send(msg, "http://localhost:8080/ode/services/ProcessManagement");
    }

    private OMElement sendToDeployment(OMElement msg) throws AxisFault {
        return _client.send(msg, "http://localhost:8080/ode/services/DeploymentService");
    }

}
