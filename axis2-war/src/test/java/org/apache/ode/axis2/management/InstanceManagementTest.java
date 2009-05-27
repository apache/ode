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
import org.apache.ode.tools.sendsoap.cline.HttpSoapSender;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.il.OMUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.testng.AssertJUnit.assertTrue;

public class InstanceManagementTest extends Axis2TestBase {

    private OMFactory _factory;
    private DateFormat xsdDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    private ServiceClientUtil _client;
    private String _deployedName;

    private static int instanceNb(OMElement response){
        return instances(response).size();
    }

    private static List<OMElement> instances(OMElement response){
        return IteratorUtils.toList(OMUtils.getFirstChildWithName(response, "instance-info-list").getChildrenWithName(new QName(Namespaces.ODE_PMAPI_TYPES_NS, "instance-info")));
    }

    private static OMElement instance(OMElement response){
        Iterator it = response.getChildrenWithName(new QName("", "instance-info"));
        return it.hasNext()? (OMElement) it.next(): null;
    }


  @Test
    public void testListInstances() throws Exception {
        OMElement listRoot = _client.buildMessage("listInstances", new String[] {"filter", "order", "limit"},
                new String[] {"name=DynPartnerMain", "", "10"});
        OMElement result = sendToIM(listRoot);
        assertTrue("Ensures that there's only one process instance", instanceNb(result) ==1);

        // Another query with more options
        Calendar notSoLongAgo = Calendar.getInstance();
        notSoLongAgo.add(Calendar.MINUTE, -2);
        String notSoLongAgoStr = xsdDF.format(notSoLongAgo.getTime());
        listRoot = _client.buildMessage("listInstances", new String[] {"filter", "order", "limit"},
                new String[] {"name=DynPartnerResponder namespace=http://ode/bpel/responder " +
                        "started>=" + notSoLongAgoStr, "", "10"});
        result = sendToIM(listRoot);
        assert(result.toString().split("instance-info").length == 5);
    }

  @Test
    public void testListAllInstances() throws Exception {
        OMElement root = _client.buildMessage("listAllInstancesWithLimit", new String[] {"limit"}, new String[] {"1"});
        OMElement result = sendToIM(root);
        // We shold have only one instance (so 2 opening/closing elmts)
        assert(result.toString().split("instance-info").length == 3);
        // And one of our executed instances are there
        assert(result.toString().indexOf("DynPartnerMain") >= 0 ||
                result.toString().indexOf("DynPartnerResponder") >= 0);
    }

  @Test
    public void testInstanceSummaryListProcess() throws Exception {
        OMElement listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerMain", ""});
        OMElement result = sendToPM(listRoot);
        // Ensures that there's only 2 process-info string (ending and closing tags) and hence only one process
        String ns = "http://www.apache.org/ode/pmapi/types/2006/08/02/";
        Iterator iter = result.getFirstElement().getFirstElement().getFirstChildWithName(new QName(ns, "instance-summary"))
                .getChildrenWithName(new QName(ns, "instances"));
        int count = 0;
        while (iter.hasNext()) {
            OMElement omelmt = (OMElement) iter.next();
            count += Integer.parseInt(omelmt.getAttributeValue(new QName(null, "count")));
        }
        assert(count == 1);
    }

  @Test
    public void testGetInstanceInfo() throws Exception {
        OMElement root = _client.buildMessage("listAllInstances", new String[] {}, new String[] {});
        OMElement result = sendToIM(root);
        String iid = result.getFirstElement().getFirstChildWithName(new QName(Namespaces.ODE_PMAPI_TYPES_NS, "instance-info"))
                .getFirstChildWithName(new QName(Namespaces.ODE_PMAPI_TYPES_NS, "iid")).getText();
        root = _client.buildMessage("getInstanceInfo", new String[] {"iid"}, new String[] {iid});
        result = sendToIM(root);
        assert(result.toString().split("instance-info").length == 3);
    }

  @Test
    public void testGetInstanceInfoFault() throws Exception {
        // Hopefully this id won't exist
        OMElement root = _client.buildMessage("getInstanceInfo", new String[] {"iid"}, new String[] {"65431"});
        try {
            @SuppressWarnings("unused")
            OMElement result = sendToIM(root);
        } catch (AxisFault axisFault) {
            assert(axisFault.getCause().toString().indexOf("InstanceNotFoundException") > 0);
        }
    }

