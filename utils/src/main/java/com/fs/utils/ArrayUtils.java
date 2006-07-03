/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils;

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
