/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

/**
 * Compiled representation of the BPEL <code>&lt;compensate&gt;</code> activity.
 */
public class OCompensate extends OActivity {
  static final long serialVersionUID = -1L  ;

  /** The scope that is compensated by this activity. */
  public OScope compensatedScope;

  public OCompensate(OProcess owner) {
    super(owner);
  }
}
