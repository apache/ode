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
import org.apache.ode.tools.sendsoap.cline.HttpSoapSender;
import org.apache.ode.utils.Namespaces;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;


/**
 * @deprecated Needs to be migrated in axis2-war and upgraded to the new Axis2TestBase test fwk.
 */
public class ExecutionPathTest extends TestCase {

    private OMFactory _factory;
    private ServiceClientUtil _client;

    public void testListEvents() throws Exception {
        OMElement root = _client.buildMessage("listEvents", new String[] {"instanceFilter", "eventFilter", "maxCount"},
                new String[] {"", "", "0"});
        OMElement result = sendToIM(root);
        HashSet<String> aids = new HashSet<String>();
        Iterator childrenIter = result.getChildElements();
        while (childrenIter.hasNext()) {
            OMElement evtinfo = (OMElement) childrenIter.next();
            Iterator evtElmtIter = evtinfo.getChildElements();
            while (evtElmtIter.hasNext()) {
                OMElement evtelmt = (OMElement) evtElmtIter.next();
                if (evtelmt.getLocalName().equals("activity-definition-id")) {
                    aids.add(evtelmt.getText());
                }
            }
        }

        String[] saids = new String[aids.size() + 1];
        saids[0] = "aid";
        int m = 1;
        for (String s : aids) saids[m++] = s;

        root = _client.buildMessage("getExtensibilityElements",
                new String[] {"pid", "aids"},
                new Object[] { new QName("http://ode/bpel/unit-test", "DynPartnerMain"), saids });
        result = sendToPM(root);

        // These extensibility elements are known to be in the deployed process,
        // checking them here:
        assertTrue(result.toString().indexOf("Receive Order 1") > 0);
        assertTrue(result.toString().indexOf("Receive Order 2") > 0);
        assertTrue(result.toString().indexOf("Receive Order 3") > 0);
        assertTrue(result.toString().indexOf("Receive Order 4") > 0);
        assertTrue(result.toString().indexOf("ID-5938e518-bfcf-1004-8cce-0438cf81d0f0") > 0);
    }

    protected void setUp() throws Exception {
        // Create a factory
        _factory = OMAbstractFactory.getOMFactory();
        _client = new ServiceClientUtil();

        // Use the factory to create three elements
        OMNamespace depns = _factory.createOMNamespace(Namespaces.ODE_DEPLOYAPI_NS, "deployapi");
        OMElement root = _factory.createOMElement("deploy", depns);
        OMElement namePart = _factory.createOMElement("name", null);
        namePart.setText("DynPartner");
        OMElement zipPart = _factory.createOMElement("package", null);
        OMElement zipElmt = _factory.createOMElement("zip", depns);

        // Add the zip to deploy
        InputStream is = getClass().getClassLoader().getResourceAsStream("DynPartner.zip");
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

        // Execute
        URL svcUrl = new URL("http://localhost:8080/ode/processes/DynMainService");
        InputStream sis = getClass().getClassLoader().getResourceAsStream("testDynPartnerRequest.soap");
        System.out.println(HttpSoapSender.doSend(svcUrl, sis, null, 0, null, null, null));
        // Just making sure the instance starts
        Thread.sleep(1000);
    }

    protected void tearDown() throws Exception {
        // Prepare undeploy message
        OMNamespace depns = _factory.createOMNamespace(Namespaces.ODE_DEPLOYAPI_NS, "deployapi");
        OMElement root = _factory.createOMElement("undeploy", depns);
        OMElement part = _factory.createOMElement("packageName", null);
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

    private OMElement sendToIM(OMElement msg) throws AxisFault {
        return _client.send(msg, "http://localhost:8080/ode/services/InstanceManagement");
    }

    private OMElement sendToDeployment(OMElement msg) throws AxisFault {
        return _client.send(msg, "http://localhost:8080/ode/services/DeploymentService");
    }

}
