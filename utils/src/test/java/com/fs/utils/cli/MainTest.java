/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.cli;

import com.fs.utils.cli.Main;

import junit.framework.TestCase;

/**
 * Test of the {@link Main} bootstrap class.
 */
public class MainTest extends TestCase {

  static MainTest _self;
  public void setUp() {
    _self = this;
  }

  public void testMain() throws Throwable {
    Main.main(new String[] { getClass().getResource("test.cfg").getPath() , "foo", "bar"} );
  }


  void fromMain(Class cls, String[] args) {

    assertEquals(2, args.length);
    assertEquals("foo", args[0]);
    assertEquals("bar", args[1]);


  }
}
