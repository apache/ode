/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.fs;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

public class FileUtilsTest extends TestCase {

  public void testDirectoryEntriesInPathWrongArg() {
    try {
      FileUtils.directoryEntriesInPath(null);
      fail("expected IllegalArgumentException");
    }
    catch (IllegalArgumentException iex) {
      // expected
    }

    try {
      FileUtils.directoryEntriesInPath(new File("does/not/exist"));
      fail("expected IllegalArgumentException");
    }
    catch (IllegalArgumentException iex) {
      // expected
    }
  }

  public void testDirectoryEntriesInPathWithFile() throws IOException {
    File tmpFile = File.createTempFile("file", "tmp");
    assertNotNull(tmpFile);
    tmpFile.deleteOnExit();

    List<File> collectedFiles = FileUtils.directoryEntriesInPath(tmpFile);
    assertEquals(1, collectedFiles.size());
    assertEquals(tmpFile.getName(), collectedFiles.iterator().next().getName());
  }

  public void testDirectoryEntriesInPathWithDirectory() throws IOException {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"), "root.tmp");
    assertTrue(tmpDir.mkdir());
    tmpDir.deleteOnExit();

    File tmpFile = File.createTempFile("file", "tmp", tmpDir);
    assertNotNull(tmpFile);
    tmpFile.deleteOnExit();

    List<File> collectedFiles = FileUtils.directoryEntriesInPath(tmpDir);
    assertEquals(2, collectedFiles.size());

    Iterator<File> fi = collectedFiles.iterator();
    assertEquals(tmpDir.getName(), fi.next().getName());
    assertEquals(tmpFile.getName(), fi.next().getName());
  }

}
