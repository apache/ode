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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * This class is based on {@link org.apache.log4j.helpers.FileWatchdog}.<p/>
 * Modifications have been made to support additional file events (creation, deletion and updates), and to allow "manual"
 * invocations of {@link #checkAndConfigure()} (i.e wihtout having to use a thread) while preserving time checking.<p/>
 * Now two use cases coexist:
 * <ol>
 * <li>Pass an instance of {@link FileWatchDog} to a new thread ({@link FileWatchDog} is a {@link Runnable}).
 *  So that {@link FileWatchDog#checkAndConfigure()} will be called automatically every {@code delay} milliseconds.</li>
 * <li>Invoke {@link FileWatchDog#checkAndConfigure()} only when you feel like it. If the expiration date previously set is lower than NOW then event
 * callback methods will be invoked accordingly.</li>
 * </ol>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class FileWatchDog implements Runnable {
    static final public long DEFAULT_DELAY = 60000;
    private final Log log;

    long expire;
    long lastModif;
    long delay = DEFAULT_DELAY;
    boolean fileExistedBefore, warnedAlready, interrupted;
    protected final File file;

    protected FileWatchDog(File file, long delay) {
        this(file);
        this.delay = delay;
    }

    protected FileWatchDog(File file) {
        this.file = file;
        log = LogFactory.getLog(FileWatchDog.class);
    }

    protected boolean isInitialized() throws Exception {
        return true;
    }

    protected void init() throws Exception {
    }

    protected void doOnDelete() throws Exception {
        init();
    }

    protected void doOnUpdate() throws Exception {
        init();
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void run() {
        try {
            while (!interrupted) {
                try {
                    Thread.currentThread().sleep(delay);
                } catch (InterruptedException e) {
                    // no interruption expected
                }
                checkAndConfigure();
            }
        } catch (Exception e) {
            log.warn("Exception occured. Thread will stop", e);
        }
    }


    public final void checkAndConfigure() throws Exception {
        long now = System.currentTimeMillis();
        if (expire <= now) {
            expire = now + delay;
            boolean fileExists;
            try {
                fileExists = file.exists();
            } catch (SecurityException e) {
                log.warn("Was not allowed to read check file existance, file:[" + file.getPath() + "].");
                interrupted = true; // there is no point in continuing 
                return;
            }

            if (fileExists) {
                fileExistedBefore = true;
                long l = file.lastModified();
                if (l > lastModif) {
                    lastModif = l;
                    if (log.isDebugEnabled())
                        log.debug("File [" + file + "] has been modified");
                    doOnUpdate();
                    warnedAlready = false;
                }
            } else if (!isInitialized()) {
                // first time and no file
                init();
            } else {
                if (fileExistedBefore) {
                    fileExistedBefore = false;
                    doOnDelete();
                }
                if (!warnedAlready) {
                    warnedAlready = true;
                    if (log.isDebugEnabled()) log.debug("[" + file + "] does not exist.");
                }
            }
        }
    }


}
