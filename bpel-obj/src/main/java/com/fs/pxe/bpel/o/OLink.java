/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

/**
 * Compiled representation of a BPEL control link.
 */
public class OLink extends OBase {
  static final long serialVersionUID = -1L  ;
  
  /** The flow in which the link is declared. */
  public OFlow declaringFlow;

  /** The name of the link. */
  public String name;

  /** The link's transition condition. */
  public OExpression transitionCondition;

  /** The source activity. */
  public OActivity source;

  /** The target activity. */
  public OActivity target;

  public OLink(OProcess owner) {
    super(owner);
  }
}
