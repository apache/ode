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

import java.io.Serializable;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

/**
 * Extensibility element that contains the xml-schema text as a string.
 */
public class XMLSchemaType implements ExtensibilityElement, Serializable {

  private static final long serialVersionUID = -5826656164421594730L;

  private static final String NS_XSD_2001 = "http://www.w3.org/2001/XMLSchema";

  /** WSDL QNAME identifier */
  public static final QName QNAME = new QName(NS_XSD_2001, "schema");

  /** For compatibility with some older classes. */
  public static final QName qname = QNAME;

  private byte[] _xmlSchema;

  public XMLSchemaType(byte[] xmlSchema) {
    super();
    _xmlSchema = xmlSchema;
  }

  /* (non-Javadoc)
   * @see javax.wsdl.extensions.ExtensibilityElement#setElementType(javax.xml.namespace.QName)
   */
  public void setElementType(QName arg0) {
  }

  /* (non-Javadoc)
   * @see javax.wsdl.extensions.ExtensibilityElement#getElementType()
   */
  public QName getElementType() {
    return QNAME;
  }

  /* (non-Javadoc)
   * @see javax.wsdl.extensions.ExtensibilityElement#setRequired(java.lang.Boolean)
   */
  public void setRequired(Boolean arg0) {
  }

  /* (non-Javadoc)
   * @see javax.wsdl.extensions.ExtensibilityElement#getRequired()
   */
  public Boolean getRequired() {
    return Boolean.FALSE;
  }

  public byte[] getXMLSchema() {
    return _xmlSchema;
  }
}
