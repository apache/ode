/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Compiled representation of a BPEL fault handler.
 */
public class OFaultHandler extends OBase {
  
  static final long serialVersionUID = -1L  ;
  
  public final List<OCatch> catchBlocks = new ArrayList<OCatch>();

  public OFaultHandler(OProcess owner) {
    super(owner);
  }

  public Collection<OLink> outgoinglinks() {
    throw new UnsupportedOperationException(); // TODO: implement me!
  }
}
