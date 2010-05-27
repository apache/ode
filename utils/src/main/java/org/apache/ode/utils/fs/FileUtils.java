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
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Various file system utilities.
 */
public class FileUtils {

    private static final Log __log = LogFactory.getLog(FileUtils.class);

    /**
     * Test if the given path is absolute or not.
     * @param path
     * @return true is absolute
     * @see java.io.File#isAbsolute()
     */
    public static boolean isAbsolute(String path){
        return new File(path).isAbsolute();
    }

    /**
     * Test if the given path is relative or absolute.
     * @param path
     * @return true is relative
     * @see java.io.File#isAbsolute()
     */
    public static boolean isRelative(String path){
        return !isAbsolute(path);
    }


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
     * subdirectories, applying the given FileFilter. The FileFilter is also applied to the given rootDirectory.
     * As a result the rootDirectory might be in the returned list.
     * <p>
     * Returned files are ordered lexicographically but for each directory, files come before its sudirectories.
     * For instance:<br/>
     * test<br/>
     * test/alpha.txt<br/>
     * test/zulu.txt<br/>
     * test/a<br/>
     * test/a/alpha.txt<br/>
     * test/z<br/>
     * test/z/zulu.txt<br/>
     * <p>
     * instead of:<br/>
     * test<br/>
     * test/a<br/>
     * test/a/alpha.txt<br/>
     * test/alpha.txt<br/>
     * test/z<br/>
     * test/z/zulu.txt<br/>
     * test/zulu.txt<br/>
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

        File[] allFiles = parentDir.listFiles();
        if (allFiles != null) {
            TreeSet<File> dirs = new TreeSet<File>();
            TreeSet<File> acceptedFiles = new TreeSet<File>();
            for (File f : allFiles) {
                if (f.isDirectory()) {
                    dirs.add(f);
                } else {
                    if ((filter == null) || ((filter != null) && (filter.accept(f)))) {
                        acceptedFiles.add(f);
                    }
                }
            }
            collectedFiles.addAll(acceptedFiles);
            for (File currentFile : dirs) {
                FileUtils.directoryEntriesInPath(collectedFiles, currentFile, filter);
            }
        }
    }

    public static String encodePath(String path) {
        return path.replaceAll(" ", "%20");
    }

    public static void main(String[] args) {
        List<File> l = directoryEntriesInPath(new File("/tmp/test"));
        for(File f : l) System.out.println(f);
        System.out.println("########");
     TreeSet<File> s= new TreeSet(l);
        for(File f : s) System.out.println(f);
    }
}
