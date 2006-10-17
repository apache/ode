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

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.util.OMUtils;
import org.apache.ode.axis2.util.SOAPUtils;
import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.epr.WSAEndpoint;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.transaction.TransactionManager;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A running service, encapsulates the Axis service, its receivers and our
 * receivers as well.
 */
public class ODEService {

    private static final Log __log = LogFactory.getLog(ODEService.class);

    private static final int TIMEOUT = 2 * 60 * 1000;

    private AxisService _axisService;

    private BpelServer _server;

    private TransactionManager _txManager;

    private Definition _wsdlDef;

    private QName _serviceName;

    private String _portName;

    private Map<String, ResponseCallback> _waitingCallbacks;

    private WSAEndpoint _serviceRef;

    public ODEService(AxisService axisService, Definition def, QName serviceName, String portName, BpelServer server,
                      TransactionManager txManager) {
        _axisService = axisService;
        _server = server;
        _txManager = txManager;
        _wsdlDef = def;
        _serviceName = serviceName;
        _portName = portName;
        _waitingCallbacks = Collections.synchronizedMap(new HashMap<String, ResponseCallback>());
        _serviceRef = EndpointFactory.convertToWSA(createServiceRef(genEPRfromWSDL(_wsdlDef, serviceName, portName)));
    }

