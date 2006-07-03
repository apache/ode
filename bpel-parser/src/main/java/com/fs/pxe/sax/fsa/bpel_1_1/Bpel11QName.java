/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import javax.xml.namespace.QName;

import com.fs.pxe.bpel.parser.BpelProcessBuilder;

class Bpel11QName extends QName {

  public Bpel11QName(String local) {
    super(BpelProcessBuilder.BPEL4WS_NS,local);
  }

}
