/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

public class CompensationHandlerRegistered extends ScopeEvent {
	private static final long serialVersionUID = 1L;

  public TYPE getType() {
    return TYPE.scopeHandling;
  }
}
