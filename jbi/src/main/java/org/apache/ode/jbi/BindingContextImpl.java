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

import java.util.HashMap;
import java.util.Map;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;

/**
 * JBI Implementation of ODE's {@link org.apache.ode.bpel.iapi.BindingContext}
 * interface.
 *
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
class BindingContextImpl implements BindingContext {
    private static final Log __log = LogFactory.getLog(BindingContextImpl.class);

    private final OdeContext _ode;

    BindingContextImpl(OdeContext ode) {
        _ode = ode;
    }

    public EndpointReference activateMyRoleEndpoint(QName processId, Endpoint myRoleEndpoint) {
        try {
            return _ode.activateEndpoint(processId, myRoleEndpoint);
        } catch (Exception ex) {
            throw new ContextException("Could not activate endpoint " + myRoleEndpoint + " for process " + processId,
                    ex);
        }
    }

    public void deactivateMyRoleEndpoint(Endpoint myRoleEndpoint) {
        try {
            _ode.deactivateEndpoint(myRoleEndpoint);
        } catch (Exception ex) {
            String errmsg = "Could not deactivate endpoint: " + myRoleEndpoint;
            __log.error(errmsg, ex);
            throw new ContextException(errmsg, ex);
        }
    }

    public PartnerRoleChannel createPartnerRoleChannel(QName processId, PortType portType,
            Endpoint initialPartnerEndpoint) {
        if (initialPartnerEndpoint != null) {
            JbiEndpointReference jbiEpr = new JbiEndpointReference(initialPartnerEndpoint, _ode);
            return new PartnerRoleChannelImpl(jbiEpr);
        } else {
            return new PartnerRoleChannelImpl();
        }
    }


    private class PartnerRoleChannelImpl implements PartnerRoleChannel {

        private final JbiEndpointReference _initialEPR;

        PartnerRoleChannelImpl(JbiEndpointReference epr) {
            _initialEPR = epr;
        }

        PartnerRoleChannelImpl() {
            _initialEPR = null;
        }

        public EndpointReference getInitialEndpointReference() {
            return _initialEPR;
        }

        public void close() {
            ; // noop
        }

    }


    public long calculateSizeofService(EndpointReference epr) {
        return _ode.calculateSizeOfService(epr);
    }
}
