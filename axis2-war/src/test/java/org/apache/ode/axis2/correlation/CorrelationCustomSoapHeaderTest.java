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

package org.apache.ode.axis2.correlation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisService;
import org.apache.ode.axis2.Axis2TestBase;
import org.apache.ode.bpel.pmapi.InstanceInfoListDocument;
import org.apache.ode.bpel.pmapi.ProcessInfoDocument;
import org.apache.ode.bpel.pmapi.TInstanceInfo;
import org.apache.ode.bpel.pmapi.TInstanceInfoList;
import org.apache.ode.utils.DOMUtils;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class CorrelationCustomSoapHeaderTest extends Axis2TestBase {
    @Test(dataProvider="configs")
    public void testCorrelationWithCustomSoapHeaders() throws Exception{
        server.undeployProcess("TestCorrelationCustomSoapHeader");
        if (!server.isDeployed("TestCorrelationCustomSoapHeader")) 
            server.deployProcess("TestCorrelationCustomSoapHeader");

        String response1 = sendRequestFile("http://localhost:"+getTestPort(0)+"/processes/Correlation-Header/wsdlWithHeader/Process/initiator",
                "TestCorrelationCustomSoapHeader", "firstRequest.soap");

        //Take out the IID from the response which is used as the correlation value
        Element rootElemt = DOMUtils.stringToDOM(response1);
        Element soapBody = DOMUtils.getFirstChildElement(rootElemt);
        assertEquals("Body", soapBody.getLocalName());

        Element responseElem = DOMUtils.getFirstChildElement(soapBody);
        assertEquals("Recevie_first_messageResponse", responseElem.getLocalName());

        InstanceInfoListDocument infoListDoc = server.getODEServer().getInstanceManagement().listInstances("name=Process namespace=http://example.com/wsdlWithHeader/Process status=active", "", 1);
        TInstanceInfoList infoList = infoListDoc.getInstanceInfoList();
        TInstanceInfo[] infoListArr = infoList.getInstanceInfoArray();

        assertNotNull(infoListArr);
        assertNotNull(infoListArr[0]);

        Long iid = null;

        TInstanceInfo object = infoListArr[0];
        //instance has to be in active status
        assertEquals("ACTIVE", object.getStatus().toString());
        iid = new Long(object.getIid());

        //send second request
        String response2 = sendRequestFile("http://localhost:"+getTestPort(0)+"/processes/correlationWithHeaders", "TestCorrelationCustomSoapHeader", "secondRequest.soap");

        //instance should have completed now
        String iidStatus = server.getODEServer().getInstanceManagement().getInstanceInfo(iid).getInstanceInfo().getStatus().toString();
        assertEquals("COMPLETED", iidStatus);

        server.undeployProcess("TestCorrelationCustomSoapHeader");
    }

}
