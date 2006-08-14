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
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

public class InstanceManagementTest extends TestCase {

  public void testListProcesses() throws Exception {
    OMElement root = buildMessage("listProcesses", new String[] {"filter", "orderKeys"}, new String[] {"", ""});
    OMElement result = sendToPM(root);
    // TODO update to deploy a known process first, to test whether it's there
    System.out.println(result);
  }

  public void testListAllProcesses() throws Exception {
    OMElement root = buildMessage("listAllProcesses", new String[] {}, new String[] {});
    OMElement result = sendToPM(root);
    // TODO update to deploy a known process first, to test whether it's there
    System.out.println(result);
  }

  private OMElement buildMessage(String operation, String[] params, String[] values) {
    //create a factory
    OMFactory factory = OMAbstractFactory.getOMFactory();

    //use the factory to create three elements
    OMNamespace pmns = factory.createOMNamespace("http://www.apache.org/ode/pmapi","pmapi");
    OMElement root = factory.createOMElement(operation, pmns);
    for (int m = 0; m < params.length; m++) {
      OMElement omelmt = factory.createOMElement(params[m], null);
      omelmt.setText(values[m]);
      root.addChild(omelmt);
    }
    return root;
  }

  private OMElement sendToPM(OMElement msg) throws AxisFault {
    Options options = new Options();
    EndpointReference target = new EndpointReference("http://localhost:8080/ode/services/ProcessManagement");
    options.setTo(target);

    ServiceClient serviceClient = new ServiceClient();
    serviceClient.setOptions(options);

    return serviceClient.sendReceive(msg);
  }
}
