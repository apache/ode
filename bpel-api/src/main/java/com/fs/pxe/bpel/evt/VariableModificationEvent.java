/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

public class VariableModificationEvent extends VariableEvent {
  private static final long serialVersionUID = 1L;
  public VariableModificationEvent() {
    super();
  }

  public VariableModificationEvent(String varName) {
    super(varName);
  }
}
