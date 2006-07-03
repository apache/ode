/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.stl;

import java.util.Enumeration;
import java.util.Iterator;


/**
 * Adapter class, adapting <code>Enumeration</code> objects to the
 * <code>Iterator</code> interface.
 */
public class EnumerationIterator<T> implements Iterator<T> {
  private Enumeration<T> _enum;

  public EnumerationIterator(Enumeration<T> e) {
    _enum = e;
  }

  public boolean hasNext() {
    return _enum.hasMoreElements();
  }

  public T next() {
    return _enum.nextElement();
  }

  public void remove() {
    throw new UnsupportedOperationException("Method remove() not supported.");
  }

}
