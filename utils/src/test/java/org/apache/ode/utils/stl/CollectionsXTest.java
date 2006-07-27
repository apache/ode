/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
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
