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
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class for dealing with arrays.
 */
public class CollectionUtils {

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};
    public static final Class[] EMPTY_CLASS_ARRAY = new Class[]{};

    /**
     * Make a {@link Collection} out of an array.
     *
     * @param type     the type of {@link Collection} to make.
     * @param elements objects to put into the collection.
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

    /**
     * Compares the two specified maps for equality.  Returns
     * <tt>true</tt> if the two maps represent the same mappings.  More formally, two maps <tt>m1</tt> and
     * <tt>m2</tt> represent the same mappings if
     * <tt>m1.keySet().equals(m2.keySet())</tt> and for every key <tt>k</tt>
     * in <tt>m1.keySet()</tt>, <tt> (m1.get(k)==null ? m2.get(k)==null :
     * m1.get(k).equals(m2.get(k))) </tt>.
     * <p/>
     * This implementation first checks if the <tt>m1</tt> and <tt>m2</tt> are the same object;
     * if so it returns <tt>true</tt>.  Then, it checks if the two maps have the same sizw; if
     * not, it returns <tt>false</tt>.  If so, it iterates over <tt>m1</tt>'s
     * <tt>entrySet</tt> collection, and checks that map <tt>m1</tt>
     * contains each mapping that map <tt>m2</tt> contains.  If map <tt>m1</tt>
     * fails to contain such a mapping, <tt>false</tt> is returned.  If the
     * iteration completes, <tt>true</tt> is returned.
     *
     * @return <tt>true</tt> if the specified object is equal to this map.
     */
    public static boolean equals(Map m1, Map m2) {
        if (m2 == m1) return true;
        if (m1 == null) return false;
        if (m2 == null) return false;
        if (m2.size() != m1.size()) return false;

        try {
            for (Iterator it = m1.entrySet().iterator(); it.hasNext();) {
                Map.Entry e = (Map.Entry) it.next();
                Object key = e.getKey();
                Object value = e.getValue();
                if (value == null) {
                    if (!(m2.get(key) == null && m2.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m2.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
        return true;
    }

}
