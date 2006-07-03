/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

import java.io.Serializable;

/**
 * Information about the source that was used to create a compiled object.
 */
public class DebugInfo implements Serializable {
  static final long serialVersionUID = -1L  ;

  /** Source file / resource name. */
  public final String sourceURI;

  /** Source line number (start). */
  public final int startLine;

  /** Source line number (end). */
  public final int endLine;

  public String description;

  public DebugInfo(String sourceURI, int startLine, int endLine) {
    this.sourceURI = sourceURI;
    this.startLine = startLine;
    this.endLine = endLine;
  }

  public DebugInfo(String sourceURI, int line) {
    this(sourceURI, line, line);
  }

}
