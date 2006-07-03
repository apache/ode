/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.utils.stl;

public class CompositeUnaryFunction<E,V,T> implements UnaryFunction<E,V> {
  private UnaryFunction<T,V> _f;
  private UnaryFunction<E,T> _g;

  public CompositeUnaryFunction(UnaryFunction<T,V> f, UnaryFunction<E,T> g) {
    _f = f;
    _g = g; 
  }

  public V apply(E x) {
    return _f.apply(_g.apply(x));
  }

}
