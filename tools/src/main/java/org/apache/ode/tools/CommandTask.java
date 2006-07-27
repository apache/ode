/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.tools;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;


/**
 * Base <code>Task</code> that implements <code>CommandContext</code>.  This is
 * extended by other <code>Task</code> implementations that serve as
 * <code>Command</code> wrappers.
 */
public abstract class CommandTask extends Task implements CommandContext {

  public void outln(String s) {
    handleOutput(s);
  }

  public void out(String s) {
    handleOutput(s);
  }
  
  public void errln(String s) {
    handleErrorOutput(s);
  }

  public void error(String s) {
    log(s,Project.MSG_ERR);
  }

  public void error(String s, Throwable t) {
    log(s,Project.MSG_ERR);
  }

  public void warn(String s, Throwable t) {
    log(s,Project.MSG_WARN);
  }

  public void warn(String s) {
    log(s,Project.MSG_WARN);
  }

  public void info(String s) {
    log(s,Project.MSG_INFO);
  }

  public void info(String s, Throwable t) {
    log(s,Project.MSG_INFO);
  }

  public void debug(String s, Throwable t) {
    log(s,Project.MSG_VERBOSE);
  }

  public void debug(String s) {
    log(s,Project.MSG_VERBOSE);
  }  
}
