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
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;

public class DeploymentTest extends TestCase {

    public void testDeployUndeploy() throws Exception {
        // Create a factory
        OMFactory factory = OMAbstractFactory.getOMFactory();

        // Use the factory to create three elements
        OMNamespace depns = factory.createOMNamespace("http://www.apache.org/ode/pmapi","deployapi");
        OMElement root = factory.createOMElement("deploy", null);
        OMElement namePart = factory.createOMElement("name", depns);
        namePart.setText("DynPartner");
        OMElement zipPart = factory.createOMElement("package", depns);
        OMElement zipElmt = factory.createOMElement("zip", depns);

        // Add the zip to deploy
        // TODO figure out a way to get a process zip
        FileInputStream fis = new FileInputStream("/home/dusty/tmp/DynPartner.zip");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int b = fis.read(); b >= 0; b = fis.read()) {
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

        // Prepare undeploy message
        root = factory.createOMElement("undeploy", depns);
        OMElement part = factory.createOMElement("processName", null);
        part.setText("DynPartner");
        root.addChild(part);

        // Undeploy
//    sendToDeployment(root);
    }

    private OMElement sendToDeployment(OMElement msg) throws AxisFault {
        Options options = new Options();
        EndpointReference target = new EndpointReference("http://localhost:8080/ode/services/DeploymentService");
        options.setTo(target);

        ServiceClient serviceClient = new ServiceClient();
        serviceClient.setOptions(options);

        return serviceClient.sendReceive(msg);
    }

}
