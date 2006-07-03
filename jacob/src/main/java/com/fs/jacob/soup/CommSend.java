/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.soup;

import java.lang.reflect.Method;

/**
 * Persistent store representation of a message (i.e. method application /
 * channel write) waiting for a corresponding object (i.e. channel read).
 * This structure consists of a label identifying the method that should be
 * applied to the object once it is available, and the arguments that should
 * be applied to said method.
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class CommSend extends Comm {

  private Object[] _args;
  private Method _method;

  protected CommSend() {}

  public CommSend(CommChannel chnl, Method method, Object[] args) {
    super(null, chnl);
    _args = args;
    _method = method;
  }

  public Method getMethod() {
    return _method;
  }

  /**
   * Get the arguments for the method application.
   *
   * @return array of arguments that should be applied to the method
   */
  public Object[] getArgs() {
    return _args;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer(getChannel().toString());
    buf.append(" ! ");
    buf.append(_method.getName());
    buf.append('(');
    for (int i = 0; _args != null &&  i < _args.length; ++i) {
      if (i != 0) buf.append(',');
      buf.append(_args[i]);
    }
    buf.append(')');
    return buf.toString();
  }
}
