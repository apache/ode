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

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessStore;

/**
 * AXIS2 implementation of the {@link org.apache.ode.bpel.iapi.BindingContext}
 * interface. Deals with the activation of endpoints.
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 * 
 */
public class BindingContextImpl implements BindingContext {
    private ODEServer _server;
    private ProcessStore _store;

    public BindingContextImpl(ODEServer server, ProcessStore store) {
        _server = server;
        _store = store;
    }

    public EndpointReference activateMyRoleEndpoint(QName processId, Endpoint myRoleEndpoint) {
        try {
            ProcessConf pconf = _store.getProcessConfiguration(processId); 
        	Definition wsdl = pconf.getDefinitionForService(myRoleEndpoint.serviceName);
        	if (wsdl == null)
        		throw new ContextException("Unable to access WSDL definition to activate MyRole endpoint for service " + myRoleEndpoint.serviceName
        				+ " and port " + myRoleEndpoint.portName);
            ODEService svc = _server.createService(pconf, myRoleEndpoint.serviceName, myRoleEndpoint.portName);
            return svc.getMyServiceRef();
        } catch (AxisFault axisFault) {
            throw new ContextException("Could not activate endpoint for service " + myRoleEndpoint.serviceName
                    + " and port " + myRoleEndpoint.portName, axisFault);
        }
    }

    public void deactivateMyRoleEndpoint(Endpoint myRoleEndpoint) {
        _server.destroyService(myRoleEndpoint.serviceName);
    }

    public PartnerRoleChannel createPartnerRoleChannel(QName processId, PortType portType,
                                                       Endpoint initialPartnerEndpoint) {
        // NOTE: This implementation assumes that the initial value of the
        // partner role determines the binding.
        return _server.createExternalService(_store
                .getProcessConfiguration(processId).getDefinitionForService(initialPartnerEndpoint.serviceName),
                initialPartnerEndpoint.serviceName, initialPartnerEndpoint.portName);
    }

}
