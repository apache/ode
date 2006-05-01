/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.wsdl;

import org.apache.ode.utils.DOMUtils;

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


/**
 * WSDL extension to capture (read-only) the schema type info.
 *
 * @author jguinney
 */
public class XMLSchemaTypeSerializer implements ExtensionSerializer,
                                                ExtensionDeserializer,
                                                Serializable {
    
  private static final long serialVersionUID = -870479908175017298L;

  public XMLSchemaTypeSerializer() {
    super();
  }

  /**
   * Unimplemented.
   *
   * @see javax.wsdl.extensions.ExtensionSerializer#marshall(java.lang.Class,
   *      javax.xml.namespace.QName,
   *      javax.wsdl.extensions.ExtensibilityElement, java.io.PrintWriter,
   *      javax.wsdl.Definition, javax.wsdl.extensions.ExtensionRegistry)
   */
  public void marshall(Class clazz, QName qname, ExtensibilityElement element,
                       PrintWriter writer, Definition definition,
                       ExtensionRegistry extensionRegistry)
                throws WSDLException {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a {@link XMLSchemaType}
   *
   * @see javax.wsdl.extensions.ExtensionDeserializer#unmarshall(java.lang.Class,
   *      javax.xml.namespace.QName, org.w3c.dom.Element,
   *      javax.wsdl.Definition, javax.wsdl.extensions.ExtensionRegistry)
   */
  public ExtensibilityElement unmarshall(Class clazz, QName qname, Element element,
                                         Definition definition,
                                         ExtensionRegistry extensionRegistry)
                                  throws WSDLException {
    DOMUtils.pancakeNamespaces(element);
    return new XMLSchemaType(DOMUtils.domToString(element));
  }
}
