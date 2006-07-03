/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools;

import org.apache.commons.logging.Log;

public class ClineCommandContext implements CommandContext {

  private Log _log;
  
  public ClineCommandContext(Log l) {
    _log = l;
  }
  
  public void outln(String s) {
    System.out.println(s);
  }

  public void out(String s) {
    System.out.print(s);
  }
  
  public void errln(String s) {
    System.err.println(s);
  }
  
  public void error(String s) {
    _log.error(s);
  }

  public void error(String s, Throwable t) {
    _log.error(s,t);
  }

  public void info(String s) {
    _log.info(s);
  }

  public void info(String s, Throwable t) {
    _log.info(s,t);
  }

  public void debug(String s, Throwable t) {
    _log.debug(s,t);
  }

  public void debug(String s) {
    _log.debug(s);
  }
  
  public void warn(String s) {
    _log.warn(s);
  }
  
  public void warn(String s, Throwable t) {
    _log.warn(s,t);
  }
}
