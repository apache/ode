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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

public class EndpointReferenceContextImpl implements EndpointReferenceContext {

  private static final Log __log = LogFactory.getLog(EndpointReferenceContextImpl.class);

  private ODEServer _server;

  public EndpointReferenceContextImpl(ODEServer server) {
    _server = server;
  }

  public EndpointReference resolveEndpointReference(Element element) {
    if (__log.isDebugEnabled())
      __log.debug("Resolving endpoint reference " + DOMUtils.domToString(element));
    return EndpointFactory.createEndpoint(element);
  }

  public EndpointReference activateEndpoint(QName qName, QName qName1, Element element) {
    // Deprecated method
    return null;
  }

  public void activateEndpoint(QName serviceName, String portName, Definition wsdl) {
    try {
      _server.createService(wsdl, serviceName, portName);
    } catch (AxisFault axisFault) {
      throw new ContextException("Could not activate endpoint for service " + serviceName
              + " and port " + portName, axisFault);
    }
  }

  public void activateExternalEndpoint(QName serviceName, String portName, Definition wsdl) {
    _server.createExternalService(wsdl, serviceName, portName);
  }

  public void deactivateEndpoint(EndpointReference endpointReference) {
    // Axis doesn't need any explicit endpoint activation / deactivation
  }

  public EndpointReference convertEndpoint(QName qName, Element element) {
    EndpointReference endpoint = EndpointFactory.convert(qName, element);
    return endpoint;
  }
}
