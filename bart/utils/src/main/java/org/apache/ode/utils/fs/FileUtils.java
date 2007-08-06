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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Various file system utilities.
 */
public class FileUtils {

    private static final Log __log = LogFactory.getLog(FileUtils.class);

    /**
     * Delete a file/directory, recursively.
     *
     * @param file
     *          file/directory to delete
     * @return <code>true</code> if successful
     */
    public static boolean deepDelete(File file) {
        if (file.exists()) {
            if (__log.isDebugEnabled()) {
                __log.debug("deleting: " + file.getAbsolutePath());
            }
            if (file.delete()) {
                return true;
            }

            if (file.isDirectory()) {
                boolean success = true;
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    success &= deepDelete(files[i]);
                }
                return success ? file.delete() : false;
            }
            else {
                __log.error("Unable to deepDelete file " + file.getAbsolutePath()
                        + "; this may be caused by a descriptor leak and should be reported.");
                return false;
            }
        }
        else {
            // file seems to be gone already?! anyway nothing to do for us.
            return true;
        }
    }

    /**
     * Recursively collect all Files in the given directory and all its
     * subdirectories.
     *
     * @param rootDirectory
     *          the top level directory used for the search
     * @return a List of found Files
     */
    public static List<File> directoryEntriesInPath(File rootDirectory) {
        return FileUtils.directoryEntriesInPath(rootDirectory, null);
    }

    /**
     * Recursively collect all Files in the given directory and all its
     * subdirectories, applying the given FileFilter.
     *
     * @param rootDirectory
     *          the top level directory used for the search
     * @param filter
     *          a FileFilter used for accepting/rejecting individual entries
     * @return a List of found Files
     */
    public static List<File> directoryEntriesInPath(File rootDirectory, FileFilter filter) {
        if (rootDirectory == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        if (!rootDirectory.exists()) {
            throw new IllegalArgumentException("File does not exist!");
        }

        ArrayList<File> collectedFiles = new ArrayList<File>(32);

        if (rootDirectory.isFile()) {
            if ((filter == null) || ((filter != null) && (filter.accept(rootDirectory)))) {
                collectedFiles.add(rootDirectory);
            }
            return collectedFiles;
        }

        FileUtils.directoryEntriesInPath(collectedFiles, rootDirectory, filter);
        return collectedFiles;
    }

    private static void directoryEntriesInPath(List<File> collectedFiles, File parentDir, FileFilter filter) {
        if ((filter == null) || ((filter != null) && (filter.accept(parentDir)))) {
            collectedFiles.add(parentDir);
        }

        File[] files = parentDir.listFiles();
        if (files != null) {
            for (int numFiles = files.length, i = 0; i < numFiles; i++) {
                File currentFile = files[i];

                if ((filter == null) || ((filter != null) && (filter.accept(currentFile)))) {
                    collectedFiles.add(currentFile);
                }

                if (currentFile.isDirectory()) {
                    FileUtils.directoryEntriesInPath(collectedFiles, currentFile, filter);
                }
            }
        }
    }

    public static String encodePath(String path) {
        return path.replaceAll(" ", "%20");
    }

}
