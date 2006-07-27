/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.tools;

public interface Command {
  
  public void execute(CommandContext cc) throws ExecutionException;
}
