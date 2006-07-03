/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.stl;

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorEnumeration<T> implements Enumeration<T> {
  private Iterator<T> _iter;

  public IteratorEnumeration(Iterator<T> i) {
    _iter = i;
  }

  public boolean hasMoreElements() {
    return _iter.hasNext();
  }

  public T nextElement() {
    return _iter.next();
  }

}
