/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.stl;

/**
 * Equality comparison unary function, compares against a constant value.
 * 
 * <p>
 * Created on Feb 4, 2004 at 5:15:38 PM.
 * </p>
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class EqualsUnaryFunction<T> extends MemberOfFunction {
  private T _c;

  public EqualsUnaryFunction(T c) {
    _c = c;
  }

  public boolean isMember(Object x) {
    return _c.equals(x);
  }

}
