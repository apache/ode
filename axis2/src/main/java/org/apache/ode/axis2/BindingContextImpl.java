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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.agents.memory.SizingAgent;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.apache.ode.axis2.hooks.ODEMessageReceiver;
import org.apache.ode.axis2.httpbinding.HttpExternalService;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.utils.wsdl.WsdlUtils;

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
    private Map<ODEService, EndpointReference> _serviceEprMap = new HashMap<ODEService, EndpointReference>();

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
            EndpointReference epr = svc.getMyServiceRef();
            _serviceEprMap.put(svc, epr);
            return epr;
        } catch (AxisFault axisFault) {
            throw new ContextException("Could not activate endpoint for service " + myRoleEndpoint.serviceName
                    + " and port " + myRoleEndpoint.portName, axisFault);
        }
    }

    public void deactivateMyRoleEndpoint(Endpoint myRoleEndpoint) {
        ODEService service = destroyService(myRoleEndpoint.serviceName, myRoleEndpoint.portName);
        if (service != null) {
            _serviceEprMap.remove(service);
        }
    }

    public PartnerRoleChannel createPartnerRoleChannel(QName processId, PortType portType,
                                                       Endpoint initialPartnerEndpoint) {
        // NOTE: This implementation assumes that the initial value of the
        // partner role determines the binding.
        ProcessConf pconf = _server._store.getProcessConfiguration(processId);
        Definition wsdl = pconf.getDefinitionForService(initialPartnerEndpoint.serviceName);
        if (wsdl == null) {
            throw new ContextException("Cannot find definition for service " + initialPartnerEndpoint.serviceName
                                       + " in the context of process "+processId);
        }
        return createExternalService(pconf, initialPartnerEndpoint.serviceName, initialPartnerEndpoint.portName);
    }

    public long calculateSizeofService(EndpointReference epr) {
        if (_server._odeConfig.isProcessSizeThrottled()) {
            for (ODEService service : _serviceEprMap.keySet()) {
                if (epr.equals(_serviceEprMap.get(epr))) {
                    return SizingAgent.deepSizeOf(service);
                }
            }
        }
        return 0;
    }

    protected ODEService createService(ProcessConf pconf, QName serviceName, String portName) throws AxisFault {
        AxisService axisService = ODEAxisService.createService(_server._axisConfig, pconf, serviceName, portName);
        ODEService odeService = new ODEService(axisService, pconf, serviceName, portName, _server._bpelServer, _server._txMgr);

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
        if (__log.isDebugEnabled()) {
            __log.debug("Created Axis2 service " + serviceName);
        }
        return odeService;
    }

    protected ODEService destroyService(QName serviceName, String portName) {
        if (__log.isDebugEnabled()) {
            __log.debug("Destroying service " + serviceName + " port " + portName);
        }
        ODEService service = (ODEService) _services.remove(serviceName, portName);
        if (service != null) {
            // try to clean up the service after itself
            try {
                String axisServiceName = service.getAxisService().getName();
                AxisService axisService = _server._axisConfig.getService(axisServiceName);
                
                //axisService might be null if it could not be properly activated before. 
                if (axisService != null) {
                    // first, de-allocate its schemas
                    axisService.releaseSchemaList();
                    // then, de-allocate its parameters
                    // the service's wsdl object model is stored as one of its parameters!
                    // can't stress strongly enough how important it is to clean this up.
                    ArrayList<Parameter> parameters = (ArrayList<Parameter>) axisService.getParameters();
                    for (Parameter parameter : parameters) {
                        axisService.removeParameter(parameter);
                    }
                }
                // now, stop the service
                _server._axisConfig.stopService(axisServiceName);
                // if only this method did a good job of cleaning up after itself
                _server._axisConfig.removeService(service.getName());
                completeCleanup(axisService);
                _server._axisConfig.cleanup();
            } catch (AxisFault axisFault) {
                __log.error("Couldn't destroy service " + serviceName);
            }
        } else {
            if (__log.isDebugEnabled()) {
                __log.debug("Couldn't find service " + serviceName + " port " + portName + " to destroy.");
            }
        }
        return service;
    }

    /**
     * /!\ Monkey patching to remove references to the service:
     * Manually & externally & really really horribly fix for ODE-580/AXIS2-3870
     * The exception handling is for locked down environment where reflection would not be allowed...
     *
     * This patch is needed for Axis2 1.3 and 1.4.1
     * @param service
     * @throws AxisFault
     */
    private void completeCleanup(AxisService service) {
        try {
            Field field= _server._axisConfig.getClass().getDeclaredField("allEndpoints");
            field.setAccessible(true);
            synchronized (_server._axisConfig) {
                //removes the endpoints to this service
                Map allEndpoints = (Map) field.get(_server._axisConfig);

                //removes the service endpoints
                for (Iterator<String> iter = service.getEndpoints().keySet().iterator(); iter.hasNext();) {
                    allEndpoints.remove(service.getName() + "." + iter.next());
                }
            }
        } catch(Exception e) {
            __log.error("Workaround for ODE-580/AXIS2-3870 failed. AxisConfig clean up might be incomplete.",  e);
        }
    }

    protected ExternalService createExternalService(ProcessConf pconf, QName serviceName, String portName) throws ContextException {
        ExternalService extService = null;

        Definition def = pconf.getDefinitionForService(serviceName);
        try {
            if (WsdlUtils.useHTTPBinding(def, serviceName, portName)) {
                if (__log.isDebugEnabled()) __log.debug("Creating HTTP-bound external service " + serviceName);
                extService = new HttpExternalService(pconf, serviceName, portName, _server._executorService, _server._scheduler, _server._bpelServer, _server.httpConnectionManager, _server._clusterUrlTransformer);
            } else if (WsdlUtils.useSOAPBinding(def, serviceName, portName)) {
                if (__log.isDebugEnabled()) __log.debug("Creating SOAP-bound external service " + serviceName);
                extService = new SoapExternalService(pconf, serviceName, portName, _server._executorService, _server._axisConfig, _server._scheduler, _server._bpelServer, _server.httpConnectionManager, _server._clusterUrlTransformer);
            }
        } catch (Exception ex) {
            __log.error("Could not create external service.", ex);
            throw new ContextException("Error creating external service! name:" + serviceName + ", port:" + portName, ex);
        }

        // if not SOAP nor HTTP binding
        if (extService == null) {
            throw new ContextException("Only SOAP and HTTP binding supported!");
        }

        if (__log.isDebugEnabled()) {
            __log.debug("Created external service " + serviceName);
        }
        return extService;
    }

}
