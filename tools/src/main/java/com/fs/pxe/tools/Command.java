/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools;

public interface Command {
  
  public void execute(CommandContext cc) throws ExecutionException;
}
