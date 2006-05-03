/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.bom.wsdl;

import com.ibm.wsdl.util.xml.XPathUtils;

import java.io.PrintWriter;
import java.io.Serializable;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * WSDL4J de/serializer for BPEL <code>partnerLinkType</code> elements.
 * @see ExtensionSerializer
 * @see ExtensionDeserializer
 */
class PartnerLinkTypeSerializer_2_0
        extends BaseSerializerDeserializer
        implements ExtensionSerializer,
        ExtensionDeserializer,
        Serializable {

  private static final long serialVersionUID = -1L;
  
  final QName ROLE;
  final QName PARTNER_LINKTYPE;
  
  PartnerLinkTypeSerializer_2_0(String bpelUri){
    ROLE = new QName(bpelUri, "role");
    PARTNER_LINKTYPE = new QName(bpelUri, "partnerLinkType");
  }

  /**
   * @see javax.wsdl.extensions.ExtensionSerializer#marshall(java.lang.Class, javax.xml.namespace.QName, javax.wsdl.extensions.ExtensibilityElement, java.io.PrintWriter, javax.wsdl.Definition, javax.wsdl.extensions.ExtensionRegistry)
   */
  public void marshall(Class arg0, QName arg1, ExtensibilityElement arg2,
                       PrintWriter arg3, Definition arg4,
                       ExtensionRegistry arg5)
                throws WSDLException {
    throw new UnsupportedOperationException();
  }

  /**
   * @see javax.wsdl.extensions.ExtensionDeserializer#unmarshall(java.lang.Class, javax.xml.namespace.QName, org.w3c.dom.Element, javax.wsdl.Definition, javax.wsdl.extensions.ExtensionRegistry)
   */
  public ExtensibilityElement unmarshall(Class arg0, QName arg1, Element el,
                                         Definition def, ExtensionRegistry arg4)
                                  throws WSDLException {
    
    
    validateExtensibilityElementContext(el);
    
    PartnerLinkTypeImpl partnerLink = new PartnerLinkTypeImpl();
    partnerLink.setElementType(arg1);
    String plinkName = getAttribute(el, "name");
    if (plinkName == null) {
      InvalidBpelPartnerLinkTypeException i = new InvalidBpelPartnerLinkTypeException(
          __msgs.msgElementRequiresAttr(PARTNER_LINKTYPE.toString(),"name"));
      i.setLocation(XPathUtils.getXPathExprFromNode(el));
      throw i;
    }
    partnerLink.setName(new QName(def.getTargetNamespace(),plinkName));

    NodeList nl = el.getChildNodes();


    for (int i = 0; i < nl.getLength(); ++i) {
      if (nl.item(i).getNodeType() != Node.ELEMENT_NODE)
        continue;

      Element roleEl = (Element) nl.item(i);

      if (!roleEl.getLocalName().equals("role") || !roleEl.getNamespaceURI().equals(ROLE.getNamespaceURI()))
        continue;

      PartnerLinkTypeImpl.RoleImpl role =  parseRole(roleEl,def);

      if (partnerLink.getRole(role.getName()) != null) {
        WSDLException we = new WSDLException(WSDLException.INVALID_WSDL,
                __msgs.msgRoleAlreadyDefined(partnerLink.getName(),role.getName()));
        we.setLocation(XPathUtils.getXPathExprFromNode(roleEl));
        throw we;
      }
      partnerLink.addRole(role);
    }

    if (partnerLink.getRoles().size() == 0) {
      InvalidBpelPartnerLinkTypeException i = new InvalidBpelPartnerLinkTypeException(
          __msgs.msgMissingRoleForPartnerLinkType(partnerLink.getName().toString()));
      i.setLocation(XPathUtils.getXPathExprFromNode(el));
      throw i;
    } else if (partnerLink.getRoles().size() > 2) {
      InvalidBpelPartnerLinkTypeException i = new InvalidBpelPartnerLinkTypeException(
          __msgs.msgNoMoreThanNumberOfElements(2,ROLE.toString(),
              PARTNER_LINKTYPE.toString()));
      i.setLocation(XPathUtils.getXPathExprFromNode(el));
      throw i;
    }
    

    return partnerLink;
  }

  private PartnerLinkTypeImpl.RoleImpl parseRole(Element el, Definition def) throws WSDLException {
    PartnerLinkTypeImpl.RoleImpl role = new PartnerLinkTypeImpl.RoleImpl();

    String roleName = getAttribute(el, "name");
    if (roleName == null) {
      InvalidBpelRoleException i = new InvalidBpelRoleException(
          __msgs.msgElementRequiresAttr(ROLE.toString(),"name"));
      i.setLocation(XPathUtils.getXPathExprFromNode(el));
      throw i;
    }
    role.setName(roleName);
    
    String portType = getAttribute(el, "portType");
    if(portType == null){
    	InvalidBpelRoleException i = new InvalidBpelRoleException(
        __msgs.msgElementRequiresAttr(ROLE.toString(),"portType"));
    	i.setLocation(XPathUtils.getXPathExprFromNode(el));
      throw i;
    }
    role.setPortType(derefQName(portType, el));
    
    return role;
  }

}
