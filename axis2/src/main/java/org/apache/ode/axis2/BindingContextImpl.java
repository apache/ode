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

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.apache.ode.axis2.hooks.ODEMessageReceiver;
import org.apache.ode.axis2.httpbinding.HttpExternalService;
import org.apache.ode.axis2.soapbinding.SoapExternalService;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.utils.wsdl.WsdlUtils;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * AXIS2 implementation of the {@link org.apache.ode.bpel.iapi.BindingContext}
 * interface. Deals with the activation of endpoints.
 *
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class BindingContextImpl implements BindingContext {
    protected final Log __log = LogFactory.getLog(getClass());

    private ODEServer _server;
    private MultiKeyMap _services = new MultiKeyMap();

    public BindingContextImpl(ODEServer server) {
        _server = server;
    }

    public EndpointReference activateMyRoleEndpoint(QName processId, Endpoint myRoleEndpoint) {
        try {
            ProcessConf pconf = _server._store.getProcessConfiguration(processId);
            Definition wsdl = pconf.getDefinitionForService(myRoleEndpoint.serviceName);
            if (wsdl == null)
                throw new ContextException("Unable to access WSDL definition to activate MyRole endpoint for service " + myRoleEndpoint.serviceName
                        + " and port " + myRoleEndpoint.portName);
            ODEService svc = createService(pconf, myRoleEndpoint.serviceName, myRoleEndpoint.portName);
            return svc.getMyServiceRef();
        } catch (AxisFault axisFault) {
            throw new ContextException("Could not activate endpoint for service " + myRoleEndpoint.serviceName
                    + " and port " + myRoleEndpoint.portName, axisFault);
        }
    }

    public void deactivateMyRoleEndpoint(Endpoint myRoleEndpoint) {
        destroyService(myRoleEndpoint.serviceName, myRoleEndpoint.portName);
    }

    public PartnerRoleChannel createPartnerRoleChannel(QName processId, PortType portType,
                                                       Endpoint initialPartnerEndpoint) {
        // NOTE: This implementation assumes that the initial value of the
        // partner role determines the binding.
        ProcessConf pconf = _server._store.getProcessConfiguration(processId);
        Definition wsdl = pconf.getDefinitionForService(initialPartnerEndpoint.serviceName);
        if (wsdl == null) {
            throw new ContextException("Cannot find definition for service " + initialPartnerEndpoint.serviceName
                    + " in the context of process " + processId);
        }
        return createExternalService(pconf, initialPartnerEndpoint.serviceName, initialPartnerEndpoint.portName);
    }

    protected ODEService createService(ProcessConf pconf, QName serviceName, String portName) throws AxisFault {
        AxisService axisService = ODEAxisService.createService(_server._axisConfig, pconf, serviceName, portName);
        ODEService odeService = new ODEService(axisService, pconf, serviceName, portName, _server._bpelServer);

        destroyService(serviceName, portName);
        _services.put(serviceName, portName, odeService);

        // Setting our new service on the ODE receiver
        Iterator operationIterator = axisService.getOperations();
        while (operationIterator.hasNext()) {
            AxisOperation op = (AxisOperation) operationIterator.next();
            if (op.getMessageReceiver() instanceof ODEMessageReceiver) {
                ((ODEMessageReceiver) op.getMessageReceiver()).setService(odeService);
                break;
            }
        }

        // We're public!
        _server._axisConfig.addService(axisService);
        __log.debug("Created Axis2 service " + serviceName);
        return odeService;
    }


    protected void destroyService(QName serviceName, String portName) {
        __log.debug("Destroying service " + serviceName + " port " + portName);
        ODEService service = (ODEService) _services.remove(serviceName, portName);
        if (service != null) {
            // try to clean up the service after itself
            try {
                String axisServiceName = service.getAxisService().getName();
                AxisService axisService = _server._axisConfig.getService(axisServiceName);
                // first, de-allocate its schemas
                axisService.releaseSchemaList();
                // then, de-allocate its parameters
                // the service's wsdl object model is stored as one of its parameters!
                // can't stress strongly enough how important it is to clean this up.
                ArrayList<Parameter> parameters = (ArrayList<Parameter>) axisService.getParameters();
                for (Parameter parameter : parameters) {
                    axisService.removeParameter(parameter);
                }
                // now, stop the service
                _server._axisConfig.stopService(axisServiceName);
                // if only this method did a good job of cleaning up after itself
                _server._axisConfig.removeService(axisServiceName);
                _server._axisConfig.cleanup();
            } catch (AxisFault axisFault) {
                __log.error("Couldn't destroy service " + serviceName);
            }
        } else {
            __log.debug("Couldn't find service " + serviceName + " port " + portName + " to destroy.");
        }
    }

    protected ExternalService createExternalService(ProcessConf pconf, QName serviceName, String portName) throws ContextException {
        ExternalService extService = null;

        Definition def = pconf.getDefinitionForService(serviceName);
        try {
            if (WsdlUtils.useHTTPBinding(def, serviceName, portName)) {
                if (__log.isDebugEnabled()) __log.debug("Creating HTTP-bound external service " + serviceName);
                extService = new HttpExternalService(pconf, serviceName, portName, _server._bpelServer, _server.httpConnectionManager);
            } else if (WsdlUtils.useSOAPBinding(def, serviceName, portName)) {
                if (__log.isDebugEnabled()) __log.debug("Creating SOAP-bound external service " + serviceName);
                extService = new SoapExternalService(def, serviceName, portName, _server._axisConfig, pconf, _server.httpConnectionManager);
            }
        } catch (Exception ex) {
            __log.error("Could not create external service.", ex);
            throw new ContextException("Error creating external service! name:" + serviceName + ", port:" + portName, ex);
        }

        // if not SOAP nor HTTP binding
        if (extService == null) {
            throw new ContextException("Only SOAP and HTTP binding supported!");
        }

        __log.debug("Created external service " + serviceName);
        return extService;
    }

}
