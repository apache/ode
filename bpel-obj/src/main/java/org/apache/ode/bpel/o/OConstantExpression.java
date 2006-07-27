/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

public class OConstantExpression extends OExpression {
  static final long serialVersionUID = -1L  ;

  private Object _val;

  public OConstantExpression(OProcess owner, Object val) {
    super(owner);
    setVal(val);
  }

  public Object getVal() {
    return _val;
  }

  public void setVal(Object val) {
    if (val == null)
      throw new IllegalArgumentException("OConstatExpression cannot be null.");

    this._val = val;
  }

  public String toString() {
    return "{OConstantExpression " + _val  + "}";
  }
}
