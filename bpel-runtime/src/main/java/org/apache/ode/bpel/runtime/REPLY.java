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
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.o.OReply;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.runtime.channels.FaultData;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class REPLY extends ACTIVITY {
    private static final long serialVersionUID = 3040651951885161304L;
    private static final Log __log = LogFactory.getLog(REPLY.class);

    REPLY(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
    }

    public void run() {
        final OReply oreply = (OReply)_self.o;

        if (__log.isDebugEnabled()) {
            __log.debug("<reply>  partnerLink=" + oreply.partnerLink + ", operation=" + oreply.operation);
        }
        FaultData fault = null;

        // TODO: Check for fault without message.

        try {
            sendVariableReadEvent(_scopeFrame.resolve(oreply.variable));
            Node msg = fetchVariableData(_scopeFrame.resolve(oreply.variable), false);

            assert msg instanceof Element;

            for (Iterator<OScope.CorrelationSet> i = oreply.initCorrelations.iterator(); i.hasNext(); ) {
                OScope.CorrelationSet cset = i.next();
                initializeCorrelation(_scopeFrame.resolve(cset),
                        _scopeFrame.resolve(oreply.variable));
            }
            for (OScope.CorrelationSet aJoinCorrelation : oreply.joinCorrelations) {
            	// will be ignored if already initialized
                initializeCorrelation(_scopeFrame.resolve(aJoinCorrelation), _scopeFrame.resolve(oreply.variable));
            }

            //		send reply
            getBpelRuntimeContext()
                    .reply(_scopeFrame.resolve(oreply.partnerLink), oreply.operation.getName(),
                            oreply.messageExchangeId, (Element)msg,
                            (oreply.fault != null) ? oreply.fault : null);
        } catch (FaultException e) {
            __log.error(e);
            fault = createFault(e.getQName(), oreply);
        }

        _self.parent.completed(fault, CompensationHandler.emptySet());
    }
}
