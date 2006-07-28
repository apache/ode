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

package org.apache.ode.bpel.dd;

import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Enhance a deployment descriptor by adding endpoint references
 * if they are defined in a related WSDL document (from the
 * <code>soap:address</code> element).
 */
public class EndpointEnhancer implements DDEnhancer {

  private static final Log __log = LogFactory.getLog(DDHandler.class);

  public boolean enhance(TDeploymentDescriptor dd, OProcess oprocess, Definition[] wsdlDefs)
          throws DDException {
    // Creating endpoints from WSDL SOAP address if none are specified
    boolean updated = false;
    for (OPartnerLink plink : oprocess.getAllPartnerLinks()) {
      __log.debug("Analyzing partner link " + plink.getName());
      if (plink.hasPartnerRole() && plink.initializePartnerRole) {
        URL soapUrl = resolveServiceURL(wsdlDefs, plink.partnerRolePortType);
        __log.debug("Resolved service url: " + soapUrl);
        updated = declareEndpoint(dd, plink, soapUrl, false) || updated;
      }
      // If my role is never used in assignment, no need to declare it.
      if (plink.hasMyRole()) {
        URL soapUrl = resolveServiceURL(wsdlDefs, plink.myRolePortType);
        updated = declareEndpoint(dd, plink, soapUrl, true) || updated;
      }
    }
    return updated;
  }

  /**
   * Reads WSDL description to establish a default endpoint address using
   * the soap:address element.
   * @param wsdlDefs
   * @param pType
   * @return
   * @throws DDException
   */
  private URL resolveServiceURL(Definition[] wsdlDefs, PortType pType) throws DDException {
    QName portTypeQName = pType.getQName();

    Binding wsdlBinding = null;
    for (Definition def : wsdlDefs) {
      for (Object obinding : def.getBindings().values()) {
        Binding binding = (Binding) obinding;
        if (binding.getPortType().getQName().getLocalPart().equals(portTypeQName.getLocalPart())
                && binding.getPortType().getQName().getNamespaceURI().equals(portTypeQName.getNamespaceURI())) {
          wsdlBinding = binding;
        }
      }
    }
    if (wsdlBinding == null) {
      __log.warn("No binding for portType with namespace " + portTypeQName.getNamespaceURI() +
                 " and name " + portTypeQName.getLocalPart() + " in WSDL definition.");
      return null;
    }
    Port wsdlPort = null;
    for (Definition def : wsdlDefs) {
      for (Object oservice : def.getServices().values()) {
        Service service = (Service) oservice;
        for (Object oport : service.getPorts().values()) {
          Port port = (Port) oport;
          if (port.getBinding().getQName().equals(wsdlBinding.getQName())) {
            wsdlPort = port;
          }
        }
      }
    }
    if (wsdlPort == null) {
      __log.warn("No port for binding with namespace " + wsdlBinding.getQName().getNamespaceURI() +
                " and name " + wsdlBinding.getQName().getLocalPart() + " in WSDL definition.");
      return null;
    }
    String soapURIStr = null;
    SOAPAddress wsdlSoapAddress = null;
    for (Object oextElmt : wsdlPort.getExtensibilityElements()) {
      ExtensibilityElement extElmt = (ExtensibilityElement) oextElmt;
      if (extElmt instanceof UnknownExtensibilityElement)
        if ((((UnknownExtensibilityElement)extElmt).getElement()).hasAttribute("location"))
          soapURIStr = ((UnknownExtensibilityElement)extElmt).getElement().getAttribute("location");
      if (extElmt instanceof SOAPAddress) wsdlSoapAddress = (SOAPAddress)extElmt;
    }
    if (wsdlSoapAddress == null && soapURIStr == null) {
      __log.warn("No port for binding with namespace " + wsdlBinding.getQName().getNamespaceURI() +
              " and name " + wsdlBinding.getQName().getLocalPart() + " in WSDL definition.");
      return null;
    }

    if (soapURIStr == null) soapURIStr = wsdlSoapAddress.getLocationURI();
    if (soapURIStr == null || "".equals(soapURIStr)) {
      __log.warn("No SOAP address for port " + wsdlPort.getName() + "  in WSDL definition.");
      return null;
    }

    URL portURL;
    try {
      portURL = new URL(soapURIStr);
    } catch (MalformedURLException e) {
      __log.warn("SOAP address URL " + soapURIStr + " is invalid (not a valid URL).");
      return null;
    }
    return portURL;
  }

  private boolean declareEndpoint(TDeploymentDescriptor dd, OPartnerLink plink,
                                  URL soapUrl, boolean myRole) {
    boolean updated = false;
    TDeploymentDescriptor.EndpointRefs eprs = dd.getEndpointRefs();
    if (eprs == null) eprs = dd.addNewEndpointRefs();

    TDeploymentDescriptor.EndpointRefs.EndpointRef foundEpr = null;
    for (TDeploymentDescriptor.EndpointRefs.EndpointRef epr : eprs.getEndpointRefList()) {
      if (epr.getPartnerLinkName().equals(plink.getName()) && epr.getPartnerLinkRole().equals(TRoles.PARTNER_ROLE)
              && !myRole) {
        foundEpr = epr;
      }
      if (epr.getPartnerLinkName().equals(plink.getName()) && epr.getPartnerLinkRole().equals(TRoles.MY_ROLE)
              && myRole) {
        foundEpr = epr;
      }
    }
    if (__log.isDebugEnabled()) {
      if (foundEpr == null) __log.debug("No EPR declared in the deployment descriptor for " + plink.getName() +
              " (myRole=" + myRole + "), creating one.");
      else __log.debug("An EPR is already declared in the deployment descriptor for " + plink.getName() +
              " (myRole=" + myRole + ").");
    }
    if (foundEpr == null) {
      updated = true;
      TDeploymentDescriptor.EndpointRefs.EndpointRef epr = eprs.addNewEndpointRef();
      epr.setPartnerLinkName(plink.name);
      if (myRole) epr.setPartnerLinkRole(TRoles.MY_ROLE);
      else epr.setPartnerLinkRole(TRoles.PARTNER_ROLE);
      Node eprNode = epr.getDomNode();
      Element addrElmt = eprNode.getOwnerDocument()
              .createElementNS("http://schemas.xmlsoap.org/wsdl/soap/", "address");
      addrElmt.setAttribute("location", soapUrl.toExternalForm());
      eprNode.appendChild(addrElmt);
    }
    return updated;
  }
}
