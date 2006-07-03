/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.utils.fs;

import com.fs.utils.SystemUtils;

import java.io.File;

import junit.framework.TestCase;

public class TempFileManagerTest extends TestCase {

  private String DEFAULT_TEMPDIR_PATH = null;

  public TempFileManagerTest(String s) throws Exception {
    super(s);
    DEFAULT_TEMPDIR_PATH = new File(SystemUtils.javaTemporaryDirectory()).getCanonicalPath();
  }

  protected void setUp() throws Exception {
    // set default working directory
    TempFileManager.setWorkingDirectory(null);
    super.setUp();
  }

  protected void tearDown() throws Exception {
    TempFileManager.cleanup();
    super.tearDown();
  }

  public void testSetWorkingDirectoryTwice() throws Exception {
    File tmpDir = new File(DEFAULT_TEMPDIR_PATH);

    // this should work (no instance yet)
    TempFileManager.setWorkingDirectory(tmpDir);

    // trigger instance creation
    File managedDir = TempFileManager.getTemporaryDirectory("fooDir");
    assertTrue(managedDir.getCanonicalPath().contains(tmpDir.getCanonicalPath()));

    // now fail
    try {
      TempFileManager.setWorkingDirectory(tmpDir);
      fail();
    }
    catch (IllegalStateException ise) {
      // expected
    }
  }

  public void testSetWorkingDirectory() throws Exception {
    File tmpDir = File.createTempFile(TempFileManagerTest.class.getName(), "test");
    assertTrue(tmpDir.delete());
    assertTrue(tmpDir.mkdir());
    assertTrue(tmpDir.getCanonicalPath().contains(DEFAULT_TEMPDIR_PATH));
    TempFileManager.setWorkingDirectory(tmpDir);
    File managedFile = TempFileManager.getTemporaryFile("foo-");
    assertTrue(managedFile.getCanonicalPath().contains(tmpDir.getCanonicalPath()));
    File managedDir = TempFileManager.getTemporaryDirectory("fooDir-");
    assertTrue(managedDir.getCanonicalPath().contains(tmpDir.getCanonicalPath()));
    TempFileManager.cleanup();
    assertFalse(managedFile.exists());
    assertFalse(managedDir.exists());
    assertTrue(tmpDir.delete());
    assertFalse(tmpDir.exists());
  }

  public void testTempFile() throws Exception {
    File tmpFile = TempFileManager.getTemporaryFile("foo-");
    assertTrue(tmpFile.exists());
    assertTrue(tmpFile.isFile());
    assertTrue(tmpFile.getCanonicalPath().contains(DEFAULT_TEMPDIR_PATH));
    assertTrue(tmpFile.getCanonicalPath().contains("pxe-"));
    TempFileManager.cleanup();
    assertTrue(!tmpFile.exists());
    assertTrue(!tmpFile.isFile());
  }

  public void testTempDirectory() throws Exception {
    File tmpDir = TempFileManager.getTemporaryDirectory("dir-");
    assertTrue(tmpDir.isDirectory());
    assertTrue(tmpDir.exists());
    assertTrue(tmpDir.getCanonicalPath().contains(DEFAULT_TEMPDIR_PATH));
    assertTrue(tmpDir.getCanonicalPath().contains("pxe-"));
    TempFileManager.cleanup();
    assertTrue(!tmpDir.isDirectory());
    assertTrue(!tmpDir.exists());
  }

  public void testNestedTempDirs() throws Exception {
    File dir1 = TempFileManager.getTemporaryDirectory("dir1-");
    assertTrue(dir1.isDirectory());
    assertTrue(dir1.exists());
    assertTrue(dir1.getCanonicalPath().contains(DEFAULT_TEMPDIR_PATH));
    assertTrue(dir1.getCanonicalPath().contains("pxe-"));
    File dir2 = TempFileManager.getTemporaryDirectory("dir2-", dir1);
    assertTrue(dir2.isDirectory());
    assertTrue(dir2.exists());
    assertTrue(dir2.getCanonicalPath().contains(DEFAULT_TEMPDIR_PATH));
    assertTrue(dir2.getCanonicalPath().contains("pxe-"));
    assertTrue(dir2.getParentFile().getCanonicalPath().equals(dir1.getCanonicalPath()));
    File fileInDir2 = TempFileManager.getTemporaryFile("file-", dir2);
    assertTrue(fileInDir2.exists());
    assertTrue(fileInDir2.getParentFile().getCanonicalPath().equals(dir2.getCanonicalPath()));
    TempFileManager.cleanup();
    assertTrue(!fileInDir2.exists());
    assertTrue(!dir1.exists());
    assertTrue(!dir2.exists());
  }

}
