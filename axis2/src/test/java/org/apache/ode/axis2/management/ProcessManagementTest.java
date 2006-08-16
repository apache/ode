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

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class ProcessManagementTest extends TestCase {

    public static final String PMAPI_NS = "http://www.apache.org/ode/pmapi/types/2006/08/02/";

    private OMFactory _factory;
    private DateFormat xsdDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    public void testListProcesses() throws Exception {
        OMElement listRoot = buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerMain", ""});
        OMElement result = sendToPM(listRoot);
        // Ensures that there's only 2 process-info string (ending and closing tags) and hence only one process
        assert(result.toString().split("process-info").length == 3);

        // Another query with more options
        Calendar notSoLongAgo = Calendar.getInstance();
        notSoLongAgo.add(Calendar.MINUTE, -2);
        String notSoLongAgoStr = xsdDF.format(notSoLongAgo.getTime());
        listRoot = buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerResponder namespace=http://ode/bpel/responder " +
                        "deployed>=" + notSoLongAgoStr, ""});
        result = sendToPM(listRoot);
        assert(result.toString().split("process-info").length == 3);
    }

    public void testProcessFiles() throws Exception {
        OMElement listRoot = buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerMain", ""});
        OMElement result = sendToPM(listRoot);
        System.out.println("=> " + result);
        ArrayList<String> filenames = new ArrayList<String>();
        Iterator docs = result.getFirstElement().getFirstChildWithName(new QName(PMAPI_NS, "documents")).getChildElements();
        while (docs.hasNext()) {
            OMElement docElmt = (OMElement) docs.next();
            filenames.add(docElmt.getFirstChildWithName(new QName(PMAPI_NS, "name")).getText());
        }
        // Checking that all necessary files are really there
        assert(filenames.contains("deploy.xml"));
        assert(filenames.contains("DynPartnerMain.bpel"));
        assert(filenames.contains("DynPartnerResponder.bpel"));
        assert(filenames.contains("Main.wsdl"));
        assert(filenames.contains("Responder.wsdl"));
    }

    public void testListAllProcesses() throws Exception {
        OMElement root = buildMessage("listAllProcesses", new String[] {}, new String[] {});
        OMElement result = sendToPM(root);
        // Hopefully we have at least two processes (so 4 opening/closing elmts)
        assert(result.toString().indexOf("process-info") >= 4);
        // And our deployed processes are there
        assert(result.toString().indexOf("DynPartnerMain") >= 0);
        assert(result.toString().indexOf("DynPartnerResponder") >= 0);
    }

    public void testSetProcessProperty() throws Exception {
        OMElement root = buildMessage("setProcessProperty",
                new String[] {"pid", "propertyName", "propertyValue"},
                new Object[] { new QName("http://ode/bpel/unit-test", "DynPartnerMain"),
                        new QName("http://ode/custom/ns", "someprop"), "somevalue" });
        OMElement result = sendToPM(root);
        assert(result.toString().indexOf("DynPartnerMain") >= 0);
        assert(result.toString().indexOf("somevalue") >= 0);
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
        //create a factory
        OMFactory factory = OMAbstractFactory.getOMFactory();

        //use the factory to create three elements
        OMNamespace pmns = factory.createOMNamespace(PMAPI_NS, "pmapi");
        OMElement root = factory.createOMElement(operation, pmns);
        for (int m = 0; m < params.length; m++) {
            OMElement omelmt = factory.createOMElement(params[m], null);
            if (values[m] instanceof String)
                omelmt.setText((String) values[m]);
            else if (values[m] instanceof QName)
                omelmt.setText((QName) values[m]);
            else throw new UnsupportedOperationException("Type " + values[m].getClass() + "isn't supported as " +
                    "a parameter type (only String and QName are).");
            root.addChild(omelmt);
        }
        return root;
    }

}
