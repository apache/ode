/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;


/**
 * Test of {@link NamespaceContextImpl}
 * 
 * <p>
 * Created on Feb 4, 2004 at 6:08:26 PM.
 * </p>
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class NamespaceContextImplTest extends TestCase {
  NSContext nc;

  /**
   * DOCUMENTME
   */
  public void testGetNamespaceUri() {
    assertEquals("uri1", nc.getNamespaceURI("p1"));
    assertEquals("uri2", nc.getNamespaceURI("p2"));
    assertEquals("uri1", nc.getNamespaceURI("p1.1"));
    assertNull(nc.getNamespaceURI("foobar"));
  }

  /**
   * DOCUMENTME
   */
  public void testGetPrefix() {
    assertEquals("p2", nc.getPrefix("uri2"));
    assertEquals("p1", nc.getPrefix("uri1").substring(0, 2));
  }

  /**
   * DOCUMENTME
   */
  public void testGetPrefixSet() {
    assertEquals(3, nc.getPrefixes().size());
    assertTrue(nc.getPrefixes().contains("p1"));
    assertTrue(nc.getPrefixes().contains("p1.1"));
    assertTrue(nc.getPrefixes().contains("p2"));
  }

  /**
   * DOCUMENTME
   */
  public void testGetPrefixes() {
    Iterator i = nc.getPrefixes("uri1");
    assertTrue(i.hasNext());
    assertTrue(i.hasNext());
    assertTrue(i.hasNext());
    assertTrue(i.hasNext());
    assertTrue(i.hasNext());
    assertEquals("p1", i.next().toString().substring(0, 2));
    assertTrue(i.hasNext());
    assertEquals("p1", i.next().toString().substring(0, 2));
    assertFalse(i.hasNext());

    try {
      i.next();
      fail("should have thrown ex");
    } catch (NoSuchElementException nse) {
      //OK
    }
  }

  /**
   * DOCUMENTME
   */
  public void testGetUriSet() {
    assertEquals(2, nc.getUriSet().size());
    assertTrue(nc.getUriSet().contains("uri1"));
    assertTrue(nc.getUriSet().contains("uri2"));
  }

  /**
   * DOCUMENTME
   *
   * @throws Exception DOCUMENTME
   */
  public void testSerialization()
                         throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(nc);
    oos.close();

    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(bis);
    NSContext nc2 = (NSContext)ois.readObject();
    assertEquals(nc.getUriSet(), nc2.getUriSet());
    assertEquals(nc.getPrefixes(), nc2.getPrefixes());
    assertEquals(nc.getNamespaceURI("p1"), nc2.getNamespaceURI("p1"));
    assertEquals(nc.getNamespaceURI("p1.1"), nc2.getNamespaceURI("p1.1"));
    assertEquals(nc.getNamespaceURI("p2"), nc2.getNamespaceURI("p2"));
  }

  protected void setUp()
                throws Exception {
    nc = new NSContext();
    nc.register("p1", "uri1");
    nc.register("p2", "uri2");
    nc.register("p1.1", "uri1");
  }
}
