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
package org.apache.ode.bom.api;

import javax.xml.namespace.QName;

/**
 * BPEL Variable declaration.
 */
public interface Variable extends BpelObject {

  public static final short TYPE_SCHEMA = 0;
  public static final short TYPE_ELEMENT = 1;
  public static final short TYPE_MESSAGE = 2;

  /**
   * Get the name of the variable.
   *
   * @return variable name
   */
  String getName();

  /**
   * Set the name of the variable.
   *
   * @param varName variable name
   */
  void setName(String varName);

  /**
   * Get the scope-like construct in which this variable was declared.
   *
   * @return declaring scope
   */
  Scope getDeclaringScope();

  /**
   * Set the type of this variable to a WSDL message type.
   *
   * @param messageType message type name
   */
  void setMessageType(QName messageType);

  /**
   * Set the type of this variable to a XML schema type.
   *
   * @param schemaType XML schema type name
   */
  void setSchemaType(QName schemaType);

  /**
   * Set the type of this variable to XML element type.
   *
   * @param elementType XML element name
   */
  void setElementType(QName elementType);

  /**
   * Get the type name of this variable.
   *
   * @return an XML element, XML schema type, or WSDL message type name.
   */
  QName getTypeName();

  /**
   * Get the type of declaration; one of: {@link #TYPE_SCHEMA}, {@link #TYPE_ELEMENT},
   * or {@link #TYPE_MESSAGE}.
   *
   * @return type of variable decleration
   */
  short getDeclerationType();

}
