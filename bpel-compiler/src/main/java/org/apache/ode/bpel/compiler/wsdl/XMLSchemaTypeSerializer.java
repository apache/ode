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
package org.apache.ode.bpel.compiler.wsdl;

import org.apache.ode.utils.DOMUtils;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

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
    try {
        // xml dump is encoded in UTF-8, so the byte array should use the same encoding
        // the reading xml parser should be able to correctly detect the encoding then.
        return new XMLSchemaType(DOMUtils.domToString(element).getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
        throw new WSDLException(WSDLException.OTHER_ERROR, e.getMessage(), e);
    }
  }
}
