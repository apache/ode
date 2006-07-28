/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
