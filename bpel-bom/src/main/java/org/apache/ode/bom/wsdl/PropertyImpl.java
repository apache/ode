/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.wsdl;

import java.io.Serializable;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;


/**
 * Interim object representation of a BPEL <code>&lt;property</code> element.
 */
class PropertyImpl implements org.apache.ode.bom.wsdl.Property, ExtensibilityElement, Serializable {
	
	private static final long serialVersionUID = -1L;

  private QName _name;
  private QName _propertyType;
  private QName _elementType;
  
  /**
   * @see javax.wsdl.extensions.ExtensibilityElement#setElementType(javax.xml.namespace.QName)
   */
  public void setElementType(QName arg0) {
    _elementType = arg0;
  }

  /**
   * @see javax.wsdl.extensions.ExtensibilityElement#getElementType()
   */
  public QName getElementType() {
    return _elementType;
  }

  /**
   * Set the name of this property for reference.
   * @param name the <code>QName</code> of the property
   */
  public void setName(QName name) {
    _name = name;
  }

  /**
   * Get the name of the property.
   * @return the <code>QName</code> of the property.
   */
  public QName getName() {
    return _name;
  }

  /**
   * Set the name of the schema type for this property.
   * @param name the <code>QName</code> of the type.
   */
  public void setPropertyType(QName name) {
    _propertyType = name;
  }

  /**
   * Get the name of the schema type for this property
   * @return the <code>QName</code> for the schema type of this property.
   */
  public QName getPropertyType() {
    return _propertyType;
  }

  /**
   * @see javax.wsdl.extensions.ExtensibilityElement#setRequired(java.lang.Boolean)
   */
  public void setRequired(Boolean arg0) {
  }

  /**
   * @see javax.wsdl.extensions.ExtensibilityElement#getRequired()
   */
  public Boolean getRequired() {
    return Boolean.FALSE;
  }
  
}