    public void onAxisMessageExchange(MessageContext msgContext, MessageContext outMsgContext, SOAPFactory soapFactory)
            throws AxisFault {
        boolean success = true;
        MyRoleMessageExchange odeMex = null;
        ResponseCallback callback = null;
        try {
            _txManager.begin();

            // Creating mesage exchange
            String messageId = new GUID().toString();
            odeMex = _server.getEngine().createMessageExchange("" + messageId, _serviceName,
                    msgContext.getAxisOperation().getName().getLocalPart());
            __log.debug("ODE routed to operation " + odeMex.getOperation() + " from service " + _serviceName);

            if (odeMex.getOperation() != null) {
                // Preparing message to send to ODE
                Message odeRequest = odeMex.createMessage(odeMex.getOperation().getInput().getMessage().getQName());
                Element msgContent = SOAPUtils.unwrap(OMUtils.toDOM(msgContext.getEnvelope().getBody()
                        .getFirstElement()), _wsdlDef, odeMex.getOperation().getInput().getMessage(), _serviceName);
                readHeader(msgContext, odeMex);
                odeRequest.setMessage(msgContent);

                // Preparing a callback just in case we would need one.
                if (odeMex.getOperation().getOutput() != null) {
                    callback = new ResponseCallback();
                    _waitingCallbacks.put(odeMex.getClientId(), callback);
                }

                if (__log.isDebugEnabled()) {
                    __log.debug("Invoking ODE using MEX " + odeMex);
                    __log.debug("Message content:  " + DOMUtils.domToString(odeRequest.getMessage()));
                }
                // Invoking ODE
                odeMex.invoke(odeRequest);
            } else {
                success = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
            throw new AxisFault("An exception occured when invoking ODE.", e);
        } finally {
            if (success) {
                __log.debug("Commiting ODE MEX " + odeMex);
                try {
                    _txManager.commit();
                } catch (Exception e) {
                    __log.error("COMMIT FAILED!", e);
                    success = false;
                }
            }
            if (!success) {
                __log.error("Rolling back ODE MEX " + odeMex);
                try {
                    _txManager.rollback();
                } catch (Exception e) {
                    throw new AxisFault("ROLLBACK FAILED!", e);
                }
            }

            if (odeMex.getOperation() != null) {
                boolean timeout = false;
                // Invocation response could be delayed, if so we have to wait
                // for it.
                if (odeMex.getMessageExchangePattern() == MessageExchangePattern.REQUEST_RESPONSE &&
                        odeMex.getStatus() == MessageExchange.Status.ASYNC) {
                    odeMex = callback.getResponse(TIMEOUT);
                    if (odeMex == null)
                        timeout = true;
                } else {
                    // Callback wasn't necessary, cleaning up
                    _waitingCallbacks.remove(odeMex.getMessageExchangeId());
                }

                if (outMsgContext != null) {
                    SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
                    outMsgContext.setEnvelope(envelope);

                    // Hopefully we have a response
                    __log.debug("Handling response for MEX " + odeMex);
                    if (timeout) {
                        __log.error("Timeout when waiting for response to MEX " + odeMex);
                        success = false;
                    } else {
                        boolean commit = false;
                        try {
                            _txManager.begin();
                        } catch (Exception ex) {
                            throw new AxisFault("Error starting transaction!", ex);
                        }
                        try {
                            onResponse(odeMex, outMsgContext);
                            commit = true;
                        } catch (AxisFault af) {
                            commit = true;
                            throw af;
                        } catch (Exception e) {
                            throw new AxisFault("An exception occured when invoking ODE.", e);
                        } finally {
                            if (commit)
                                try {
                                    _txManager.commit();
                                } catch (Exception e) {
                                    throw new AxisFault("Commit failed!", e);
                                }
                            else
                                try {
                                    _txManager.rollback();
                                } catch (Exception ex) {
                                    throw new AxisFault("Rollback failed!", ex);
                                }

                        }
                    }
                }
            }
        }
        if (!success)
            throw new AxisFault("Message was either unroutable or timed out!");
    }

    public void notifyResponse(MyRoleMessageExchange mex) {
        ResponseCallback callback = _waitingCallbacks.get(mex.getClientId());
        if (callback == null) {
            __log.error("No active service for message exchange: " + mex);
        } else {
            callback.onResponse(mex);
            _waitingCallbacks.remove(mex.getClientId());
        }
    }

    public boolean respondsTo(QName serviceName, QName portTypeName) {
        boolean result = _serviceName.equals(serviceName);
        result = result
                && _wsdlDef.getService(_serviceName).getPort(_portName).getBinding().getPortType().getQName().equals(
                portTypeName);
        return result;
    }

    private void onResponse(MyRoleMessageExchange mex, MessageContext msgContext) throws AxisFault {
        switch (mex.getStatus()) {
            case FAULT:
                throw new AxisFault(mex.getResponse().getType(), mex.getFaultExplanation(), null, null,
                        mex.getFaultResponse().getMessage() == null ? null : OMUtils.toOM(mex.getFaultResponse().getMessage()));
            case ASYNC:
            case RESPONSE:
                Element response = SOAPUtils.wrap(mex.getResponse().getMessage(), _wsdlDef, _serviceName, mex
                        .getOperation(), mex.getOperation().getOutput().getMessage());
                if (__log.isDebugEnabled()) __log.debug("Received response message " +
                        DOMUtils.domToString(response));
                msgContext.getEnvelope().getBody().addChild(OMUtils.toOM(response));
                writeHeader(msgContext, mex);
                break;
            case FAILURE:
                throw new AxisFault("Message exchange failure!");
            default:
                __log.warn("Received ODE message exchange in unexpected state: " + mex.getStatus());
        }
    }

    /**
     * Extracts endpoint information from Axis MessageContext (taken from WSA
     * headers) to stuff them into ODE mesage exchange.
     */
    private void readHeader(MessageContext msgContext, MyRoleMessageExchange odeMex) {
        Object otse = msgContext.getProperty("targetSessionEndpoint");
        Object ocse = msgContext.getProperty("callbackSessionEndpoint");
        if (otse != null) {
            Element serviceEpr = (Element) otse;
            WSAEndpoint endpoint = new WSAEndpoint();
            endpoint.set(serviceEpr);
            // Extract the session ID for the local process.
            odeMex.setProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID, endpoint.getSessionId());
        }
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
        if (odeMex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID)!= null) {
            _serviceRef.setSessionId(odeMex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID));
            msgContext.setProperty("callbackSessionEndpoint", _serviceRef);
        }

    }

    public AxisService getAxisService() {
        return _axisService;
    }

    class ResponseCallback {
        private MyRoleMessageExchange _mmex;

        private boolean _timedout;

        synchronized boolean onResponse(MyRoleMessageExchange mmex) {
            if (_timedout) {
                return false;
            }
            _mmex = mmex;
            this.notify();
            return true;
        }

        synchronized MyRoleMessageExchange getResponse(long timeout) {
            long etime = timeout == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;
            long ctime;
            try {
                while (_mmex == null && (ctime = System.currentTimeMillis()) < etime) {
                    this.wait(etime - ctime);
                }
            } catch (InterruptedException ie) {
                // ignore
            }
            _timedout = _mmex == null;
            return _mmex;
        }
    }

    /**
     * Return the service-ref element that will be used to represent this
     * endpoint.
     * 
     * @return
     */
    public EndpointReference getMyServiceRef() {
        return _serviceRef;
    }

    /**
     * Get the EPR of this service from the WSDL.
     * 
     * @param name
     *            service name
     * @param portName
     *            port name
     * @return XML representation of the EPR
     */
    public static Element genEPRfromWSDL(Definition wsdlDef, QName name, String portName) {
        System.out.println("SERVICE NAME: " + name);
        System.out.println("SERVICE PORT: " + portName);

        for (Object s : wsdlDef.getServices().values()) {
            System.out.println("Found service " + ((Service)s).getQName());
            for (Object p : ((Service) s).getPorts().values()) {
                System.out.println("Found port " + ((Port)p).getName());
            }
        }

        Service serviceDef = wsdlDef.getService(name);
        System.out.println("Service def " + serviceDef);
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
