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

package org.apache.ode.jbi;

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
 * Implementation of the ODE {@link org.apache.ode.bpel.iapi.MessageExchangeContext} interface. This class is used by the ODE engine
 * to make invocation on JBI services provided by other engines (i.e. the BPEL engine is acting as client/consumer of services).
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {

    private static final Log __log = LogFactory.getLog(MessageExchangeContextImpl.class);

    private OdeContext _ode;

    /** Supported invocation styles. For now this is fixed. */
    private static final Set<InvocationStyle> __supported;
    static {
        HashSet<InvocationStyle> supported = new HashSet<InvocationStyle>();
        supported.add(InvocationStyle.UNRELIABLE);
        supported.add(InvocationStyle.TRANSACTED);
        __supported = Collections.unmodifiableSet(supported);
    }

    public MessageExchangeContextImpl(OdeContext ode) {
        _ode = ode;
    }


    public void invokePartnerUnreliable(PartnerRoleMessageExchange mex) throws ContextException {
        _ode._consumer.invokePartner(mex);
    }

    public void invokePartnerReliable(PartnerRoleMessageExchange mex) throws ContextException {
        throw new ContextException("Unsupported.");

    }

    public void invokePartnerTransacted(PartnerRoleMessageExchange mex) throws ContextException {
        throw new ContextException("Unsupported.");

    }


    public void cancel(PartnerRoleMessageExchange mex) throws ContextException {
        // What can we do in JBI to cancel? --- not much. 
        
    }

    public Set<InvocationStyle> getSupportedInvocationStyle(PartnerRoleChannel prc, EndpointReference partnerEpr) {
        return __supported ;
    }


    public void onMyRoleMessageExchangeStateChanged(MyRoleMessageExchange myRoleMex) throws BpelEngineException {
    }
    

    
}
