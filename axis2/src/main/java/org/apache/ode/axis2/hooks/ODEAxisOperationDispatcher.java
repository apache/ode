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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * Dispatches the service based on the information from the target endpoint URL.
 */
public class ODEAxisOperationDispatcher extends AbstractDispatcher {

    private static MultiKeyMap _elmtToOperation = new MultiKeyMap();

    /** Field NAME */
    public static final String NAME = "ODEAxisOperationDispatcher";
    private static final Log log = LogFactory.getLog(ODEAxisOperationDispatcher.class);
    QName operationName = null;

    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {
        AxisOperation operation;

        // Start with the wsaAction. We assume wsaAction is the more reliable
        // way to identify the operation.
        String action = messageContext.getWSAAction();
        if (action != null) {
            if (log.isDebugEnabled()) {
                log.debug(Messages.getMessage("checkingoperation", action));
            }
            operation = service.getOperationByAction(action);
            if (operation != null)
                return operation;
        }

        // Failing that, look at the body of the SOAP message. We expect one
        // element that has the same (local) name as the operation. This works
        // well for RPC, not always for Doc/Lit.
        OMElement bodyFirstChild = messageContext.getEnvelope().getBody().getFirstElement();
        if (bodyFirstChild != null) {
            String localName = bodyFirstChild.getLocalName();
            if (log.isDebugEnabled()) {
                log.debug("Checking for Operation using SOAP message body's first child's local name : "
                        + localName);
            }
                
            operation = service.getOperation(new QName(localName));
            if (operation != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found operation " + operation);
                }
                return operation;
            }

            // Of course, the element name most likely uses the suffix
            // Request or Response, so look for those and strip them.
            int index = localName.lastIndexOf("Request");
            if (index >=0 && index + "Request".length() == localName.length()) {
                AxisOperation op = service.getOperation(new QName(localName.substring(0, index)));
                if (op != null) return op;
            }
            index = localName.lastIndexOf("Response");
            if (index >=0 && index + "Response".length() == localName.length()) {
                AxisOperation op = service.getOperation(new QName(localName.substring(0, index)));
                if (op != null) return op;
            }

            // Seems the operation still couldn't be found, let's check our operation => element
            // mapping if we can find something (useful for doc/lit when people have the bad idea
            // of using a different name for their operation and part element)
            String opName = (String) _elmtToOperation.get(service.getName(), localName);
            if (opName != null) {
                operation = service.getOperation(new QName(opName));
                return operation;
            }
        }
        log.warn("No operation has been found!");
        return null;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        // #ODEAxisServiceDispatcher will do that
        return null;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }

    /**
     * Associates an operation and the corresponding message part element name. Only
     * makes sense for doc/lit services (only one part) for which the operation can't
     * easily be guessed from the message element name.
     * @param axisServiceName the service name as registered in Axis2
     * @param operationName operation local name
     * @param elmtName element local name
     */
    public static void addElmtToOpMapping(String axisServiceName, String operationName, String elmtName) {
        if (operationName.equals(elmtName)) return;
        _elmtToOperation.put(axisServiceName, elmtName, operationName);
    }

}