/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.PropertyVal;
import org.apache.ode.utils.NSContext;

import javax.xml.namespace.QName;

public class PropertyValImpl extends BpelObjectImpl implements PropertyVal {
  private static final long serialVersionUID = 1L;

  private String _variable;
  private QName _property;

  public PropertyValImpl(NSContext nsContext) {
    super(nsContext);
  }

  public String getVariable() {
    return _variable;
  }

  public void setVariable(String variable) {
    _variable = variable;
  }

  public QName getProperty() {
    return _property;
  }

  public void setProperty(QName property) {
    _property = property;
  }

}
