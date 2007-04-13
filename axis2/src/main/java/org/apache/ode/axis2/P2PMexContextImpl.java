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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.Scheduler;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class P2PMexContextImpl implements MessageExchangeContext {
    private static final Log __log = LogFactory.getLog(P2PMexContextImpl.class);

    private MessageExchangeContext _wrapped;

    private ODEServer _server;

    private Scheduler _scheduler;

    private Map<String, PartnerRoleMessageExchange> _waiters = Collections
            .synchronizedMap(new HashMap<String, PartnerRoleMessageExchange>());

    public P2PMexContextImpl(ODEServer server, MessageExchangeContext wrapped, Scheduler scheduler) {
        _server = server;
        _wrapped = wrapped;
        _scheduler = scheduler;
    }

    public void invokePartner(final PartnerRoleMessageExchange pmex) throws ContextException {
        ExternalService target = (ExternalService) pmex.getChannel();
        ODEService myService = _server.getService(target.getServiceName(), target.getPortName());

        // If we have direct access to the other process (i.e. it is locally
        // deployed), then we would like to avoid the
        // whole SOAP step. In this case we do direct invoke.
        if (myService != null) {
            // Defer invoke until tx is comitted.
            _scheduler.registerSynchronizer(new Scheduler.Synchronizer() {
                public void afterCompletion(boolean success) {
                    if (!success) return;
                    try {
                        _scheduler.execIsolatedTransaction(new Callable<Void>() {
                            public Void call() throws Exception {
                                MyRoleMessageExchange mymex = buildAndInvokeMyRoleMex(pmex);
                                mymex.release();
                                return null;
                            }
                        });
                    } catch (Exception ex) {
                        __log.error("Unexpected error", ex);
                        throw new RuntimeException(ex);
                    }
                }

                public void beforeCompletion() {
                }
            });

            if (pmex.getMessageExchangePattern() == MessageExchange.MessageExchangePattern.REQUEST_RESPONSE) {
                _waiters.put(pmex.getMessageExchangeId(),pmex);
                if (__log.isDebugEnabled())
                    __log.debug("registered P2P reply waiter for Partner Mex " + pmex);
            }
            // There is no way we can get a synchronous response.
            pmex.replyAsync();
        } else {
            _wrapped.invokePartner(pmex);
        }
    }

    public void onAsyncReply(MyRoleMessageExchange myRoleMex) throws BpelEngineException {
        if (__log.isDebugEnabled())
            __log.debug("Received Async reply to " + myRoleMex);

        // Note that when we created the MyRoleMessageExchange, we gave the PartnerRoleMex Id
        // as the client id.
        PartnerRoleMessageExchange pmex = myRoleMex.getClientId() == null ? null : _waiters.remove(myRoleMex.getClientId());
        if (pmex == null) {
            if (__log.isDebugEnabled())
                __log.debug("Received Async reply to " + myRoleMex + " is NOT a P2P reply, deferring.");
            _wrapped.onAsyncReply(myRoleMex);
            return;
        }


        if (__log.isDebugEnabled())
            __log.debug("for async reply, found matching P2P Partner Mex " + pmex);

        handleResponse(pmex, myRoleMex);

    }

    private MyRoleMessageExchange buildAndInvokeMyRoleMex(PartnerRoleMessageExchange pmex) {
        ExternalService target = (ExternalService) pmex.getChannel();

        MyRoleMessageExchange odeMex = _server.getBpelServer().getEngine().createMessageExchange(
                pmex.getMessageExchangeId(),
                target.getServiceName(), pmex.getOperationName());

        if(__log.isDebugEnabled())
            __log.debug("Invoking (P2P) service " + odeMex.getServiceName() + " with operation " +
                    odeMex.getOperationName());

        copyHeader(pmex, odeMex);

        odeMex.invoke(pmex.getRequest());

        if (__log.isDebugEnabled())
            __log.debug("Invoked (P2P) service " + odeMex.getServiceName() + " with operation " +
                    odeMex.getOperationName() + "; MyRoleMex status = " + odeMex.getStatus());


        if (odeMex.getStatus() != MessageExchange.Status.ASYNC) {
            if (__log.isDebugEnabled())
                __log.debug("invoke of P2P service did not result in ASYNC state, removing waiter for " + pmex);
            _waiters.remove(pmex.getMessageExchangeId());
            handleResponse(pmex, odeMex);
        }

        return odeMex;
    }

    private void handleResponse(PartnerRoleMessageExchange pmex, MyRoleMessageExchange myRoleMex) {

        switch (myRoleMex.getStatus()) {
        case FAILURE:
            // We can't seem to get the failure out of the myrole mex?
            pmex.replyWithFailure(MessageExchange.FailureType.OTHER, "operation failed", null);
            break;
        case FAULT:
            // note, we are reusing the message object from the my role mex..
            // not quite kosher.
            pmex.replyWithFault(myRoleMex.getFault(), myRoleMex.getFaultResponse());
            break;
        case RESPONSE:
            pmex.reply(myRoleMex.getResponse());
            break;
        default:
            __log.debug("Unexpected state: " + myRoleMex.getStatus());
            break;

        }

    }

    private void copyHeader(MessageExchange source, MessageExchange dest) {
        if (source.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID) != null)
            dest.setProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID, source
                    .getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID));
        if (source.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID) != null)
            dest.setProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID, source
                    .getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID));
    }

}
