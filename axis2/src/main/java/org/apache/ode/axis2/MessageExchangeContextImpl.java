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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;

/**
 * Implementation of the ODE {@link org.apache.ode.bpel.iapi.MessageExchangeContext}
 * interface. This class is used by the ODE engine to make invocation of external
 * services using Axis.
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {

    private static final Log __log = LogFactory.getLog(MessageExchangeContextImpl.class);
    
    /** The currently supported invocation styles. */
    private static final Set<InvocationStyle> __supportedInvocationStyles;
    
    static {
        HashSet<InvocationStyle> styles = new HashSet<InvocationStyle>();
        styles.add(InvocationStyle.ASYNC);
        styles.add(InvocationStyle.BLOCKING);
        __supportedInvocationStyles = Collections.unmodifiableSet(styles);
    }
    
    public MessageExchangeContextImpl(ODEServer server) {
    }


    public void invokePartnerAsynch(PartnerRoleMessageExchange mex) throws ContextException {
        throw new UnsupportedOperationException();
    }

    public void invokePartnerBlocking(PartnerRoleMessageExchange partnerRoleMessageExchange) throws ContextException {
        if (__log.isDebugEnabled())
            __log.debug("Invoking a partner operation: " + partnerRoleMessageExchange.getOperationName());

        ExternalService service = (ExternalService)partnerRoleMessageExchange.getPartnerRoleChannel();
        if (__log.isDebugEnabled())
            __log.debug("The service to invoke is the external service " + service);
        service.invoke(partnerRoleMessageExchange);
        
    }

    public void invokePartnerReliable(PartnerRoleMessageExchange mex) throws ContextException {
        throw new UnsupportedOperationException();
    }

    public void invokePartnerTransacted(PartnerRoleMessageExchange mex) throws ContextException {
        throw new UnsupportedOperationException();        
    }

    

    public void onAsyncReply(MyRoleMessageExchange myRoleMessageExchange) throws BpelEngineException {
        if (__log.isDebugEnabled())
            __log.debug("Processing an async reply from service " + myRoleMessageExchange.getServiceName());

        // Nothing to do, no callback is necessary, the client just synchornizes itself with the
        // mex reply when invoking the engine.
    }

    public void onReliableReply(MyRoleMessageExchange myRoleMex) throws BpelEngineException {
        __log.error("RELIABLE reply from service " + myRoleMex.getServiceName() +"; RELIABLE IS NOT SUPPORTED!");

        // We don't support this yet, so not much to do here. 
    }


    public void cancel(PartnerRoleMessageExchange mex) throws ContextException {
        // TODO Auto-generated method stub
        
    }


    public Set<InvocationStyle> getSupportedInvocationStyle(PartnerRoleChannel prc, EndpointReference partnerEpr) {
        return __supportedInvocationStyles;
    }

}
