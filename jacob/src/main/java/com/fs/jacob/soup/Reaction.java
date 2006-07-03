/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.soup;

import java.lang.reflect.Method;

import com.fs.jacob.JavaClosure;
import com.fs.utils.ObjectPrinter;

/**
 * DOCUMENTME.
 * <p>Created on Feb 16, 2004 at 9:23:40 PM.</p>
 * 
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class Reaction extends SoupObject {
  private JavaClosure _closure;
  private Method _method;
  private Object[] _args;

  public Reaction(JavaClosure target, Method method, Object[] args) {
    _closure = target;
    _method = method;
    _args = args;
  }

  public JavaClosure getClosure() {
    return _closure;
  }

  public Method getMethod() {
    return _method;
  }

  public Object[] getArgs() {
    return _args;
  }

  public String toString () {
    return ObjectPrinter.toString(this, new Object[] { "closure", _closure, "method", _method.getName(), "args", _args});
  }

}
