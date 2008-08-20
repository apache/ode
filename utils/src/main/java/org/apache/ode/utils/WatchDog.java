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

package org.apache.ode.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is based on {@link org.apache.log4j.helpers.FileWatchdog}.<p/>
 * Modifications have been made to support additional abstract ressource and more events (creation, deletion and updates), and to allow "manual"
 * invocations of {@link #check()} (i.e wihtout having to use a thread) while preserving time checking.<p/>
 * Now two use cases coexist:
 * <ol>
 * <li>Pass an instance of {@link WatchDog} to a new thread ({@link WatchDog} is a {@link Runnable}).
 * So that {@link WatchDog# check ()} will be called automatically every {@code delay} milliseconds.</li>
 * <li>Invoke {@link WatchDog# check ()} only when you feel like it. If the expiration date previously set is lower than NOW then event
 * callback methods will be invoked accordingly.</li>
 * </ol>
 *
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class WatchDog<T> implements Runnable {
    static final public long DEFAULT_DELAY = 30000;
    final Log log = LogFactory.getLog(getClass());

    private long expire;
    private T lastModif;
    private long delay;
    private boolean existedBefore, warnedAlready, interrupted;
    protected final Mutable<T> mutable;

    /**
     * @param mutable the object to watch closely
     * @param delay   between two checks
     */
    public WatchDog(Mutable<T> mutable, long delay) {
        this(mutable);
        this.delay = delay;
    }

    /**
     * @see #WatchDog(org.apache.ode.utils.WatchDog.Mutable, long)
     */
    public WatchDog(Mutable<T> mutable) {
        this.mutable = mutable;
        this.delay = DEFAULT_DELAY;
    }

    protected boolean isInitialized() {
        return true;
    }

    /**
     * Called by {@link #check()} if the object is not {@link #isInitialized initialized} and the {@link WatchDog.Mutable#exists()}  resource does not exist}.
     * <br/> This method might called to reset the object.
     *
     * @throws Exception
     */
    protected void init() {
    }

    /**
     * Called only if the resource previously existed and now does not exist.
     * <br/>The default implementation invokes {@link #init()} .
     *
     * @throws Exception
     */
    protected void doOnDelete() {
        init();
    }

    /**
     * Called only if the resource previously existed but the {@link WatchDog.Mutable#lastModified()} timestamp has changed (greater than the previous value).
     * <br/>The default implementation invokes {@link #init()} .
     *
     * @throws Exception
     */
    protected void doOnUpdate() {
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
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // no interruption expected
                }
                check();
            }
        } catch (Exception e) {
            log.warn("Exception occured. Thread will stop", e);
        }
    }

    public final void check() {
        long now = System.currentTimeMillis();
        if (expire <= now) {
            expire = now + delay;
            if (log.isDebugEnabled()) log.debug("Check for changes: "+mutable);
            if (mutable.exists()) {
                existedBefore = true;
                if (lastModif==null || mutable.hasChangedSince(lastModif)) {
                    lastModif = mutable.lastModified();
                    doOnUpdate();
                    if (log.isInfoEnabled()) log.info(mutable + " updated");
                    warnedAlready = false;
                }
            } else if (!isInitialized()) {
                // no resource and first time
                init();
                if (log.isInfoEnabled()) log.info(mutable + " initialized");
            } else {
                if (existedBefore) {
                    existedBefore = false;
                    lastModif = null;
                    doOnDelete();
                    if (log.isInfoEnabled()) log.info(mutable + " deleted");
                }
                if (!warnedAlready) {
                    warnedAlready = true;
                    if (log.isInfoEnabled()) log.info(mutable + " does not exist.");
                }
            }
        }
    }

    /**
     * have you said that duck typing would be nice?
     */
    public interface Mutable<T> {
        boolean exists();

        boolean hasChangedSince(T since);

        T lastModified();
    }

}
