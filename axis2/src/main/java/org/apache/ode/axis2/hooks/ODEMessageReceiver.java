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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.ODEService;

/**
 * Receives messages forwarded by Axis.
 */
public class ODEMessageReceiver extends AbstractMessageReceiver {

    private static final Log __log = LogFactory.getLog(ODEMessageReceiver.class);

    private ODEService _service;

    public final void invokeBusinessLogic(final MessageContext msgContext) throws AxisFault {
        if (hasResponse(msgContext.getAxisOperation())) {
            if (__log.isDebugEnabled())
                __log.debug("Received request message for " + msgContext.getAxisService().getName() + "."
                        + msgContext.getAxisOperation().getName());
            // Client is expecting a response, running in the same thread
            MessageContext outMsgContext = MessageContextBuilder.createOutMessageContext(msgContext);
            // pass on the endpoint properties for output context
            outMsgContext.getOptions().setParent(_service.getOptions());
            outMsgContext.getOperationContext().addMessageContext(outMsgContext);
            invokeBusinessLogic(msgContext, outMsgContext);
            if (__log.isDebugEnabled()) {
                __log.debug("Reply for " + msgContext.getAxisService().getName() + "."
                        + msgContext.getAxisOperation().getName());
                __log.debug("Reply message " + outMsgContext.getEnvelope());
            }
            AxisEngine.send(outMsgContext);
        } else {
            if (__log.isDebugEnabled())
                __log.debug("Received one-way message for " + msgContext.getAxisService().getName() + "."
                        + msgContext.getAxisOperation().getName());
            invokeBusinessLogic(msgContext, null);
        }
    }

    private void invokeBusinessLogic(MessageContext msgContext, MessageContext outMsgContext)
            throws AxisFault {
        _service.onAxisMessageExchange(msgContext, outMsgContext, getSOAPFactory(msgContext));

    }

    public void setService(ODEService service) {
        _service = service;
    }

    private boolean hasResponse(AxisOperation op) {
        switch (op.getAxisSpecificMEPConstant()) {
            case WSDLConstants.MEP_CONSTANT_IN_OUT:
                return true;
            case WSDLConstants.MEP_CONSTANT_OUT_ONLY:
                return true;
            case WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN:
                return true;
            case WSDLConstants.MEP_CONSTANT_ROBUST_OUT_ONLY:
                return true;
            default:
                return false;
        }
    }
}
