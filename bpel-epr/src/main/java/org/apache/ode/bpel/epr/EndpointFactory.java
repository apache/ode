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

package org.apache.ode.bpel.epr;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Element;

/**
 * Factory for {@link org.apache.ode.bpel.iapi.EndpointReference}
 * implementations.
 */
public class EndpointFactory {

    private static final Log __log = LogFactory.getLog(EndpointFactory.class);

    private static QName WSDL20_ELMT_QNAME = new QName(Namespaces.WSDL_20, "service");

    private static QName WSDL11_ELMT_QNAME = new QName(Namespaces.WSDL_11, "service");

    private static QName WSA_ELMT_QNAME = new QName(Namespaces.WS_ADDRESSING_NS, "EndpointReference");

    private static QName SOAP_ADDR_ELMT_QNAME = new QName(Namespaces.SOAP_NS, "address");

    private static MutableEndpoint[] ENDPOINTS = new MutableEndpoint[] { new URLEndpoint(), new WSAEndpoint(),
            new WSDL11Endpoint(), new WSDL20Endpoint() };

    /**
     * Creates a ServiceEndpoint using the provided Node. The actual endpoint
     * type is detected using the endpoint node (text or element qname).
     *
     * @param endpointElmt
     * @return the new ServiceEndpoint
     */
    public static MutableEndpoint createEndpoint(Element endpointElmt) {
        for (MutableEndpoint endpoint : EndpointFactory.ENDPOINTS) {
            // Eliminating the service-ref element for accept
            if (endpoint.accept(endpointElmt)) {
                MutableEndpoint se;
                try {
                    se = endpoint.getClass().newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                se.set(endpointElmt);
                return se;
            }
        }
        __log.warn("Couldnt create any endpoint for element " + DOMUtils.domToString(endpointElmt));
        return null;
    }

    /**
     * Convert an EPR element into another EPR using the provided target type.
     * The target type is actually the qualified name of the root element for
     * the target EPR (i.e wsa:MutableEndpoint, wsdl:service) or null to convert
     * to a simple URL.
     *
     * @param targetElmtType
     *            QName to convert to
     * @param sourceEndpoint
     * @return the converted MutableEndpoint
     */
    public static MutableEndpoint convert(QName targetElmtType, Element sourceEndpoint) {
        MutableEndpoint targetEpr;
        MutableEndpoint sourceEpr = EndpointFactory.createEndpoint(sourceEndpoint);
        Map transfoMap = sourceEpr.toMap();
        if (targetElmtType == null) {
            targetEpr = new URLEndpoint();
        } else if (targetElmtType.equals(EndpointFactory.WSDL20_ELMT_QNAME)) {
            targetEpr = new WSDL20Endpoint();
        } else if (targetElmtType.equals(EndpointFactory.WSDL11_ELMT_QNAME)) {
            targetEpr = new WSDL11Endpoint();
        } else if (targetElmtType.equals(EndpointFactory.WSA_ELMT_QNAME)) {
            targetEpr = new WSAEndpoint();
        } else if (targetElmtType.equals(EndpointFactory.SOAP_ADDR_ELMT_QNAME)) {
            targetEpr = new URLEndpoint();
        } else {
            // When everything fails, shooting for the most simple EPR format
            targetEpr = new URLEndpoint();
        }

        targetEpr.fromMap(transfoMap);
        if (__log.isDebugEnabled()) {
            __log.debug("Converted endpoint to type " + targetElmtType);
            __log.debug("Source endpoint " + DOMUtils.domToString(sourceEndpoint));
            __log.debug("Destination endpoint " + DOMUtils.domToString(targetEpr.toXML()));
        }
        return targetEpr;
    }

    public static WSAEndpoint convertToWSA(MutableEndpoint source) {
        if (source == null)
            return null;

        if (source instanceof WSAEndpoint)
            return (WSAEndpoint) source;

        return new WSAEndpoint(source.toMap());
    }
}
