/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.capi.CompilerContext;

/**
 * Base implementation of the {@link ActivityGenerator} interface.
 */
abstract class DefaultActivityGenerator implements ActivityGenerator {
  protected CompilerContext _context;

  public void setContext(CompilerContext context) {
    _context = context;
  }

}
