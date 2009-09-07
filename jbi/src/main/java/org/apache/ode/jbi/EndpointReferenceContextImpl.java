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
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Map;
import java.util.HashMap;

/**
 * Implementation of the ODE {@link org.apache.ode.bpel.iapi.EndpointReferenceContext}
 * interface used by the BPEL engine to convert XML descriptions of endpoint
 * references (EPRs) into Java object representations. In the JBI context all
 * endpoint references are considered to be JBI
 * {@link javax.jbi.servicedesc.ServiceEndpoint}s are resolved by using the
 * {@link javax.jbi.component.ComponentContext#resolveEndpointReference(org.w3c.dom.DocumentFragment)}
 * method. Note that is is possible to resolve both "internal" and "external"
 * endpoint in this manner.  The schema to the internal end-point representation
 * is described on page 50 of the JBI specification 1.0.
 *
 */
public class EndpointReferenceContextImpl implements EndpointReferenceContext {
  private static final Log __log = LogFactory.getLog(EndpointReferenceContextImpl.class);

  private final OdeContext _ode;

  static final QName JBI_EPR = new QName("http://java.sun.com/jbi/end-point-reference", "end-point-reference");

  public EndpointReferenceContextImpl(OdeContext ode) {
    _ode = ode;
  }

  public EndpointReference resolveEndpointReference(Element epr) {
    QName elname = new QName(epr.getNamespaceURI(),epr.getLocalName());

    if (__log.isDebugEnabled()) {
      __log.debug( "resolveEndpointReference:\n" + prettyPrint( epr ) );
    }
    if (!elname.equals(EndpointReference.SERVICE_REF_QNAME))
      throw new IllegalArgumentException("EPR root element "
          + elname + " should be " + EndpointReference.SERVICE_REF_QNAME);

    Document doc = DOMUtils.newDocument();
    DocumentFragment fragment = doc.createDocumentFragment();
    Element e = DOMUtils.findChildByName(epr, JBI_EPR, true);
    if (e != null) {
        fragment.appendChild(doc.importNode(e, true));
    }
    
    ServiceEndpoint se = _ode.getContext().resolveEndpointReference(fragment);
    if (__log.isDebugEnabled()) {
        __log.debug("resolveEndpointReference2 jbiepr:" + DOMUtils.domToString(fragment) + " se:" + se);
    }
    if (se == null)
      return null;
    return new JbiEndpointReference(se);
  }

  public EndpointReference convertEndpoint(QName eprType, Element epr) {
      Document doc = DOMUtils.newDocument();
      DocumentFragment fragment = doc.createDocumentFragment();
      NodeList children = epr.getChildNodes();
      for (int i = 0 ; i < children.getLength(); ++i)
        fragment.appendChild(doc.importNode(children.item(i), true));
      ServiceEndpoint se = _ode.getContext().resolveEndpointReference(fragment);
      if (se == null)
          return null;

      return new JbiEndpointReference(se, eprType);

  }

  public static QName convertClarkQName(String name) {
    int pos = name.indexOf('}');
    if ( name.startsWith("{") && pos > 0 ) {
      String ns = name.substring(1,pos);
      String lname = name.substring(pos+1, name.length());
      return new QName( ns, lname );
    }
    return new QName( name );
  }

  private String prettyPrint( Element el ) {
      try {
          return DOMUtils.prettyPrint( el );
      } catch ( java.io.IOException ioe ) {
          return ioe.getMessage();
      }
  }

    public Map getConfigLookup(EndpointReference epr) {
        Map m = new HashMap();
        m.put("service", ((JbiEndpointReference)epr).getServiceEndpoint().getServiceName());
        return m;
    }
}
