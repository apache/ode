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

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
public class WatchDog<T, C extends WatchDog.Observer> implements Runnable {
    static final public long DEFAULT_DELAY = 30000;
    final Log log = LogFactory.getLog(WatchDog.class);

    private long expire;
    private T lastModif;
    private long delay = DEFAULT_DELAY;
    private boolean existedBefore, warnedAlready, interrupted;
    protected Mutable<T> mutable;
    protected C observer;

    public WatchDog() {
    }

    /**
     * @param mutable the object to watch closely
     * @param delay   between two checks
     */
    public WatchDog(Mutable<T> mutable, long delay) {
        this(mutable);
        this.delay = delay;
    }

    public WatchDog(Mutable<T> mutable, C observer) {
        this.mutable = mutable;
        this.observer = observer;
    }

    /**
     * @see #WatchDog(org.apache.ode.utils.WatchDog.Mutable, long)
     */
    public WatchDog(Mutable<T> mutable) {
        this.mutable = mutable;
    }


    public Mutable<T> getMutable() {
        return mutable;
    }

    public C getObserver() {
        return observer;
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
            /* get a lock on the observer right now
             It would be overkilled to lock before testing the dates.
             By locking after the comparaison the worst scenario is that 2 threads get in the "if",
             thread A gets the lock, thread B waits for it. Once the lock is released, thread B acquires it and do the checks on the mutable.
             So this scenario is *harmless*.
             */
            observer.getLock().lock();
            expire = now + delay;
            try {
                if (log.isDebugEnabled()) log.debug("[" + mutable + "]" + " check for changes");
                if (mutable.exists()) {
                    existedBefore = true;
                    if (lastModif == null || mutable.hasChangedSince(lastModif)) {
                        lastModif = mutable.lastModified();
                        observer.onUpdate();
                        if (log.isInfoEnabled()) log.info("[" + mutable + "]" + " updated");
                        warnedAlready = false;
                    } else {
                        if (log.isDebugEnabled()) log.debug("[" + mutable + "]" + " has not changed");
                    }
                } else if (!observer.isInitialized()) {
                    // no resource and first time
                    observer.init();
                    if (log.isInfoEnabled()) log.info("[" + mutable + "]" + " initialized");
                } else {
                    if (existedBefore) {
                        existedBefore = false;
                        lastModif = null;
                        observer.onDelete();
                        if (log.isInfoEnabled()) log.info("[" + mutable + "]" + " deleted");
                    }
                    if (!warnedAlready) {
                        warnedAlready = true;
                        if (log.isInfoEnabled()) log.info("[" + mutable + "]" + " does not exist.");
                    }
                }
            }catch(Exception e){
                if (log.isDebugEnabled()) log.debug("[" + mutable + "]" + " exception occurred during check.", e);
                // reset so that the next check retries right away
                expire = 0;
                lastModif = null;
                existedBefore = false;
                warnedAlready = false;
                observer.reset();
                if (log.isInfoEnabled()) log.info("[" + mutable + "] resetted.");
                throw new RuntimeException(e);
            } finally {
                observer.getLock().unlock();
            }
        } else {
            if (log.isTraceEnabled()) log.trace("[" + mutable + "]" + " wait period is not over");
        }
    }

    public static <C extends Observer> WatchDog<Long, C> watchFile(File file, C handler) {
        return new WatchDog<Long, C>(new FileMutable(file), handler);
    }

    public static <C extends Observer> WatchDog<Map<File, Long>, C> watchFiles(List<File> files, C handler) {
        return new WatchDog<Map<File, Long>, C>(new FileSetMutable(files), handler);
    }

    /**
     * have you said that duck typing would be nice?
     */
    public interface Mutable<T> {
        boolean exists();

        boolean hasChangedSince(T since);

        T lastModified();
    }

    static public class FileMutable implements WatchDog.Mutable<Long> {
        File file;

        public FileMutable(File file) {
            this.file = file;
        }

        public boolean exists() {
            return file.exists();
        }

        public boolean hasChangedSince(Long since) {
            // do use 'greater than' to handle file deletion. The timestamp of a non-exising file is 0L. 
            return lastModified().longValue() != since.longValue();
        }

        public Long lastModified() {
            return Long.valueOf(file.lastModified());
        }

        public String toString() {
            return file.toString();
        }
    }

    static public class FileSetMutable implements WatchDog.Mutable<Map<File, Long>> {

        File[] files;

        public FileSetMutable(Collection<File> files) {
            this.files = new File[files.size()];
            files.toArray(this.files);
        }

        public FileSetMutable(File[] files) {
            this.files = files;
        }

        public boolean exists() {
            return true;
        }

        public boolean hasChangedSince(Map<File, Long> since) {
            Map<File, Long> snapshot = lastModified();
            return !CollectionUtils.equals(snapshot, since);
        }

        public Map<File, Long> lastModified() {
            Map<File, Long> m = new HashMap<File, Long>(files.length * 15 / 10);
            for (File f : files) m.put(f, Long.valueOf(f.lastModified()));
            return m;
        }
    }

    public interface Observer<A> {

        boolean isInitialized();

        /**
         * Called by {@link WatchDog#check()} if the underlying object is not {@link #isInitialized initialized} and the {@link WatchDog.Mutable#exists()}  resource does not exist}.
         * <br/> This method might called to reset the underlying object.
         *
         * @throws Exception
         */
        void init();

        void reset();

        /**
         * Called only if the resource previously existed and now does not exist.
         * <br/>The default implementation invokes {@link #init()} .
         *
         * @throws Exception
         */
        void onDelete();

        /**
         * Called only if the resource previously existed but the {@link WatchDog.Mutable#lastModified()} timestamp has changed (greater than the previous value).
         * <br/>The default implementation invokes {@link #init()} .
         *
         * @throws Exception
         */
        void onUpdate();

        Lock getLock();

        A get();

    }

    /**
     * A default implementation of #ChangeHandler. Delete and Update will both invoke the #init method which satifies most use cases.
     * So subclasses may simply override the #init method to fit their own needs.
     */
    public static class DefaultObserver<A> implements Observer<A> {

        protected final ReadWriteLock lock = new ReentrantReadWriteLock();
        protected A object;

        /**
         * @return true if the wrapped if not null
         */
        public boolean isInitialized() {
            return object != null;
        }

        /**
         * empty implementation
         */
        public void init() {
        }

        public void reset() {
            object = null;
        }

        /**
         * delegate to #init
         */
        public void onDelete() {
            init();
        }

        /**
         * delegate to #init
         */
        public void onUpdate() {
            init();
        }

        public Lock getLock() {
            return lock.writeLock();
        }

        public A get() {
            lock.readLock().lock();
            try {
                return object;
            } finally {
                lock.readLock().unlock();
            }
        }
    }
}
