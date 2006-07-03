/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.wsdl;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

/**
 * BPEL Object Model (BOM) representation of a property declaration.
 */
public interface Property  extends ExtensibilityElement{
  
  /**
   * Get the name of the property.
   *
   * @return property name (namespace qualified)
   */
  QName getName();

  /**
   * Set the name of the property.
   * @param qName property name
   */
  void setName(QName qName);

  /**
   * Get the data type of the property.
   *
   * @return property type (XMLschema type, namespace qualified)
   */
  QName getPropertyType();

  /**
   * Set the data type of the property.
   * @param propertyType property type
   */
  void setPropertyType(QName propertyType);

}
