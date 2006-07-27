/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.wsdl;

import org.apache.ode.bom.api.Query;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

/**
 * Representation of a BPEL <code>propertyAlias</code> declaration.
 */
public interface PropertyAlias extends ExtensibilityElement{
  
  QName getMessageType();

  String getPart();

  QName getPropertyName();

  void setPropertyName(QName propertyName);

  Query getQuery();
}

