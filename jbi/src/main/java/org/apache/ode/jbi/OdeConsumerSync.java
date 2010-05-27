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

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;

/**
 * Completes {@link OdeConsumer} implementation using the synchronous invocation style (sendSync).
 *
 * @author Maciej Szefler
 *
 */
class OdeConsumerSync extends OdeConsumer {
    private static final Log __log = LogFactory.getLog(OdeConsumerSync.class);


    OdeConsumerSync(OdeContext ode) {
        super(ode);
    }


    protected void doSendTwoWay(final PartnerRoleMessageExchange odeMex, final InOut inout) {
        _ode._executorService.submit(new Runnable() {
            public void run() {
                try {
                    _outstandingExchanges.put(inout.getExchangeId(), odeMex);
                    boolean sendOk = _ode.getChannel().sendSync(inout, _responseTimeout);
                    if (!sendOk) {
                        __log.warn("Timeout while sending message for JBI message exchange: " + inout.getExchangeId());
                    }
                    onJbiMessageExchange(inout);
                } catch (MessagingException e) {
                    String errmsg = "Error sending request-response message to JBI for ODE mex " + odeMex;
                    __log.error(errmsg, e);
                }
            }
        });
    }

    protected void doSendOneWay(final PartnerRoleMessageExchange odeMex, final InOnly inonly) {
        _ode._executorService.submit(new Runnable() {
            public void run() {
                try {
                    boolean sendOk = _ode.getChannel().sendSync(inonly, _responseTimeout);
                    if (!sendOk) {
                        __log.warn("Timeout while sending message for JBI message exchange: " + inonly.getExchangeId());
                    }
                    onJbiMessageExchange(inonly);
                } catch (MessagingException e) {
                    String errmsg = "Error sending request-only message to JBI for ODE mex " + odeMex;
                    __log.error(errmsg, e);
                }
            }
        });
    }

    protected void inOutDone(InOut inout) {
        // nothing
    }
}
