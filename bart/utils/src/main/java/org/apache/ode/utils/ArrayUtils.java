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
package org.apache.ode.utils;

import java.util.Collection;

/**
 * Utility class for dealing with arrays.
 */
public class ArrayUtils {

  public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};
  public static final Class[] EMPTY_CLASS_ARRAY = new Class[]{};

  /**
   * Make a {@link Collection} out of an array.
   *
   * @param type the type of {@link Collection} to make.
   * @param elements objects to put into the collection.
   *
   * @return a {@link Collection} of the type given in the <code>type</type> argument containing <code>elements</code>
   */
  @SuppressWarnings("unchecked")
  public static <T> Collection<T> makeCollection(Class<? extends Collection> type, T[] elements) {
    if (elements == null) {
      return null;
    }

    try {
      Collection<T> c = type.newInstance();

      for (int i = 0; i < elements.length; ++i) {
        c.add(elements[i]);
      }

      return c;
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid arguments.", ex);
    }
  }

}
