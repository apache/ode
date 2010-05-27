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


import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;

/**
 * Asynchronous JBI service consumer
 */
class OdeConsumerAsync extends OdeConsumer {
    private static final Log __log = LogFactory.getLog(OdeConsumerAsync.class);

    /**
     * We create an executor to handle all the asynchronous invocations/timeouts. Note, we don't need a lot of threads
     * here, the operations are all async, using single-thread executor avoids any possible problems in concurrent
     * use of delivery channel.
     *
     * WARNING:  Canceling tasks does not immediately release them, so we don't use the schedule-cancel pattern here.
     */
    private ScheduledExecutorService _executor;

    private Map<String, Long> _mexTimeouts = new ConcurrentHashMap<String, Long>();

    OdeConsumerAsync(OdeContext ode) {
        super(ode);
       _executor = Executors.newSingleThreadScheduledExecutor();
        _executor.scheduleWithFixedDelay(new MEXReaper(), _responseTimeout, _responseTimeout/10, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doSendOneWay(final PartnerRoleMessageExchange odeMex, final InOnly inonly) {
        _executor.submit(new Runnable() {
            public void run() {
                try {
                    _outstandingExchanges.put(inonly.getExchangeId(), odeMex);
                    _ode.getChannel().send(inonly);
                } catch (MessagingException e) {
                    String errmsg = "Error sending request-only message to JBI for ODE mex " + odeMex;
                    __log.error(errmsg, e);
                }
            }
        });

    }

    @Override
    protected void doSendTwoWay(final PartnerRoleMessageExchange odeMex, final InOut inout) {
        _executor.submit(new Runnable() {
            public void run() {
                try {
                    _outstandingExchanges.put(inout.getExchangeId(), odeMex);
                    _mexTimeouts.put(inout.getExchangeId(), System.currentTimeMillis()+_responseTimeout);
                    _ode.getChannel().send(inout);
                } catch (MessagingException e) {
                    String errmsg = "Error sending request-only message to JBI for ODE mex " + odeMex;
                    __log.error(errmsg, e);
                }
            }
        });

    }

    protected void inOutDone(InOut inout) {
        _mexTimeouts.remove(inout.getExchangeId());
    }

    private class MEXReaper implements Runnable {
        public void run() {
            long now = System.currentTimeMillis();
            Object[] inouts = _mexTimeouts.keySet().toArray();
            for (int i=0; i<inouts.length; i++) {
                long timeout = _mexTimeouts.get(inouts[i]);
                if (now >= timeout) {
                    _mexTimeouts.remove(inouts[i]);
                    final PartnerRoleMessageExchange pmex = _outstandingExchanges.remove(inouts[i]);

                    if (pmex == null) /* no worries, got a response. */
                        continue;

                    __log.warn("Timeout on JBI message exchange " + inouts[i]);
                    try {
                        _ode._scheduler.execIsolatedTransaction(new Callable<Void>() {
                            public Void call() throws Exception {
                                pmex.replyWithFailure(FailureType.NO_RESPONSE, "Response not received after " + _responseTimeout + "ms.", null);
                                return null;
                            }
                        });
                    } catch (Exception ex) {
                        __log.error("Error executing transaction:  ", ex);
                    }
                }
            }
        }
    }
}
