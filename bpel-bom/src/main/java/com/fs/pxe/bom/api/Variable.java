/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

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
