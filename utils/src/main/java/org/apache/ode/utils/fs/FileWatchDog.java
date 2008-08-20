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

import org.apache.ode.utils.WatchDog;

import java.io.File;

public class FileWatchDog extends WatchDog<Long> {

    protected final File file;

    public FileWatchDog(File file, long delay) {
        super(new FileDecorator(file),delay);
        this.file = file;
    }

    public FileWatchDog(File file) {
        this(file, WatchDog.DEFAULT_DELAY);
    }

    static class FileDecorator implements WatchDog.Mutable<Long>{
        File file;

        FileDecorator(File file) {
            this.file = file;
        }

        public boolean exists() {
            return file.exists();
        }

        public boolean hasChangedSince(Long since) {
            return lastModified().longValue()>since.longValue();
        }

        public Long lastModified() {
            return Long.valueOf(file.lastModified());
        }

        public String toString() {
            return file.toString();
        }
    }


}
