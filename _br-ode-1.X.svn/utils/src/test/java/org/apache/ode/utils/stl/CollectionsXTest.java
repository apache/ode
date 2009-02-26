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

import junit.framework.TestCase;

public class CollectionsXTest extends TestCase {

  public void testFilterByMemberOfFunction() {
    List<Object> src = Arrays.asList(new Object(), "foo", 42, "bar");
    Collection<Object> result = new ArrayList<Object>();
    CollectionsX.filter(result, src, new MemberOfFunction<Object>() {
      public boolean isMember(Object o) {
        return o.getClass().equals(String.class);
      }
    });
    assertEquals(2, result.size());
    Iterator results = result.iterator();
    assertEquals("foo", results.next());
    assertEquals("bar", results.next());
  }

  public void testFilterIteratorByClass() {
    List<Object> src = Arrays.asList(new Object(), "foo", 42, "bar");
    Collection<String> result = new ArrayList<String>();
    CollectionsX.filter(result, src.iterator(), String.class);
    assertEquals(2, result.size());
    Iterator<String> results = result.iterator();
    assertEquals("foo", results.next());
    assertEquals("bar", results.next());
  }

  public void testFilterByClass() {
    List<Object> src = Arrays.asList(new Object(), "foo", 42, "bar");
    Collection<String> result = CollectionsX.filter(src, String.class);
    assertEquals(2, result.size());
    Iterator<String> results = result.iterator();
    assertEquals("foo", results.next());
    assertEquals("bar", results.next());
  }

  public void testFilterByClassNoResult() {
    List<Object> src = Arrays.asList(new Object(), "foo", 42, "bar");
    Collection<Double> noresult = CollectionsX.filter(src, Double.class);
    assertNotNull(noresult);
    assertTrue(noresult.isEmpty());
  }

}
