/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.SequenceActivity;
import com.fs.utils.NSContext;


/**
 * BPEL object model representation of a <code>&lt;sequence&gt;</code> activity.
 */
public class SequenceActivityImpl extends CompositeActivityImpl implements SequenceActivity {

  private static final long serialVersionUID = -1L;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public SequenceActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public SequenceActivityImpl() {
    super();
  }

  /**
   * @see ActivityImpl#getType()
   */
  public String getType() {
    return "sequence";
  }
}
