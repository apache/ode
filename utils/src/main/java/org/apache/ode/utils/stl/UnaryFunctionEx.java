/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.stl;

public interface UnaryFunctionEx<E,V> {
  V apply(E x) throws Exception;
}
