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

import org.apache.ode.utils.GUID;
import org.apache.ode.utils.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenience class for managing temporary files and cleanup on JVM exit.
 */
public class TempFileManager {

  private static final Log __log = LogFactory.getLog(TempFileManager.class);

  private static TempFileManager __singleton;
  private static File __baseDir;
  private static File __workDir;

  private SortedSet<File> _registeredFiles = new TreeSet<File>(Collections.reverseOrder(null));

  private static synchronized TempFileManager getInstance() {
    if (__singleton == null) {
      __singleton = new TempFileManager();
    }
    return __singleton;
  }

  private TempFileManager() {
    super();

    if (__baseDir == null) {
      String tmpDirPath = null;
      try {
        tmpDirPath = SystemUtils.javaTemporaryDirectory();
      }
      catch (SecurityException se) {
        __log.error("Unable to read system property for temporary directory setting; "
            + "will use default configuration.");
        tmpDirPath = "";
      }

      File tmpDir = new File(tmpDirPath);
      if (tmpDir.exists()) {
        __baseDir = tmpDir;
      }
      else {
        throw new IllegalStateException("Odd system configuration - temporary working directory "
            + tmpDirPath + " does not exist.");
      }
    }

    try {
      File odeTmp = new File(__baseDir, "ode-" + new GUID().toString());
      if (odeTmp.mkdir()) {
        __workDir = odeTmp;
        __log.debug("Set working directory to: " + __workDir.getAbsolutePath());
        this._registerTemporaryFile(__workDir);
      }
      else {
        throw new IllegalStateException("Unable to create temporary working directory in "
            + __baseDir.getPath());
      }
    }
    catch (SecurityException se) {
      throw new IllegalStateException("The security configuration is preventing the creation of a "
          + "temporary working directory.", se);
    }

  }

  /**
   * <p>
   * Set the working temporary directory.  This method can only be invoked when
   * the singleton instance is uninitialized, and the <code>File</code> passed in
   * must be both a directory and writable.
   * </p>
   * @param f the temporary working directory
   */
  public static synchronized void setWorkingDirectory(File f) {
    if (__singleton == null) {
      if (f == null) {
        __baseDir = null;
      }
      else {
        if (f.isDirectory() && f.canWrite()) {
          __baseDir = f;
          if (__log.isDebugEnabled()) {
              __log.debug("Setting base working directory: " + f);
          }
        }
        else {
          throw new IllegalArgumentException("Not a writeable directory: " + f);
        }
      }
    }
    // cannot set working directory after an instance has been created;
    // call cleanup() first.
    else {
      String msg;
      if (__baseDir != null) {
        msg = "Already initialized in base directory: " + __baseDir.getPath();
      }
      else {
        msg = "Already initialized, but no base directory set.";
      }
      throw new IllegalStateException(msg);
    }
  }

  /**
   * <p>
   * Get a temporary file, if possible, and register it for cleanup later.  In the
   * event that a temporary file cannot be created, the method will attempt to
   * create a file in the current working directory instead.
   * </p>
   *
   * @param handle a prefix to use in naming the file; probably only useful for
   * debugging.
   * @return the temporary file.
   */
  public static synchronized File getTemporaryFile(String handle) {
    return getTemporaryFile(handle, __workDir);
  }

  public static synchronized File getTemporaryFile(String handle, File parent) {
    // force initialization if necessary
    if (__singleton == null) {
      getInstance();
    }

    if (handle == null) {
      handle = "temp-";
    }

    if (parent == null) {
      parent = (__workDir != null ? __workDir : __baseDir);
    }

    File tmp;
    try {
      tmp = File.createTempFile(handle + Long.toHexString(System.currentTimeMillis()), ".tmp", parent);
    } catch (IOException ioe) {
      __log.error("Unable to create temporary file in working directory " +
          (parent == null ? "<null>; " : (parent.getPath() + "; ")) +
          "falling back to current working directory.", ioe);
      tmp = new File(handle + new GUID().toString());
    }

    registerTemporaryFile(tmp);
    return tmp;
  }

  /**
   * <p>
   * Get a temporary working directory.
   * </p>
   *
   * @param handle a prefix to use in naming the directory.
   * @return the temp directory.
   * @see #getTemporaryFile(String)
   */
  public static synchronized File getTemporaryDirectory(String handle) {
    return getTemporaryDirectory(handle,null);
  }

  public static synchronized File getTemporaryDirectory(String handle, File parent) {
    File f = getTemporaryFile(handle, parent);
    f.delete();
    f.mkdirs();
    return f;
  }

  /*
   * Register an externally created file/directory for later cleanup.
   */
  public static synchronized void registerTemporaryFile(File f) {
    getInstance()._registerTemporaryFile(f);
  }

  private synchronized void _registerTemporaryFile(File f) {
    _registeredFiles.add(f);
    if (__log.isDebugEnabled()) {
        __log.debug("Registered temporary file: " + f.getPath());
    }
  }

  /**
   * <p>
   * Clear out the temporary working directory.  This can be called by, e.g.,
   * a commandline tool or other client when it is known that all temporary
   * files can be deleted.
   * </p>
   */
  public static synchronized void cleanup() {
    if (__singleton != null) {
      __singleton._cleanup();
      __singleton = null;
    } else {
      __log.debug("No cleanup necessary.");
    }
  }

  @SuppressWarnings("unchecked")
  private synchronized void _cleanup() {
    try {
      // collect all subdirectory contents that still exist, ordered files-first
      SortedSet<File> allFiles = new TreeSet(Collections.reverseOrder(null));
      for (File f: _registeredFiles) {
        if (f.exists()) {
          allFiles.addAll(FileUtils.directoryEntriesInPath(f));
        }
      }

      if (__log.isDebugEnabled()) {
          __log.debug("cleaning up " +  allFiles.size() + " files.");
      }

      // now delete all files
      for (File f: allFiles) {
        if (__log.isDebugEnabled()) {
            __log.debug("deleting: " + f.getAbsolutePath());
        }
        if (f.exists() && !f.delete()) {
          __log.error("Unable to delete file " + f.getAbsolutePath() +
          "; this may be caused by a descriptor leak and should be reported.");
          // fall back to deletion on VM shutdown
          f.deleteOnExit();
        }
      }
    } finally {
      _registeredFiles.clear();
      __workDir = null;
      __log.debug("cleanup done.");
    }
  }

}
