/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bpel.capi.CompilerContext;
import org.apache.ode.bpel.o.OActivity;


/**
 * Interface implemented by classes providing activity-generating logic. 
 * Implementations of this interface are used to convert an
 * activity description object ({@link org.apache.ode.bom.impl.nodes.ActivityImpl})
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
