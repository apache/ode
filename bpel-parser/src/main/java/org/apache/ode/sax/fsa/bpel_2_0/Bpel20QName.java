/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bpel.parser.BpelProcessBuilder;

import javax.xml.namespace.QName;

class Bpel20QName extends QName {
  
  private static final long serialVersionUID = 1L;

	public Bpel20QName(String local) {
    super(BpelProcessBuilder.WSBPEL2_0_NS,local);
  }
}
