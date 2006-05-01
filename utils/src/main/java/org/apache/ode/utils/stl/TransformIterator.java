/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.stl;

import java.util.Iterator;


/**
 * Transforming {@link Iterator} implementation; these iterators apply a unary
 * function to each object before it is returned.
 * 
 * <p>
 * Created on Feb 4, 2004 at 4:48:14 PM.
 * </p>
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class TransformIterator<E,V> implements Iterator<V> {
  private UnaryFunction<E,V> _txform;
  private Iterator<E> _i;

  public TransformIterator(Iterator<E> i, UnaryFunction<E,V> txForm) {
    _i = i;
    _txform = txForm;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public boolean hasNext() {
    return _i.hasNext();
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public V next() {
    return _txform.apply(_i.next());
  }

  /**
   * DOCUMENTME
   */
  public void remove() {
    _i.remove();
  }
}
