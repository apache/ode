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

import org.apache.ode.utils.DOMUtils;
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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;


/**
 * WSDL serializer/deserializer for the BPEL <code>property</code> WSDL extension.
 */
class PropertySerializer
        extends BaseSerializerDeserializer
        implements ExtensionDeserializer, ExtensionSerializer, Serializable {

  private static final long serialVersionUID = -1L;

	/* (non-Javadoc)
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
  public ExtensibilityElement unmarshall(Class arg0, QName qn, Element el,
                                         Definition def, ExtensionRegistry er)
                                  throws WSDLException {
    
    validateExtensibilityElementContext(el);
    
    if (!DOMUtils.isEmptyElement(el)) {
      InvalidBpelPropertyException ibpe = new InvalidBpelPropertyException(
          __msgs.msgElementMustBeEmpty(qn.toString()));
      ibpe.setLocation(XPathUtils.getXPathExprFromNode(el));
    }
    
    PropertyImpl property = new PropertyImpl();
    property.setElementType(qn);

    Attr name = el.getAttributeNode("name");
    if (name == null) {
      InvalidBpelPropertyException ibpe = new InvalidBpelPropertyException(
          __msgs.msgElementRequiresAttr(qn.toString(),"name"));
      ibpe.setLocation(XPathUtils.getXPathExprFromNode(el));
      throw ibpe;
    }
    
    property.setName(new QName(def.getTargetNamespace(), name.getValue()));
    
    Attr type = el.getAttributeNode("type");
    if (type == null) {
      InvalidBpelPropertyException ibpe = new InvalidBpelPropertyException(
          __msgs.msgElementRequiresAttr(qn.toString(),"type"));
      ibpe.setLocation(XPathUtils.getXPathExprFromNode(el));
      throw ibpe;
    }
    property.setPropertyType(derefQName(type.getValue(), el));
    
    return property;
  }


}
