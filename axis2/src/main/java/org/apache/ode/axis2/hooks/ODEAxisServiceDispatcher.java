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

package org.apache.ode.axis2.hooks;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.PolicyUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.rampart.RampartMessageData;

/**
 * Dispatches the service based on the information from the target endpoint URL.
 */
public class ODEAxisServiceDispatcher extends AbstractDispatcher {

    /** Field NAME */
    public static final String NAME = "ODEAxisServiceDispatcher";
    private static final Log log = LogFactory.getLog(ODEAxisServiceDispatcher.class);
    QName operationName = null;

    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {
        // #ODEAxisOperationDispatcher will do that
        return null;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();

        if (toEPR != null) {
            if (log.isDebugEnabled()) {
                log.debug("Checking for Service using target endpoint address : " + toEPR.getAddress());
            }

            // The only thing we understand if a service name that
            // follows /processes/ in the request URL.
            String path = parseRequestURLForService(toEPR.getAddress());
            if (path != null) {
                AxisConfiguration registry =
                        messageContext.getConfigurationContext().getAxisConfiguration();
                AxisService service = registry.getService(path);
                if (service!=null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found service in registry from name " + path + ": " + service);
                    }
                    // Axis2 >1.3 is less clever than 1.3. See ODE-509
                    // We have to do additional work for him.
                    // TODO: Check if there is a better workaround possible.
                    Policy policy = PolicyUtil.getMergedPolicy(new ArrayList<PolicyComponent>(service.getPolicySubject().getAttachedPolicyComponents()), service);
                    if (policy != null) {
                        if (log.isDebugEnabled()) log.debug("Apply policy: " + policy.getName());
                        messageContext.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
                    }
                    return service;
                }
            }
        }
        log.warn("No service has been found!");
        return null;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }

    /**
     * Obtain the service name from the request URL. The request URL is
     * expected to use the path "/processes/" under which all processes
     * and their services are listed. Returns null if the path does not
     * contain this part.
     */
    protected String parseRequestURLForService(String path) {
        int index = path.indexOf("/processes/");
        if (-1 != index) {
            String service;

            int serviceStart = index + "/processes/".length();
            if (path.length() > serviceStart + 1) {
                service = path.substring(serviceStart);
                // Path may contain query string, not interesting for us.
                int queryIndex = service.indexOf('?');
                if (queryIndex > 0) {
                    service = service.substring(0, queryIndex);
                }
                return service;
            }
        }
        return null;
    }

}
