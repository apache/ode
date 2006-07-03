/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.wsdl;

import com.fs.pxe.bom.api.Query;
import com.fs.pxe.bom.impl.nodes.ExpressionImpl;
import com.fs.utils.DOMUtils;
import com.fs.utils.msg.MessageBundle;
import com.ibm.wsdl.util.xml.XPathUtils;

import java.io.PrintWriter;
import java.io.Serializable;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;


/**
 * WSDL serializer/deserializer for the BPEL <code>propertyAlias</code> extension.
 */
class PropertyAliasSerializer_11
        extends BaseSerializerDeserializer
        implements ExtensionDeserializer,
        ExtensionSerializer,
        Serializable {
  private static final long serialVersionUID = -1L;
  
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);
  
	/* (non-Javadoc)
   * @see javax.wsdl.extensions.ExtensionSerializer#marshall(java.lang.Class, javax.xml.namespace.QName, javax.wsdl.extensions.ExtensibilityElement, java.io.PrintWriter, javax.wsdl.Definition, javax.wsdl.extensions.ExtensionRegistry)
   */
  public void marshall(Class arg0, QName arg1, ExtensibilityElement arg2,
                       PrintWriter arg3, Definition arg4,
                       ExtensionRegistry arg5)
                throws WSDLException {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see javax.wsdl.extensions.ExtensionDeserializer#unmarshall(java.lang.Class, javax.xml.namespace.QName, org.w3c.dom.Element, javax.wsdl.Definition, javax.wsdl.extensions.ExtensionRegistry)
   */
  public ExtensibilityElement unmarshall(Class arg0, QName arg1, Element el,
                                         Definition def, ExtensionRegistry arg4)
                                  throws WSDLException {

    validateExtensibilityElementContext(el);
    
    if (!DOMUtils.isEmptyElement(el)) {
      InvalidBpelPropertyException ibpe = new InvalidBpelPropertyException(
          __msgs.msgElementMustBeEmpty(arg1.toString()));
      ibpe.setLocation(XPathUtils.getXPathExprFromNode(el));
    }
    
    PropertyAliasImpl alias = new PropertyAliasImpl();
    alias.setElementType(arg1);
    alias.setPropertyName(checkAttr(arg1, el,"propertyName"));
    alias.setMessageType(checkAttr(arg1, el,"messageType"));
    
    Message msg =def.getMessage(alias.getMessageType()); 
    if (msg == null) {
      InvalidBpelPropertyAliasException ibpae = new InvalidBpelPropertyAliasException(
          __msgs.msgNoSuchMessageTypeForPropertyAlias(alias.getMessageType().toString()));
      ibpae.setLocation(XPathUtils.getXPathExprFromNode(el));
      throw ibpae;
    }
    
    if (el.getAttributeNode("part") != null) {
      alias.setPart(el.getAttribute("part"));
      if (msg.getPart(alias.getPart()) == null) {
        InvalidBpelPropertyAliasException i = new InvalidBpelPropertyAliasException(
            __msgs.msgNoSuchPartForPropertyAlias(alias.getMessageType().toString(),
                alias.getPart()));
        i.setLocation(XPathUtils.getXPathExprFromNode(el));
        throw i;
      }
    }
    
    if (el.getAttributeNode("query") != null) {
      Query qry = new ExpressionImpl(null);
      qry.setNamespaceContext(DOMUtils.getMyNSContext(el));
      qry.setXPathString(getAttribute(el, "query"));
      qry.setLineNo(-1); // TODO: Fix location information
      alias.setQuery(qry);
    }

    alias.setNSContext(DOMUtils.getMyNSContext(el));
    
    return alias;
  }
  
  private QName checkAttr(QName qname, Element el, String attr) throws WSDLException {
    Attr att = el.getAttributeNode(attr);
    if (att == null) {
      InvalidBpelPropertyAliasException ibpae = new InvalidBpelPropertyAliasException(
          __msgs.msgElementRequiresAttr(qname.toString(),attr));
      ibpae.setLocation(XPathUtils.getXPathExprFromNode(el));
      throw ibpae;
    }
    return derefQName(att.getValue(), el);
  }
}
