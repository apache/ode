/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

import javax.xml.namespace.QName;

/**
 * Compiled representation of the BPEL <code>&lt;throw&gt;</code> activity.
 */
public class OThrow extends OActivity {
  
  static final long serialVersionUID = -1L  ;
  public OScope.Variable faultVariable;
  public QName faultName;

  public OThrow(OProcess owner) {
    super(owner);
  }
}
