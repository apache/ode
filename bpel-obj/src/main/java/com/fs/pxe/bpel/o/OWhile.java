/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

/**
 * Compiled representation of a <code>&lt;while&gt;</code> activity.
 */
public class OWhile extends OActivity {

  static final long serialVersionUID = -1L  ;
  /** The while condition. */
  public OExpression whileCondition;

  public OActivity activity;

  public OWhile(OProcess owner) {
    super(owner);
  }
}
