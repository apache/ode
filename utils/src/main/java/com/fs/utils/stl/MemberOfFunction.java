/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.stl;

/**
 * Interface used for defining object filters/selectors, classes that are used
 * to determine whether a given object belong in a set.
 * 
 * <p>
 * Created on Feb 4, 2004 at 4:48:55 PM.
 * </p>
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public abstract class MemberOfFunction<E> implements UnaryFunction<E,Boolean> {
  /**
   * A unary function that tests whether an element is the member of a set.
   * @param o element to test
   * @return <code>true</code> if element is a member
   */
  public abstract boolean isMember(E o);

  /**
   * Implementation of {@link UnaryFunction} method defering to
   * {@link #isMember(E)}.
   * @param x element to test
   * @return {@link Boolean.TRUE} if isMemeber returns <code>true</code>,
   *         <code>false</code> otherwise
   */
  public final Boolean apply(E x) {
    return isMember(x)
           ? Boolean.TRUE
           : Boolean.FALSE;
  }
}
