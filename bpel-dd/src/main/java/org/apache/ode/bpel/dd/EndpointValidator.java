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
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Validates endpoint references declared in a deployment descriptor.
 */
public class EndpointValidator implements DDValidator {

  public void validate(TDeploymentDescriptor dd, OProcess oprocess) throws DDValidationException {
    // Validating that endpoints are declared on existing partner links
    if (dd.getEndpointRefs() != null) {
      for (TDeploymentDescriptor.EndpointRefs.EndpointRef epr : dd.getEndpointRefs().getEndpointRefList()) {
        boolean found = false;
        for (OPartnerLink plink : oprocess.getAllPartnerLinks()) {
          if (epr.getPartnerLinkName().equals(epr.getPartnerLinkName())
                  && plink.hasPartnerRole() && epr.getPartnerLinkRole().equals(TRoles.PARTNER_ROLE))
            found = true;
          if (epr.getPartnerLinkName().equals(epr.getPartnerLinkName())
                  && plink.hasMyRole() && epr.getPartnerLinkRole().equals(TRoles.MY_ROLE))
            found = true;
        }
        if (found) {
          validateEndpoint((Element) epr.getDomNode());
        } else throw new DDValidationException("Partner link " + epr.getPartnerLinkName() +
                  " could not be found with specified role in process definition.");
      }
    }
  }

  private void validateEndpoint(Element eprElmt) throws DDValidationException {
    Element root = DOMUtils.getFirstChildElement(eprElmt);
    String url = null;
    if (root == null) {
      throw new DDValidationException("Only soap:address elements or a wsa:Address element is accepted " +
              "as a valid endpoint in deployment descriptor.");
    } else if (root.getLocalName().equals("address")
            && root.getNamespaceURI().equals("http://schemas.xmlsoap.org/wsdl/soap/")) {
      url = root.getAttribute("location");
    } else if (root.getLocalName().equals("EndpointReference")
              && root.getNamespaceURI().equals(Namespaces.WS_ADDRESSING_NS)) {
      NodeList address = root.
              getElementsByTagNameNS(Namespaces.WS_ADDRESSING_NS, "Address");
      if (address.getLength() == 0)
        throw new DDValidationException("A WS-Addressing EndpointReference must enclose " +
                "a wsa:Address element.");
      url = "";
      for (int m = 0; m < address.item(0).getChildNodes().getLength(); m++) {
        Node txtNode = address.item(0).getChildNodes().item(m);
        if (txtNode.getNodeType() == Node.TEXT_NODE) {
          String txt = ((Text)txtNode).getWholeText();
          if (txt.trim().length() > 0) url = url + txt;
        }
      }
    } else if (root.getLocalName().equals("end-point-reference")
            && root.getNamespaceURI().equals( Namespaces.JBI_END_POINT_REFERENCE)) {
      String serviceName = root.getAttribute("service-name");
      String endpointName = root.getAttribute("end-point-name");
    }

    if ( url != null ) {
        try {
          new URL(url);
        } catch (MalformedURLException e) {
          throw new DDValidationException("URL " + root.getAttribute("location") + " declared as endpoint reference " +
                  "in deployment descriptor isn't a valid URL.");
        }
    }
  }

}
