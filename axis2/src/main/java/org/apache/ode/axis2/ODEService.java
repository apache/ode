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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.Map;

import javax.transaction.TransactionManager;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TwoChannelAxisOperation;
import org.apache.axis2.transport.jms.JMSConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.util.SoapMessageConverter;
import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.epr.WSAEndpoint;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A running service, encapsulates the Axis service, its receivers and our
 * receivers as well.
 */
public class ODEService {

    private static final Log __log = LogFactory.getLog(ODEService.class);

    private AxisService _axisService;
    private BpelServer _server;
    private TransactionManager _txManager;
    private ProcessConf _pconf;
    private Definition _wsdlDef;
    private QName _serviceName;
    private String _portName;
    private WSAEndpoint _serviceRef;
    private SoapMessageConverter _converter;

    public ODEService(AxisService axisService, ProcessConf pconf, QName serviceName, String portName, BpelServer server,
                      TransactionManager txManager) throws AxisFault {
        _axisService = axisService;
        _server = server;
        _txManager = txManager;
        _pconf = pconf;
        _wsdlDef = pconf.getDefinitionForService(serviceName);
        _serviceName = serviceName;
        _portName = portName;
        _serviceRef = EndpointFactory.convertToWSA(createServiceRef(genEPRfromWSDL(_wsdlDef, serviceName, portName)));
        _converter = new SoapMessageConverter(_wsdlDef, serviceName, portName);

    }
    
    public String getName() {
    	return _axisService.getName();
    }
    
