/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.wsdl;

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

  private String _xmlSchema;

  public XMLSchemaType(String xmlSchema) {
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

  /**
   * Returns the schema content as string.
   *
   * @return
   */
  public String getXMLSchema() {
    return _xmlSchema;
  }
}
