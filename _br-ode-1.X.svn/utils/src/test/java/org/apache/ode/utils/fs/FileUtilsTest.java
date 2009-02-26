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
    tmpDir.mkdir();
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

    public void testIsRelative(){
        // we don't test isAbsolute because it's platform dependent
        // for instance "c:\foo" will tested as relative on unix.
        String[] rPaths = new String[]{"policy.xml", "../foo/bar", "../../bar.xml"};
        for(String p:rPaths) assertTrue("This path is not relative! ", FileUtils.isRelative(p));
    }
}
