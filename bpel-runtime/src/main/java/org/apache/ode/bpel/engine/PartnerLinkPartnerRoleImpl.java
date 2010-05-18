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
package org.apache.ode.bpel.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.o.OPartnerLink;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
class PartnerLinkPartnerRoleImpl extends PartnerLinkRoleImpl {
    static final Log __log = LogFactory.getLog(BpelProcess.class);

    Endpoint _initialPartner;

    public PartnerRoleChannel _channel;
    
    public boolean usePeer2Peer = true;

    PartnerLinkPartnerRoleImpl(BpelProcess process, OPartnerLink plink, Endpoint initialPartner) {
        super(process, plink);
        _initialPartner = initialPartner;
    }

    public void processPartnerResponse(PartnerRoleMessageExchangeImpl messageExchange) {
        if (__log.isDebugEnabled()) {
            __log.debug("Processing partner's response for partnerLink: " + messageExchange);
        }

        BpelRuntimeContextImpl processInstance =
                _process.createRuntimeContext(messageExchange.getDAO().getInstance(), null, null);
        processInstance.invocationResponse(messageExchange);
        processInstance.execute();
    }

}