    public void onAxisMessageExchange(MessageContext msgContext, MessageContext outMsgContext, SOAPFactory soapFactory)
            throws AxisFault {
        boolean success = true;
        MyRoleMessageExchange odeMex = null;
        Future responseFuture = null;
        try {
            _txManager.begin();
            if (__log.isDebugEnabled()) __log.debug("Starting transaction.");

            // Creating message exchange
            String messageId = new GUID().toString();
            odeMex = _server.getEngine().createMessageExchange("" + messageId, _serviceName,
                    msgContext.getAxisOperation().getName().getLocalPart());
            __log.debug("ODE routed to operation " + odeMex.getOperation() + " from service " + _serviceName);
            odeMex.setProperty("isTwoWay", Boolean.toString(msgContext.getAxisOperation() instanceof TwoChannelAxisOperation));
            if (odeMex.getOperation() != null) {
                // Preparing message to send to ODE
                Message odeRequest = odeMex.createMessage(odeMex.getOperation().getInput().getMessage().getQName());
                _converter.parseSoapRequest(odeRequest, msgContext.getEnvelope(), odeMex.getOperation());
                readHeader(msgContext, odeMex);

                if (__log.isDebugEnabled()) {
                    __log.debug("Invoking ODE using MEX " + odeMex);
                    __log.debug("Message content:  " + DOMUtils.domToString(odeRequest.getMessage()));
                }

                // Invoke ODE
                responseFuture = odeMex.invoke(odeRequest);

                __log.debug("Commiting ODE MEX " + odeMex);
                try {
                    if (__log.isDebugEnabled()) __log.debug("Commiting transaction.");
                    _txManager.commit();
                } catch (Exception e) {
                    __log.error("Commit failed", e);
                    success = false;
                }
            } else {
                success = false;
            }
        } catch (Exception e) {
            __log.error("Exception occured while invoking ODE", e);
            success = false;
            String message = e.getMessage();
            if (message == null) {
            	message = "An exception occured while invoking ODE.";
            }
            throw new OdeFault(message, e);
        } finally {
            if (!success) {
                if (odeMex != null) odeMex.release(success);
                try {
                    _txManager.rollback();
                } catch (Exception e) {
                    throw new OdeFault("Rollback failed", e);
                }
            }
        }

        if (odeMex.getOperation().getOutput() != null) {
            // Waits for the response to arrive
            try {
                responseFuture.get(getTimeout(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                String errorMsg = "Timeout or execution error when waiting for response to MEX "
                        + odeMex + " " + e.toString();
                __log.error(errorMsg, e);
                throw new OdeFault(errorMsg);
            }

            if (outMsgContext != null) {
                SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
                outMsgContext.setEnvelope(envelope);

                // Hopefully we have a response
                __log.debug("Handling response for MEX " + odeMex);
                boolean commit = false;
                try {
                    if (__log.isDebugEnabled()) __log.debug("Starting transaction.");
                    _txManager.begin();
                } catch (Exception ex) {
                    throw new OdeFault("Error starting transaction!", ex);
                }
                try {
                    // Refreshing the message exchange
                    odeMex = (MyRoleMessageExchange) _server.getEngine().getMessageExchange(odeMex.getMessageExchangeId());
                    onResponse(odeMex, outMsgContext);
                    commit = true;
                } catch (AxisFault af) {
                    __log.warn("MEX produced a fault " + odeMex, af);
                    commit = true;
                    throw af;
                } catch (Exception e) {
                    __log.error("Error processing response for MEX " + odeMex, e);
                    throw new OdeFault("An exception occured when invoking ODE.", e);
                } finally {
                    odeMex.release(commit);
                    if (commit) {
                        try {
                            if (__log.isDebugEnabled()) __log.debug("Comitting transaction.");
                            _txManager.commit();
                        } catch (Exception e) {
                            throw new OdeFault("Commit failed!", e);
                        }
                    } else {
                        try {
                            _txManager.rollback();
                        } catch (Exception ex) {
                            throw new OdeFault("Rollback failed!", ex);
                        }
                    }
                }
            }
            if (!success) {
                throw new OdeFault("Message was either unroutable or timed out!");
            }
        } else {
            // One ways cleanup
            odeMex.release(true);
        }
    }

    public boolean respondsTo(QName serviceName, QName portTypeName) {
        boolean result = _serviceName.equals(serviceName);
        result = result
                && _wsdlDef.getService(_serviceName).getPort(_portName).getBinding().getPortType().getQName().equals(
                portTypeName);
        return result;
    }

    /**
     * do not store the value so it can be dynamically updated
     */
    private long getTimeout() {
        String timeout = _pconf.getEndpointProperties(_serviceRef).get(Properties.PROP_MEX_TIMEOUT);
        if (timeout != null) {
            try {
                return Long.parseLong(timeout);
            } catch (NumberFormatException e) {
                if(__log.isWarnEnabled()) __log.warn("Mal-formatted Property: ["+ Properties.PROP_MEX_TIMEOUT+"="+timeout+"] Default value ("+Properties.DEFAULT_MEX_TIMEOUT+") will be used");
            }
        }
        return Properties.DEFAULT_MEX_TIMEOUT;
    }

    /**
     *
     * @return an Option instance containing the properties for this endpoint
     * @see org.apache.ode.bpel.iapi.ProcessConf#getEndpointProperties(org.apache.ode.bpel.iapi.EndpointReference)
     */
    public Options getOptions(){
        return Properties.Axis2.translate(_pconf.getEndpointProperties(_serviceRef));
    }

    private void onResponse(MyRoleMessageExchange mex, MessageContext msgContext) throws AxisFault {
        switch (mex.getStatus()) {
            case FAULT:
                if (__log.isDebugEnabled())
                    __log.debug("Fault response message: " + mex.getFault());
                SOAPFault fault = _converter.createSoapFault(mex.getFaultResponse().getMessage(), mex.getFault(), mex.getOperation());
                msgContext.getEnvelope().getBody().addFault(fault);

                if (__log.isDebugEnabled())
                    __log.debug("Returning fault: " + msgContext.getEnvelope().toString());
                break;
            case ASYNC:
            case RESPONSE:
                _converter.createSoapResponse(msgContext, mex.getResponse(), mex.getOperation());
                if (__log.isDebugEnabled())
                    __log.debug("Response message " + msgContext.getEnvelope());
                writeHeader(msgContext, mex);
                break;
            case FAILURE:
                throw new OdeFault("Message exchange failure");
            default:
                throw new OdeFault("Received ODE message exchange in unexpected state: " + mex.getStatus());
        }
    }

    /**
     * Extracts endpoint information from Axis MessageContext (taken from WSA
     * headers) to stuff them into ODE mesage exchange.
     */
    private void readHeader(MessageContext msgContext, MyRoleMessageExchange odeMex) {
        String correlationId = (String) msgContext.getProperty(JMSConstants.JMS_COORELATION_ID);
        if (correlationId != null) {
            odeMex.setProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID, correlationId);
        } else {
            Object otse = msgContext.getProperty("targetSessionEndpoint");
            if (otse != null) {
                Element serviceEpr = (Element) otse;
                WSAEndpoint endpoint = new WSAEndpoint();
                endpoint.set(serviceEpr);
                // Extract the session ID for the local process.
                odeMex.setProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID, endpoint.getSessionId());
            }
        }
        
        Object ocse = msgContext.getProperty("callbackSessionEndpoint");
        if (ocse != null) {
            Element serviceEpr = (Element) ocse;
            WSAEndpoint endpoint = new WSAEndpoint();
            endpoint.set(serviceEpr);

            // Save the session id of the remote process. Also, magically
            // initialize the EPR
            // of the partner to the EPR provided.
            odeMex.setProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID, endpoint.getSessionId());
            odeMex.setProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_EPR, DOMUtils.domToString(serviceEpr));
        }
    }

    /**
     * Handle callback endpoints for the case where partner contact process
     * my-role which results in an "updated" my-role EPR due to session id
     * injection.
     */
    private void writeHeader(MessageContext msgContext, MyRoleMessageExchange odeMex) {
        EndpointReference targetEPR = odeMex.getEndpointReference();
        if (targetEPR == null)
            return;

        // The callback endpoint is going to be the same as the target
        // endpoint in this case, except that it is updated with session
        // information (if available).
        if (odeMex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID) != null) {
            WSAEndpoint sessionAwareEndPoint = new WSAEndpoint(_serviceRef); 
            sessionAwareEndPoint.setSessionId(odeMex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID));
            msgContext.setProperty("callbackSessionEndpoint", sessionAwareEndPoint);
        }

    }

    public AxisService getAxisService() {
        return _axisService;
    }

    /**
     * Return the service-ref element that will be used to represent this
     * endpoint.
     *
     * @return my service endpoint
     */
    public EndpointReference getMyServiceRef() {
        return _serviceRef;
    }

    /**
     * Get the EPR of this service from the WSDL.
     *
     * @param name     service name
     * @param portName port name
     * @return XML representation of the EPR
     */
    public static Element genEPRfromWSDL(Definition wsdlDef, QName name, String portName) {
        Service serviceDef = wsdlDef.getService(name);
        if (serviceDef != null) {
            Port portDef = serviceDef.getPort(portName);
            if (portDef != null) {
                Document doc = DOMUtils.newDocument();
                Element service = doc.createElementNS(Namespaces.WSDL_11, "service");
                service.setAttribute("name", serviceDef.getQName().getLocalPart());
                service.setAttribute("targetNamespace", serviceDef.getQName().getNamespaceURI());
                Element port = doc.createElementNS(Namespaces.WSDL_11, "port");
                service.appendChild(port);
                port.setAttribute("name", portDef.getName());
                port.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:bindns", portDef.getBinding().getQName()
                        .getNamespaceURI());
                port.setAttribute("bindns:binding", portDef.getName());
                for (Object extElmt : portDef.getExtensibilityElements()) {
                    if (extElmt instanceof SOAPAddress) {
                        Element soapAddr = doc.createElementNS(Namespaces.SOAP_NS, "address");
                        port.appendChild(soapAddr);
                        soapAddr.setAttribute("location", ((SOAPAddress) extElmt).getLocationURI());
                    } else if (extElmt instanceof HTTPAddress) {
                        Element httpAddr = doc.createElementNS(Namespaces.HTTP_NS, "address");
                        port.appendChild(httpAddr);
                        httpAddr.setAttribute("location", ((HTTPAddress) extElmt).getLocationURI());
                    } else {
                        port.appendChild(doc.importNode(((UnknownExtensibilityElement) extElmt).getElement(), true));
                    }
                }
                return service;
            }
        }
        return null;
    }

    /**
     * Create-and-copy a service-ref element.
     *
     * @param elmt
     * @return wrapped element
     */
    public static MutableEndpoint createServiceRef(Element elmt) {
        Document doc = DOMUtils.newDocument();
        QName elQName = new QName(elmt.getNamespaceURI(), elmt.getLocalName());
        // If we get a service-ref, just copy it, otherwise make a service-ref
        // wrapper
        if (!EndpointReference.SERVICE_REF_QNAME.equals(elQName)) {
            Element serviceref = doc.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
                    EndpointReference.SERVICE_REF_QNAME.getLocalPart());
            serviceref.appendChild(doc.importNode(elmt, true));
            doc.appendChild(serviceref);
        } else {
            doc.appendChild(doc.importNode(elmt, true));
        }

        return EndpointFactory.createEndpoint(doc.getDocumentElement());
    }
}
