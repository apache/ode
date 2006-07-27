/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

import javax.xml.namespace.QName;

/**
 * Assignment L/R-value defined in terms of a BPEL property.
 */
public interface PropertyVal extends From, To {
  String getVariable();

  void setVariable(String variable);

  QName getProperty();

  void setProperty(QName property);
}
