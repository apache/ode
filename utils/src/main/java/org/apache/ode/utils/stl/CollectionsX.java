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

import java.util.*;

/**
 * Useful extensions to the java.util.Collections class.
 */
public class CollectionsX {

    public static UnaryFunction<Map.Entry, Object> ufnMapEntry_getKey = new UnaryFunction<Map.Entry, Object>() {
        public Object apply(Map.Entry x) {
            return x.getKey();
        }
    };

    public static UnaryFunction<Map.Entry, Object> ufnMapEntry_getValue = new UnaryFunction<Map.Entry, Object>() {
        public Object apply(Map.Entry x) {
            return x.getValue();
        }
    };

    public static <T> void apply(Collection<T> coll, UnaryFunction<T, ?> f) {
        apply(coll.iterator(), f);
    }

    public static <T> void apply(Iterator<T> i, UnaryFunction<T, ?> f) {
        while (i.hasNext()) {
            f.apply(i.next());
        }
    }

    public static <T> void apply(Collection<T> coll, UnaryFunctionEx<T, ?> f) throws Exception {
        apply(coll.iterator(), f);
    }

    public static <T> void apply(Iterator<T> i, UnaryFunctionEx<T, ?> f) throws Exception {
        while (i.hasNext()) {
            f.apply(i.next());
        }
    }

    /**
     * Find an element in a colletion satisfying a condition. The condition is
     * given in the form of a unary function which returns a non-<code>false</code>
     * value when the condition is satisfied. The first object in the collection
     * matching the condition is returned.
     * 
     * @param coll
     *            the collection to search through
     * @param f
     *            the test to apply to the collection elements
     * 
     * @return the first object in the collection (coll) which, satisfies the
     *         condition (f)
     */
    public static <T> T find_if(Collection<T> coll, MemberOfFunction<? super T> f) {
        return find_if(coll.iterator(), f);
    }

    /**
     * Find an element in a collection satisfying a condition.
     * 
     * @param i
     *            the iterator to iterate with
     * @param f
     *            the test to apply to the elements
     * 
     * @return the first object enumerated by the iterator (i) which satisfies
     *         the condition (f)
     * 
     * @see #find_if(java.util.Collection,
     *      org.apache.ode.utils.stl.MemberOfFunction)
     */
    public static <T> T find_if(Iterator<T> i, MemberOfFunction<? super T> f) {
        while (i.hasNext()) {
            T x = i.next();

            if (f.isMember(x)) {
                return x;
            }
        }

        return null;
    }

    public static <T> Collection<T> insert(Collection<T> coll, Enumeration<? extends T> e) {
        while (e.hasMoreElements()) {
            coll.add(e.nextElement());
        }
        return coll;
    }

    public static <T> Collection<T> insert(Collection<T> coll, Iterator<? extends T> i) {
        while (i.hasNext()) {
            coll.add(i.next());
        }
        return coll;
    }

    public static <T> Collection<T> insert(Collection<T> coll, Collection<? extends T> src) {
        return insert(coll, src.iterator());
    }

    /**
     * Remove elements from collection based on the results of specified unary
     * function. An element will be deleted if <code>f.isMember(element)
     * </code>
     * returns <code>true</code>. So: <em>coll' = { x : x el-of coll
     * AND f(x) == false }</em>
     * 
     * @param coll
     *            the collection from which to remove elements
     * @param f
     *            the function to apply
     * 
     * @return coll, for convenience
     */
    public static <T> Collection<T> remove_if(Collection<T> coll, MemberOfFunction<T> f) {
        Iterator<T> i = coll.iterator();

        while (i.hasNext()) {
            if (f.isMember(i.next())) {
                i.remove();
            }
        }

        return coll;
    }

    /**
     * Transform a collection with a unary function. Roughly speaking dest = {
     * f(a) : a el-of src }
     * 
     * @param dest
     *            the empty (mutable) collection to transform into
     * @param src
     *            the collection to transform from
     * @param f
     *            the unary function to apply
     * 
     * @return dest, for convenience
     */
    public static <C extends Collection<T>, T, V extends T, E> C transform(C dest, Collection<E> src,
            UnaryFunction<E, V> f) {
        Iterator<E> i = src.iterator();

        while (i.hasNext()) {
            dest.add(f.apply(i.next()));
        }

        return dest;
    }

    public static <C extends Collection<T>, T, V extends T, E> C transformEx(C dest, Collection<E> src,
            UnaryFunctionEx<E, V> f) throws Exception {
        Iterator<E> i = src.iterator();

        while (i.hasNext()) {
            dest.add(f.apply(i.next()));
        }

        return dest;
    }

    public static <C extends Collection<T>, T, V extends T, E> C transform(C dest, Enumeration<E> i,
            UnaryFunction<E, V> f) {
        while (i.hasMoreElements()) {
            dest.add(f.apply(i.nextElement()));
        }
        return dest;
    }

    public static <C extends Collection<T>, T, S extends T> C filter(C dest, Collection<S> source,
            MemberOfFunction<S> function) {
        return filter(dest, source.iterator(), function);
    }

    public static <C extends Collection<T>, T, S extends T> C filter(C dest, Iterator<S> source,
            MemberOfFunction<S> function) {
        while (source.hasNext()) {
            S next = source.next();
            if (function.isMember(next)) {
                dest.add(next);
            }
        }
        return dest;
    }

    public static <C extends Collection<T>, S, T extends S> C filter(C dest, Collection<S> src, Class<T> t) {
        return filter(dest, src.iterator(), t);
    }

    public static <C extends Collection<T>, S, T extends S> C filter(C newList, Iterator<S> iterator, Class<T> t) {
        while (iterator.hasNext()) {
            S next = iterator.next();
            if (t.isAssignableFrom(next.getClass())) {
                newList.add((T) next);
            }
        }
        return newList;
    }

    /**
     * Filter a collection by member class.
     * 
     * @param src
     *            source collection
     * @param aClass
     *            requested class
     * @return collection consisting of the members of the input that are
     *         assignable to the given class
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> filter(Collection src, final Class<T> aClass) {
        return filter(new ArrayList<T>(src.size()), src.iterator(), aClass);
    }

}