  @Test
    public void testGetScopeInfo() throws Exception {
      OMElement root = _client.buildMessage("listInstances", new String[] {"filter", "order", "limit"},
              new String[] {"name=DynPartnerMain", "", "10"});
        OMElement result = sendToIM(root);
        String siid = result.getFirstElement().getFirstChildWithName(new QName(Namespaces.ODE_PMAPI_TYPES_NS, "instance-info"))
                .getFirstChildWithName(new QName(Namespaces.ODE_PMAPI_TYPES_NS, "root-scope"))
                .getAttributeValue(new QName(null, "siid"));
        root = _client.buildMessage("getScopeInfoWithActivity", new String[] {"siid", "activityInfo"},
                new String[] {siid, "true"});
        result = sendToIM(root);
        assert(result.toString().split("scope-info").length == 3);
        assert(result.toString().indexOf("activity-info") >= 0);
    }

  @Test
    public void testGetVariableInfo() throws Exception {
        OMElement root = _client.buildMessage("listInstances", new String[] {"filter", "order", "limit"},
                new String[] {"name=DynPartnerMain", "", "10"});
        OMElement result = sendToIM(root);
        String siid = result.getFirstElement().getFirstChildWithName(new QName(Namespaces.ODE_PMAPI_TYPES_NS, "instance-info"))
                .getFirstChildWithName(new QName(Namespaces.ODE_PMAPI_TYPES_NS, "root-scope"))
                .getAttributeValue(new QName(null, "siid"));
        root = _client.buildMessage("getVariableInfo", new String[] {"sid", "varName"}, new String[] {siid, "dummy"});
        result = sendToIM(root);
        assert(result.toString().indexOf("fire!") >= 0);
    }


//    TODO uncomment when events querying is fixes on OpenJPA
    
//    public void testListEvents() throws Exception {
//        OMElement root = _client.buildMessage("listEvents", new String[] {"instanceFilter", "eventFilter", "maxCount"},
//                new String[] {"", "", "0"});
//        OMElement result = sendToIM(root);
//        assert(result.toString().split("event-info").length > 10);
//    }
//
//    public void testGetEventTimeline() throws Exception {
//        OMElement root = _client.buildMessage("getEventTimeline", new String[] {"instanceFilter", "eventFilter"},
//                new String[] {"", ""});
//        OMElement result = sendToIM(root);
//        assert(result.toString().split("element").length > 10);
//    }

  @Test
    public void testDeleteInstances() throws Exception {
        OMElement root = _client.buildMessage("listAllInstancesWithLimit", new String[] {"limit"}, new String[] {"1"});
        OMElement result = sendToIM(root);
        String iid = result.getFirstElement().getFirstElement().getText();
        System.out.println("=> " + result.getFirstElement().getFirstElement().getText());
        _client.buildMessage("delete", new String[] {"piid"}, new String[] {iid});
    }

  @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();

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
        OMElement res = sendToDeployment(root);
        _deployedName = res.getFirstChildWithName(new QName(null, "response")).getFirstChildWithName(new QName("http://www.apache.org/ode/deployapi", "name")).getText();

        // Execute
        URL svcUrl = new URL("http://localhost:8888/processes/DynMainService");
        InputStream sis = this.getClass().getClassLoader().getResourceAsStream("testDynPartnerRequest.soap");
        System.out.println(HttpSoapSender.doSend(svcUrl, sis, null, 0, null, null, null));
        // Just making sure the instance starts
        Thread.sleep(1000);
    }

  @AfterMethod
    protected void tearDown() throws Exception {
        // Prepare undeploy message
        OMNamespace depns = _factory.createOMNamespace(Namespaces.ODE_DEPLOYAPI_NS, "deployapi");
        OMElement root = _factory.createOMElement("undeploy", depns);
        OMElement part = _factory.createOMElement("package", null);
        part.setText(_deployedName);
        root.addChild(part);

        // Undeploy
        sendToDeployment(root);

        OMElement listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name=DynPartnerMain", ""});
        OMElement result = sendToPM(listRoot);
        assertTrue("No process expected", result.toString().matches(".*<process-info-list\\s*/>.*"));

        super.tearDown();
    }

    private OMElement sendToPM(OMElement msg) throws AxisFault {
        return _client.send(msg, "http://localhost:8888/processes/ProcessManagement");
    }

    private OMElement sendToIM(OMElement msg) throws AxisFault {
        return _client.send(msg, "http://localhost:8888/processes/InstanceManagement");
    }

    private OMElement sendToDeployment(OMElement msg) throws AxisFault {
        return _client.send(msg, "http://localhost:8888/processes/DeploymentService");
    }

}
