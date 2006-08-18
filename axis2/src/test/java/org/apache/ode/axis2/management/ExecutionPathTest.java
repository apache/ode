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
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.ode.tools.sendsoap.cline.HttpSoapSender;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;


public class ExecutionPathTest extends TestCase {

    public static final String PMAPI_NS = "http://www.apache.org/ode/pmapi/types/2006/08/02/";

    private OMFactory _factory;
    private DateFormat xsdDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    public void testListEvents() throws Exception {
        OMElement root = buildMessage("listEvents", new String[] {"instanceFilter", "eventFilter", "maxCount"},
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

        root = buildMessage("getExtensibilityElements",
                new String[] {"pid", "aids"},
                new Object[] { new QName("http://ode/bpel/unit-test", "DynPartnerMain"), saids });
        result = sendToPM(root);

        // These extensibility elements are known to be in the deployed process,
        // checking them here:
        assert(result.toString().indexOf("Receive Order 1") > 0);
        assert(result.toString().indexOf("Receive Order 2") > 0);
        assert(result.toString().indexOf("Receive Order 3") > 0);
        assert(result.toString().indexOf("Receive Order 4") > 0);
    }

    protected void setUp() throws Exception {
        // Create a factory
        _factory = OMAbstractFactory.getOMFactory();

        // Use the factory to create three elements
        OMNamespace depns = _factory.createOMNamespace(PMAPI_NS, "deployapi");
        OMElement root = _factory.createOMElement("deploy", null);
        OMElement namePart = _factory.createOMElement("name", depns);
        namePart.setText("DynPartner");
        OMElement zipPart = _factory.createOMElement("package", depns);
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
        HttpSoapSender.doSend(svcUrl, sis, System.out);
        // Just making sure the instance starts
        Thread.sleep(1000);
    }

    protected void tearDown() throws Exception {
        // Prepare undeploy message
        OMNamespace depns = _factory.createOMNamespace(PMAPI_NS, "deployapi");
        OMElement root = _factory.createOMElement("undeploy", depns);
        OMElement part = _factory.createOMElement("processName", null);
        part.setText("DynPartner");
        root.addChild(part);

        // Undeploy
        sendToDeployment(root);

        OMElement listRoot = buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerMain", ""});
        OMElement result = sendToPM(listRoot);
        assert(result.toString().indexOf("process-info") < 0);
    }

    private OMElement sendToPM(OMElement msg) throws AxisFault {
        return send(msg, "http://localhost:8080/ode/services/ProcessManagement");
    }

    private OMElement sendToIM(OMElement msg) throws AxisFault {
        return send(msg, "http://localhost:8080/ode/services/InstanceManagement");
    }

    private OMElement sendToDeployment(OMElement msg) throws AxisFault {
        return send(msg, "http://localhost:8080/ode/services/DeploymentService");
    }

    private OMElement send(OMElement msg, String url) throws AxisFault {
        Options options = new Options();
        EndpointReference target = new EndpointReference(url);
        options.setTo(target);

        ServiceClient serviceClient = new ServiceClient();
        serviceClient.setOptions(options);

        return serviceClient.sendReceive(msg);
    }

    private OMElement buildMessage(String operation, String[] params, Object[] values) {
        OMNamespace pmns = _factory.createOMNamespace(PMAPI_NS, "pmapi");
        OMElement root = _factory.createOMElement(operation, pmns);
        for (int m = 0; m < params.length; m++) {
            OMElement omelmt = _factory.createOMElement(params[m], null);
            if (values[m] instanceof String)
                omelmt.setText((String) values[m]);
            else if (values[m] instanceof QName)
                omelmt.setText((QName) values[m]);
            else if (values[m] instanceof OMElement)
                omelmt.addChild((OMElement) values[m]);
            else if (values[m] instanceof Object[]) {
                Object[] subarr = (Object[]) values[m];
                String elmtName = (String) subarr[0];
                for (int p = 1; p < subarr.length; p++) {
                    OMElement omarrelmt = _factory.createOMElement(elmtName, null);
                    omarrelmt.setText(subarr[p].toString());
                    omelmt.addChild(omarrelmt);
                }
            } else throw new UnsupportedOperationException("Type " + values[m].getClass() + "isn't supported as " +
                    "a parameter type (only String and QName are).");
            root.addChild(omelmt);
        }
        return root;
    }

}
