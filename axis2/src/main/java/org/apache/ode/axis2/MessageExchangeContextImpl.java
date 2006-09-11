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

package org.apache.ode.axis2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;

/**
 * Implementation of the ODE {@link org.apache.ode.bpel.iapi.MessageExchangeContext}
 * interface. This class is used by the ODE engine to make invocation of external
 * services using Axis.
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {

    private static final Log __log = LogFactory.getLog(MessageExchangeContextImpl.class);

    private ODEServer _server;

    public MessageExchangeContextImpl(ODEServer server) {
        _server = server;
    }

    public void invokePartner(PartnerRoleMessageExchange partnerRoleMessageExchange) throws ContextException {
        if (__log.isDebugEnabled())
            __log.debug("Invoking a partner operation: " + partnerRoleMessageExchange.getOperationName());

//        EndpointReference epr = partnerRoleMessageExchange.getEndpointReference();
//        // We only invoke with WSA endpoints, that makes our life easier
//        if (!(epr instanceof WSAEndpoint))
//            epr = EndpointFactory.convert(new QName(Namespaces.WS_ADDRESSING_NS, "EndpointReference"),
//                    epr.toXML().getDocumentElement());
//        // It's now safe to cast
//        QName serviceName = ((WSAEndpoint)epr).getServiceName();
//        String portName = ((WSAEndpoint)epr).getPortName();
//        if (__log.isDebugEnabled())
//            __log.debug("The service to invoke is the external service " + serviceName);
//        ExternalService service = _server.getExternalService(serviceName, portName);

        ExternalService service = (ExternalService) partnerRoleMessageExchange.getChannel();
        service.invoke(partnerRoleMessageExchange);
    }

    public void onAsyncReply(MyRoleMessageExchange myRoleMessageExchange) throws BpelEngineException {
        if (__log.isDebugEnabled())
            __log.debug("Processing an async reply from service " + myRoleMessageExchange.getServiceName());

        // TODO Add a port in MessageExchange (for now there's only service) to be able to find the
        // TODO right service. For now we'll just lookup by service+portType but if we have severalt ports
        // TODO for the same portType that will not work.
        ODEService service = _server.getService(myRoleMessageExchange.getServiceName(),
                myRoleMessageExchange.getPortType().getQName());
        service.notifyResponse(myRoleMessageExchange);
    }
}
