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

package org.apache.ode.jbi;

import javax.jbi.servicedesc.ServiceEndpoint;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.utils.DOMUtils;

/**
 * JBI-based implementation of the ODE {@link org.apache.ode.bpel.iapi.EndpointReference}
 * interface. This is basically a wrapper around the 
 * {@link javax.jbi.servicedesc.ServiceEndpoint} interface. 
 */
class JbiEndpointReference implements EndpointReference {

  private ServiceEndpoint _se;

  JbiEndpointReference(ServiceEndpoint se) {
    if (se == null)
      throw new NullPointerException("Null ServiceEndpoint");
    _se = se;
  }
  
  public Document toXML() {
    DocumentFragment fragment = _se.getAsReference(null);
    if (fragment == null)
      return null;
    
    Document doc = DOMUtils.newDocument();
    Element root = doc.createElementNS(SERVICE_REF_QNAME.getNamespaceURI(),SERVICE_REF_QNAME.getLocalPart());
    root.appendChild(fragment);
    doc.appendChild(root);
    return doc;
  }

  public boolean equals(Object other) {
    if (other instanceof JbiEndpointReference)
      return _se.getServiceName().equals(((JbiEndpointReference)other)._se.getServiceName());
    return false;
  }
  
  public int hashCode() {
    return _se.getServiceName().hashCode();
  }

  ServiceEndpoint getServiceEndpoint() {
    return _se;
  }
}
