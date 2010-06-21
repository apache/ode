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

package org.apache.ode.axis2.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.apache.ode.bpel.engine.ProcessAndInstanceManagementImpl;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.pmapi.InstanceManagement;
import org.apache.ode.bpel.pmapi.ProcessManagement;
import org.apache.ode.il.DynamicService;
import org.apache.ode.utils.Namespaces;

/**
 * Axis2 wrapper for process and instance management interfaces.
 */
public class ManagementService {

    private static final Log __log = LogFactory.getLog(ManagementService.class);

    public static final QName PM_SERVICE_NAME = new QName("http://www.apache.org/ode/pmapi", "ProcessManagementService");
    public static final String PM_PORT_NAME = "ProcessManagementPort";
    public static final String PM_AXIS2_NAME = "ProcessManagement";

    public static final QName IM_SERVICE_NAME = new QName("http://www.apache.org/ode/pmapi", "InstanceManagementService");
    public static final String IM_PORT_NAME = "InstanceManagementPort";
    public static final String IM_AXIS2_NAME = "InstanceManagement";

    private ProcessManagement _processMgmt;
    private InstanceManagement _instanceMgmt;

    public void enableService(AxisConfiguration axisConfig, BpelServer server, ProcessStore _store, String rootpath) {
        ProcessAndInstanceManagementImpl pm = new ProcessAndInstanceManagementImpl(server, _store);
        _processMgmt = pm;
        _instanceMgmt = pm;

        Definition def;
        try {
            WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
            wsdlReader.setFeature("javax.wsdl.verbose", false);

            File wsdlFile = new File(rootpath + "/pmapi.wsdl");
            def = wsdlReader.readWSDL(wsdlFile.toURI().toString());
            AxisService processService = ODEAxisService.createService(axisConfig, PM_SERVICE_NAME, PM_PORT_NAME,
                    PM_AXIS2_NAME, def, new DynamicMessageReceiver<ProcessManagement>(_processMgmt));
            
            /*
             * XXX: Reparsing the WSDL is a workaround for WSCOMMONS-537 (see also ODE-853). When WSCOMMONS-537 is fixed
             * we can safely remove the following line. 
             */
            def = wsdlReader.readWSDL(wsdlFile.toURI().toString());
            /* end XXX */
            
            AxisService instanceService = ODEAxisService.createService(axisConfig, IM_SERVICE_NAME, IM_PORT_NAME,
                    IM_AXIS2_NAME, def, new DynamicMessageReceiver<InstanceManagement>(_instanceMgmt));
            axisConfig.addService(processService);
            axisConfig.addService(instanceService);
        } catch (WSDLException e) {
            __log.error("Couldn't start-up management services!", e);
        } catch (IOException e) {
            __log.error("Couldn't start-up management services!", e);
        }
    }

    public ProcessManagement getProcessMgmt() {
        return _processMgmt;
    }

    public InstanceManagement getInstanceMgmt() {
        return _instanceMgmt;
    }

    class DynamicMessageReceiver<T> extends AbstractMessageReceiver {
        T _service;

        public DynamicMessageReceiver(T service) {
            _service = service;
        }

        public void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {
            DynamicService<T> service = new DynamicService<T>(_service);
            MessageContext outMsgContext = Utils.createOutMessageContext(messageContext);
            outMsgContext.getOperationContext().addMessageContext(outMsgContext);
            SOAPFactory soapFactory = getSOAPFactory(messageContext);
            SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
            outMsgContext.setEnvelope(envelope);

            OMElement response;
            try {
                response = service.invoke(messageContext.getAxisOperation().getName().getLocalPart(),
                                          messageContext.getEnvelope().getBody().getFirstElement());
                if (response != null) {
                    envelope.getBody().addChild(response);
                }
            } catch (Exception e) {
                // Building a nicely formatted fault
                envelope.getBody().addFault(toSoapFault(e, soapFactory));
            }
            AxisEngine.send(outMsgContext);
    }

        private SOAPFault toSoapFault(Exception e, SOAPFactory soapFactory) {
        SOAPFault fault = soapFactory.createSOAPFault();
        SOAPFaultCode code = soapFactory.createSOAPFaultCode(fault);
        code.setText(new QName(Namespaces.SOAP_ENV_NS, "Server"));
        SOAPFaultReason reason = soapFactory.createSOAPFaultReason(fault);
        reason.setText(e.toString());

            OMElement detail = soapFactory
                    .createOMElement(new QName(Namespaces.ODE_PMAPI_NS, e.getClass().getSimpleName()));
        StringWriter stack = new StringWriter();
        e.printStackTrace(new PrintWriter(stack));
        detail.setText(stack.toString());
        SOAPFaultDetail soapDetail = soapFactory.createSOAPFaultDetail(fault);
        soapDetail.addDetailEntry(detail);
        return fault;
    }
    }

}
