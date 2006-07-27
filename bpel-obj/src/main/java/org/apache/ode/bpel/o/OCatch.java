/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import javax.xml.namespace.QName;

/**
 * The catch pseudo-activity.
 */
public final class OCatch extends OScope {
  static final long serialVersionUID = -1L  ;
  public QName faultName;
  public OScope.Variable faultVariable;

  

  public OCatch(OProcess owner) {
    super(owner);
  }

  public String toString() {
    return "{OCatch faultName=" + faultName + ", faultVariable=" + faultVariable + "}";
  }
}
