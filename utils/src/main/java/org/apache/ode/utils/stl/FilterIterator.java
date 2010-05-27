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
import java.util.NoSuchElementException;


/**
 * Filtering {@link Iterator} implementation.
 *
 * <p>
 * Created on Feb 4, 2004 at 4:48:14 PM.
 * </p>
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class FilterIterator<T> implements Iterator<T> {
  private UnaryFunction<T,Boolean> _mf;
  private Iterator<T> _i;
  private T _next;

  public FilterIterator(Iterator<T> i, UnaryFunction<T,Boolean> mf) {
    _i = i;
    _mf = mf;
  }

  public boolean hasNext() {
    return doNext();
  }

  public T next() {
    if (doNext()) {
      T ret = _next;
      _next = null;

      return ret;
    }

    throw new NoSuchElementException("No more elements.");
  }

  public void remove() {
    // Force loading the next object.
    if (doNext()) {
      _next = null;
      _i.remove();
    }

    throw new NoSuchElementException("No more elements.");
  }

  private boolean doNext() {
    if (_next != null) {
      return true;
    }

    while (_i.hasNext()) {
      T next = _i.next();

      if (_mf.apply(next) == Boolean.TRUE) {
        _next = next;

        return true;
      }
    }

    return false;
  }
}
