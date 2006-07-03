/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bpel.capi.CompilerContext;
import com.fs.pxe.bpel.o.OActivity;


/**
 * Interface implemented by classes providing activity-generating logic. 
 * Implementations of this interface are used to convert an
 * activity description object ({@link com.fs.pxe.bom.impl.nodes.ActivityImpl})
 * into a <em>compiled</em> BPEL representation.
 */
public interface ActivityGenerator {
  public void setContext(CompilerContext context);

  /**
   * Generate compiled representation for the given activity definition.
   *
   * @param src activity definition
   */
  public void compile(OActivity output, Activity src);

  public OActivity newInstance(Activity src);
}
