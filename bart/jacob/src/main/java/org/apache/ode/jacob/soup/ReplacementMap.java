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
package org.apache.ode.jacob.soup;

/**
 * Mapping used by the JACOB soup to replace large objects with (hopefully) smaller
 * objets during the serialization process. This can be used for objects that are
 * immutable during the lifetime of the JACOB VPU.
 */
public interface ReplacementMap {
  /**
   * Determine whether the given object is a replacement object obtained from this map.
   * @param obj object to test
   * @return <code>true</code> if this {@link #getReplacement(Object)} returned this object,
   *         <code>false</code> otherwise
   */
  boolean isReplacement(Object obj);

  /**
   * Get the original object for a given replacement object.
   * @param replacement
   * @return the original for the given replacement
   * @throws IllegalArgumentException if the given object is not a replacement object
   */
  Object getOriginal(Object replacement) throws IllegalArgumentException;

  /**
   * Get a replacement object for a given "original" object.
   * @param original "original" object
   * @return replacement object
   * @throws IllegalArgumentException if the map cannot generate a replacement for the object
   */
  Object getReplacement(Object original) throws IllegalArgumentException;

  /**
   * Determine whether the given object is an object for which a replacement can be obtained.
   * @param obj object to test
   * @return <code>true</code> if the call to {@link #getReplacement(Object)} will succeed,
   *         <code>false</code> otherwise
   */
  boolean isReplaceable(Object obj);
}
