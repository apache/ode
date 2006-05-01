/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Query;
import org.apache.ode.bom.api.VariableVal;

public class VariableValImpl extends BpelObjectImpl implements VariableVal {
  private static final long serialVersionUID = 1L;

  private String _variable;
  private String _part;
  private Query _location;

  public String getVariable() {
    return _variable;
  }

  public void setVariable(String variable) {
    _variable = variable;
  }

  public String getPart() {
    return _part;
  }

  public void setPart(String part) {
    _part = part;
  }

  public Query getLocation() {
    return _location;
  }

  public void setLocation(Query location) {
    _location = location;
  }
}
