/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.elang.xpath10.compiler;

import com.fs.pxe.bpel.capi.CompilationException;

import org.jaxen.JaxenException;


/**
 * Jaxen-compliant wrapper for {@link com.fs.pxe.bpel.capi.CompilationException}.
 */
class CompilationExceptionWrapper extends JaxenException {

  public CompilationExceptionWrapper(CompilationException cause) {
    super(cause);
    assert getCompilationException() != null;
  }

  public CompilationException getCompilationException() {
    return (CompilationException) getCause();
  }
}
