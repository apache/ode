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

package org.apache.ode.axis2.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.ode.utils.Namespaces;

import javax.xml.namespace.QName;

/**
 * Client utilities that can be used to invoke easily the deployment
 * and management services with Axis2.
 */
public class ServiceClientUtil {

    /**
     * Sends the provided message to an Axis2 deployed service.
     * @param msg the message OMElement that will be included in the body
     * @param url to send the message to
     * @return the response message
     * @throws AxisFault when a problem occured during the call
     */
    public OMElement send(OMElement msg, String url) throws AxisFault {
        return send(msg, url, Options.DEFAULT_TIMEOUT_MILLISECONDS);
    }

    /**
     * Sends the provided message to an Axis2 deployed service allowing to set a
     * specific timeout (in ms).
     * @param msg the message OMElement that will be included in the body
     * @param url to send the message to
     * @param timeout in milliseconds
     * @return the response message
     * @throws AxisFault when a problem occured during the call
     */
    public OMElement send(OMElement msg, String url, long timeout) throws AxisFault {
        Options options = new Options();
        EndpointReference target = new EndpointReference(url);
        options.setTo(target);
        options.setTimeOutInMilliSeconds(timeout);

        ServiceClient serviceClient = new ServiceClient();
        try {
            serviceClient.setOptions(options);
            
            OMElement response = serviceClient.sendReceive(msg);
            // build response to materialize lazy information
            response.build();
            return response;
        } finally {
            serviceClient.cleanupTransport();
        }
    }

    /**
     * Builds a message for the deployment and management API using simple parameter
     * passing. Example: <br/>
     * <code>
     * buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
     *          new String[] {"name=DynPartnerResponder namespace=http://ode/bpel/responder " +
     *                  "deployed>=" + notSoLongAgoStr, ""});
     * </code>
     * @param operation to call
     * @param params list of the parameters for the operation as defined in the WSDL document
     * @param values of the parameters
     * @return the message to send
     */
    public OMElement buildMessage(String operation, String[] params, Object[] values) {
        OMFactory _factory = OMAbstractFactory.getOMFactory();
        OMNamespace pmns = _factory.createOMNamespace(Namespaces.ODE_PMAPI_NS, "pmapi");
        OMElement root = _factory.createOMElement(operation, pmns);
        for (int m = 0; m < params.length; m++) {
            OMElement omelmt = _factory.createOMElement(params[m], null);
            if (values[m] == null)
                omelmt.setText("");
            else if (values[m] instanceof String)
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
