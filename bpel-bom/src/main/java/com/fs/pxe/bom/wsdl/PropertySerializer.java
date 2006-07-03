/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.wsdl;

import com.fs.utils.DOMUtils;
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
