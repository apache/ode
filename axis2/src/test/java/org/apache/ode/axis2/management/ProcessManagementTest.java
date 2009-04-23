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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * @deprecated Needs to be migrated in axis2-war and upgraded to the new Axis2TestBase test fwk.
 */
public class ProcessManagementTest extends TestCase {

    private OMFactory _factory;
    private DateFormat xsdDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    private ServiceClientUtil _client;

    public void testListProcesses() throws Exception {
        OMElement listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerMain", ""});
        OMElement result = sendToPM(listRoot);
        // Ensures that there's only 2 process-info string (ending and closing tags) and hence only one process
        assertTrue(result.toString().split("process-info>").length == 3);

        // Another query with more options
        Calendar notSoLongAgo = Calendar.getInstance();
        notSoLongAgo.add(Calendar.MINUTE, -2);
        String notSoLongAgoStr = xsdDF.format(notSoLongAgo.getTime());
        listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerResponder namespace=http://ode/bpel/responder " +
                        "deployed>=" + notSoLongAgoStr, ""});
        result = sendToPM(listRoot);
        assertTrue(result.toString().split("process-info>").length == 3);
    }

    public void testProcessFiles() throws Exception {
        OMElement listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerMain", ""});
        OMElement result = sendToPM(listRoot);
        ArrayList<String> filenames = new ArrayList<String>();
        Iterator docs = result.getFirstElement().getFirstChildWithName(new QName(Namespaces.ODE_PMAPI_TYPES_NS, "documents")).getChildElements();
        while (docs.hasNext()) {
            OMElement docElmt = (OMElement) docs.next();
            filenames.add(docElmt.getFirstChildWithName(new QName(Namespaces.ODE_PMAPI_TYPES_NS, "name")).getText());
        }
        // Checking that all necessary files are really there
        assertTrue(filenames.contains("DynPartnerMain.bpel"));
        assertTrue(filenames.contains("DynPartnerResponder.bpel"));
        assertTrue(filenames.contains("Main.wsdl"));
        assertTrue(filenames.contains("Responder.wsdl"));
    }

    public void testListAllProcesses() throws Exception {
        OMElement root = _client.buildMessage("listAllProcesses", new String[] {}, new String[] {});
        OMElement result = sendToPM(root);
        // Hopefully we have at least two processes (so 4 opening/closing elmts)
        assertTrue(result.toString().split("process-info").length >= 5);
        // And our deployed processes are there
        assertTrue(result.toString().indexOf("DynPartnerMain") >= 0);
        assertTrue(result.toString().indexOf("DynPartnerResponder") >= 0);
    }

    public void testSetProcessProperty() throws Exception {
        OMElement root = _client.buildMessage("setProcessProperty",
                new String[] {"pid", "propertyName", "propertyValue"},
                new Object[] { new QName("http://ode/bpel/unit-test", "DynPartnerMain"),
                        new QName("http://ode/custom/ns", "someprop"), "somevalue" });
        OMElement result = sendToPM(root);
        assertTrue(result.toString().indexOf("DynPartnerMain") >= 0);
        assertTrue(result.toString().indexOf("somevalue") >= 0);
    }

    public void testSetProcessPropertyNode() throws Exception {
        OMElement propElmt = _factory.createOMElement("testprop", null);
        propElmt.setText("propvalue");
        OMElement root = _client.buildMessage("setProcessPropertyNode",
                new String[] {"pid", "propertyName", "propertyValue"},
                new Object[] { new QName("http://ode/bpel/unit-test", "DynPartnerMain"),
                        new QName("http://ode/custom/ns", "someprop"), propElmt });
        OMElement result = sendToPM(root);
        assertTrue(result.toString().indexOf("DynPartnerMain") >= 0);
        assertTrue(result.toString().indexOf("testprop") >= 0);
        assertTrue(result.toString().indexOf("propvalue") >= 0);
    }

    public void testGetExtensibilityElements() throws Exception {
        OMElement root = _client.buildMessage("getExtensibilityElements",
                new String[] {"pid", "aids"},
                new Object[] { new QName("http://ode/bpel/unit-test", "DynPartnerMain"),
                        new String[] {"aid", "34", "37"} });
        OMElement result = sendToPM(root);
        assertTrue(result.toString().indexOf("activity-ext-info>") >= 0);
    }

    protected void setUp() throws Exception {
        // Create a factory
        _factory = OMAbstractFactory.getOMFactory();
        _client = new ServiceClientUtil();

        // Use the factory to create three elements
        OMNamespace pmapi = _factory.createOMNamespace("http://www.apache.org/ode/pmapi", "pmapi");
        OMElement root = _factory.createOMElement("deploy", pmapi); // qualified operation name
        OMElement namePart = _factory.createOMElement("name", null);
        namePart.setText("DynPartner");
        OMElement zipPart = _factory.createOMElement("package", null);
        OMElement zipElmt = _factory.createOMElement("zip", null);

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
        OMNamespace pmapi = _factory.createOMNamespace("http://www.apache.org/ode/pmapi", "pmapi");
        OMElement root = _factory.createOMElement("undeploy", pmapi);  // qualified operation name
        OMElement part = _factory.createOMElement("processName", pmapi);
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
