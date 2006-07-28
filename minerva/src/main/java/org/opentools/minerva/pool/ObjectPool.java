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

/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.pool;

import java.io.PrintWriter;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A generic object pool.  You must provide a PoolObjectFactory (or the class
 * of a Java Bean) so the pool knows what kind of objects to create.  It has
 * many configurable parameters, such as the minimum and maximum size of the
 * pool, whether to enable idle timeouts, etc.  If the pooled objects
 * implement PooledObject, they will automatically be returned to the pool at
 * the appropriate times.
 * <P>In general, the appropriate way to use a pool is:</P>
 * <OL>
 *   <LI>Create it</LI>
 *   <LI>Configure it (set factory, name, parameters, etc.)</LI>
 *   <LI>Initialize it (once done, further configuration is not allowed)</LI>
 *   <LI>Use it</LI>
 *   <LI>Shut it down</LI>
 * </OL>
 * @see org.opentools.minerva.pool.PooledObject
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
@SuppressWarnings("unchecked")
public class ObjectPool implements PoolEventListener {
  private static final Log __log = LogFactory.getLog(ObjectPool.class);
    private final static String INITIALIZED = "Pool already initialized!";
    private final static PoolGCThread collector = new PoolGCThread();

    static {
        collector.start();
    }

    private PoolObjectFactory factory;
    private String poolName;

    private HashMap objects = null;
    private HashSet deadObjects = null;
    private int minSize = 0;
    private int maxSize = 0;
    private boolean idleTimeout = false;
    private boolean runGC = false;
    private float maxIdleShrinkPercent = 1.0f; // don't replace idle connections that timeout
    private long idleTimeoutMillis = 1800000l; // must be idle in pool for 30 minutes
    private long gcMinIdleMillis   = 1200000l; // must be unused by client for 20 minutes
    private long gcIntervalMillis  =  120000l; // shrink & gc every 2 minutes
    private long lastGC = System.currentTimeMillis();
    private boolean blocking = true;
    private boolean trackLastUsed = false;
    private boolean invalidateOnError = false;
    private PrintWriter logWriter = null;
    private Object resizeLock = new Object();

    /**
     * Creates a new pool.  It cannot be used until you specify a name and
     * object factory or bean class, and initialize it.
     * @see #setName
     * @see #setObjectFactory
     * @see #initialize
     */
    public ObjectPool() {}

    /**
     * Creates a new pool with the specified parameters.  It cannot be used
     * until you initialize it.
     * @param factory The object factory that will create the objects to go in
     *    the pool.
     * @param poolName The name of the pool.  This does not have to be unique
     *    across all pools, but it is strongly recommended (and it may be a
     *    requirement for certain uses of the pool).
     * @see #initialize
     */
    public ObjectPool(PoolObjectFactory factory, String poolName) {
        setObjectFactory(factory);
        setName(poolName);
    }

    /**
     * Creates a new pool with the specified parameters.  It cannot be used
     * until you initialize it.
     * @param javeBeanClass The Class of a Java Bean.  New instances for the
     *    pool will be created with the no-argument constructor, and no
     *    particular initialization or cleanup will be performed on the
     *    instances.  Use a PoolObjectFactory if you want more control over
     *    the instances.
     * @param poolName The name of the pool.  This does not have to be unique
     *    across all pools, but it is strongly recommended (and it may be a
     *    requirement for certain uses of the pool).
     * @see #initialize
     */
    public ObjectPool(Class javaBeanClass, String poolName) {
        setObjectFactory(javaBeanClass);
        setName(poolName);
    }

    /**
     * Sets the object factory for the pool.  The object factory controls the
     * instances created for the pool, and can initialize instances given out
     * by the pool and cleanup instances returned to the pool.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the object factory after the pool has been
     *    initialized.
     */
    public void setObjectFactory(PoolObjectFactory factory) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        this.factory = factory;
    }

    /**
     * Sets the object factory as a new factory for Java Beans.  New instances
     *    for the pool will be created with the no-argument constructor, and no
     *    particular initialization or cleanup will be performed on the
     *    instances.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the object factory after the pool has been
     *    initialized.
     */
    public void setObjectFactory(Class javaBeanClass) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        factory = new BeanFactory(javaBeanClass);
    }

    /**
     * Sets the name of the pool.  This is not required to be unique across all
     * pools, but is strongly recommended.  Certain uses of the pool (such as
     * a JNDI object factory) may require it.  This must be set exactly once
     * for each pool (it may be set in the constructor).
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the name of the pool more than once.
     */
    public void setName(String name) {
        if(name == null || name.length() == 0)
            throw new IllegalArgumentException("Cannot set pool name to null or empty!");
        if(poolName != null && !poolName.equals(name))
            throw new IllegalStateException("Cannot change pool name once set!");
        poolName = name;
    }

    /**
     * Gets the name of the pool.
     */
    public String getName() {return poolName;}

    /**
     * Gets a log writer used to record pool events.
     */
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    /**
     * Sets a log writer used to record pool events.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the log writer after the pool has been
     *    initialized.
     */
    public void setLogWriter(PrintWriter writer) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        logWriter = writer;
    }

    /**
     * Sets the minimum size of the pool.  The pool will create this many
     * instances at startup, and once running, it will never shrink below this
     * size.  The default is zero.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the minimum size after the pool has been
     *    initialized.
     */
    public void setMinSize(int size) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        minSize = size;
        if(maxSize != 0 && minSize > maxSize) {
            maxSize = minSize;
            log("WARNING: pool max size set to "+maxSize+" to stay >= min size");
        }
    }

    /**
     * Gets the minimum size of the pool.
     * @see #setMinSize
     */
    public int getMinSize() {return minSize;}

    /**
     * Sets the maximum size of the pool.  Once the pool has grown to hold this
     * number of instances, it will not add any more instances.  If one of the
     * pooled instances is available when a request comes in, it will be
     * returned.  If none of the pooled instances are available, the pool will
     * either block until an instance is available, or return null.  The default
     * is no maximum size.
     * @see #setBlocking
     * @param size The maximum size of the pool, or 0 if the pool should grow
     *    indefinitely (not recommended).
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the maximum size after the pool has been
     *    initialized.
     */
    public void setMaxSize(int size) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        maxSize = size;
        if(maxSize != 0 && minSize > maxSize) {
            minSize = maxSize;
            log("WARNING: pool min size set to "+minSize+" to stay <= max size");
        }
    }

    /**
     * Gets the maximum size of the pool.
     * @see #setMaxSize
     */
    public int getMaxSize() {return maxSize;}

    /**
     * Sets whether the pool should release instances that have not been used
     * recently.  This is intended to reclaim resources (memory, database
     * connections, file handles, etc) during periods of inactivity.  This runs
     * as often as garbage collection (even if garbage collection is disabled,
     * this uses the same timing parameter), but the required period of
     * inactivity is different.  All objects that have been unused for more
     * than the idle timeout are closed, but if you set the MaxIdleShrinkPercent
     * parameter, the pool may recreate some objects so the total number of
     * pooled instances doesn't shrink as rapidly. Also, under no circumstances
     * will the number of pooled instances fall below the minimum size.</p>
     * <P>The default is disabled.</P>
     * @see #setGCInterval
     * @see #setIdleTimeout
     * @see #setMaxIdleTimeoutPercent
     * @see #setMinSize
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the idle timeout state after the pool has
     *    been initialized.
     */
    public void setIdleTimeoutEnabled(boolean enableTimeout) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        idleTimeout = enableTimeout;
    }

    /**
     * Gets whether the pool releases instances that have not been used
     * recently.  This is different than garbage collection, which returns
     * instances to the pool if a client checked an instance out but has not
     * used it and not returned it to the pool.
     * @see #setIdleTimeoutEnabled
     */
    public boolean isIdleTimeoutEnabled() {return idleTimeout;}

    /**
     * Sets whether garbage collection is enabled.  This is the process of
     * returning objects to the pool if they have been checked out of the pool
     * but have not been used in a long periond of time.  This is meant to
     * reclaim resources, generally caused by unexpected failures on the part
     * of the pool client (which forestalled returning an object to the pool).
     * This runs on the same schedule as the idle timeout (if enabled), but
     * objects that were just garbage collected will not be eligible for the
     * idle timeout immediately (after all, they presumably represented "active"
     * clients).  Objects that are garbage collected will be checked out again
     * immediately if a client is blocking waiting for an object.  The default
     * value is disabled.
     * @see #setGCMinIdleTime
     * @see #setGCInterval
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the garbage collection state after the pool
     *    has been initialized.
     */
    public void setGCEnabled(boolean enabled) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        runGC = enabled;
    }

    /**
     * Gets whether garbage collection is enabled.
     * @see #setGCEnabled
     */
    public boolean isGCEnabled() {return runGC;}

    /**
     * Sets the idle timeout percent as a fraction between 0 and 1.  If a number
     * of objects are determined to be idle, they will all be closed and
     * removed from the pool.  However, if the ratio of objects released to
     * objects in the pool is greater than this fraction, some new objects
     * will be created to replace the closed objects.  This prevents the pool
     * size from decreasing too rapidly.  Set to 0 to decrease the pool size by
     * a maximum of 1 object per test, or 1 to never replace objects that have
     * exceeded the idle timeout.  The pool will always replace enough closed
     * connections to stay at the minimum size.
     * @see #setIdleTimeoutEnabled
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the idle timeout percent after the pool
     *    has been initialized.
     * @throws java.lang.IllegalArgumentException
     *    Occurs when the percent parameter is not between 0 and 1.
     */
    public void setMaxIdleTimeoutPercent(float percent) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        if(percent < 0f || percent > 1f)
            throw new IllegalArgumentException("Percent must be between 0 and 1!");
        maxIdleShrinkPercent = percent;
    }

    /**
     * Gets the idle timeout percent as a fraction between 0 and 1.
     * @see #setMaxIdleTimeoutPercent
     */
    public float getMaxIdleTimeoutPercent() {return maxIdleShrinkPercent;}

    /**
     * Sets the minimum idle time to release an unused object from the pool.  If
     * the object is not in use and has not been used for this amount of time,
     * it will be released from the pool.  If timestamps are enabled, the client
     * may update the last used time.  Otherwise, the last used time is only
     * updated when an object is acquired or released.  The default value is
     * 30 minutes.
     * @see #setIdleTimeoutEnabled
     * @param millis The idle time, in milliseconds.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the idle timeout after the pool
     *    has been initialized.
     */
    public void setIdleTimeout(long millis) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        idleTimeoutMillis = millis;
    }

    /**
     * Gets the minimum idle time to release an unused object from the pool.
     * @see #setIdleTimeout
     */
    public long getIdleTimeout() {return idleTimeoutMillis;}

    /**
     * Sets the minimum idle time to make an object eligible for garbage
     * collection.  If the object is in use and has not been used for this
     * amount of time, it may be returned to the pool.  If timestamps are
     * enabled, the client may update the last used time (this is generally
     * recommended if garbage collection is enabled).  Otherwise, the last used
     * time is only updated when an object is acquired or released.  The default
     * value is 20 minutes.
     * @see #setGCEnabled
     * @param millis The idle time, in milliseconds.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the garbage collection idle time after the
     *    pool has been initialized.
     */
    public void setGCMinIdleTime(long millis) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        gcMinIdleMillis = millis;
    }

    /**
     * Gets the minimum idle time to make an object eligible for garbage
     * collection.
     * @see #setGCMinIdleTime
     */
    public long getGCMinIdleTime() {return gcMinIdleMillis;}

    /**
     * Sets the length of time between garbage collection and idle timeout runs.
     * This is inexact - if there are many pools with garbage collection and/or
     * the idle timeout enabled, there will not be a thread for each one, and
     * several nearby actions may be combined.  Likewise if the collection
     * process is lengthy for certain types of pooled objects (not recommended),
     * other actions may be delayed.  This is to prevend an unnecessary
     * proliferation of threads.  Note that this parameter controls
     * both garbage collection and the idle timeout - and they will be performed
     * together if both are enabled.  The deafult value is 2 minutes.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the garbage collection interval after the
     *    pool has been initialized.
     */
    public void setGCInterval(long millis) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        gcIntervalMillis = millis;
    }

    /**
     * Gets the length of time between garbage collection and idle timeout runs.
     * @see #setGCInterval
     */
    public long getGCInterval() {return gcIntervalMillis;}

    /**
     * Sets whether a request for an object will block if the pool size is
     * maxed out and no objects are available.  If set to block, the request
     * will not return until an object is available.  Otherwise, the request
     * will return null immediately (and may be retried).  If multiple
     * requests block, there is no guarantee which will return first.  The
     * default is to block.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the blocking parameter after the
     *    pool has been initialized.
     */
    public void setBlocking(boolean blocking) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        this.blocking = blocking;
    }

    /**
     * Gets whether a request for an object will block if the pool size is
     * maxed out and no objects are available.
     * @see #setBlocking
     */
    public boolean isBlocking() {return blocking;}

    /**
     * Sets whether object clients can update the last used time.  If not, the
     * last used time will only be updated when the object is given to a client
     * and returned to the pool.  This time is important if the idle timeout or
     * garbage collection is enabled (particularly the latter).  The default
     * is false.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the timestamp parameter after the
     *    pool has been initialized.
     */
    public void setTimestampUsed(boolean timestamp) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        trackLastUsed = timestamp;
    }

    /**
     * Gets whether object clients can update the last used time.
     */
    public boolean isTimestampUsed() {return trackLastUsed;}

    /**
     * Sets the response for object errors.  If this flag is set and an error
     * event occurs, the object is removed from the pool entirely.  Otherwise,
     * the object is returned to the pool of available objects.  For example, a
     * SQL error may not indicate a bad database connection (flag not set),
     * while a TCP/IP error probably indicates a bad network connection (flag
     * set).  If this flag is not set, you can still manually invalidate
     * objects using markObjectAsInvalid.
     * @see #markObjectAsInvalid
     * @see #objectError
     */
    public void setInvalidateOnError(boolean invalidate) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        invalidateOnError = invalidate;
    }

    /**
     * Gets whether objects are removed from the pool in case of errors.
     */
    public boolean isInvalidateOnError() {return invalidateOnError;}

    /**
     * Prepares the pool for use.  This must be called exactly once before
     * getObject is even called.  The pool name and object factory must be set
     * before this call will succeed.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to initialize the pool without setting the object
     *    factory or name, or you initialize the pool more than once.
     */
    public void initialize() {
        if(factory == null || poolName == null)
            throw new IllegalStateException("Factory and Name must be set before pool initialization!");
        if(objects != null)
            throw new IllegalStateException("Cannot initialize more than once!");
        deadObjects = new HashSet();
        objects = new HashMap();
        factory.poolStarted(this, logWriter);
        lastGC = System.currentTimeMillis();
        int max = maxSize <= 0 ? minSize : Math.min(minSize, maxSize);
        for(int i=0; i<max; i++)
            createNewObject(null, false);
        collector.addPool(this);
    }

    /**
     * Shuts down the pool.  All outstanding objects are closed and all objects
     * are released from the pool.  No getObject or releaseObject calls will
     * succeed after this method is called - and they will probably fail during
     * this method call.
     */
    public void shutDown() {
        collector.removePool(this);
        factory.poolClosing(this);
        HashMap localObjects = objects;
        objects = null;

        // close all objects
        for(Iterator it = localObjects.values().iterator(); it.hasNext();) {
            ObjectRecord rec = (ObjectRecord)it.next();
            if(rec.isInUse())
                factory.returnObject(rec.getClientObject());
            factory.deleteObject(rec.getObject());
            rec.close();
        }

        localObjects.clear();
        factory = null;
        poolName = null;
    }

    /**
     * Gets an object from the pool.  If all the objects in the pool are in use,
     * creates a new object, adds it to the pool, and returns it.  If all
     * objects are in use and the pool is at maximum size, will block or
     * return null.
     * @see #setBlocking
     */
    public Object getObject() {
        return getObject(null);
    }

    /**
     * Gets an object that fits the specified parameters from the pool.
     * If all the objects in the pool are in use or don't fit, creates
     * a new object, adds it to the pool, and returns it.  If all
     * objects are in use or don't fit and the pool is at maximum size,
     * will block or return null.
     * @see #setBlocking
     */
    public Object getObject(Object parameters) {
        if(objects == null)
            throw new IllegalStateException("Tried to use pool before it was Initialized or after it was ShutDown!");

        Object result = factory.isUniqueRequest();
        if(result != null) // If this is identical to a previous request,
            return result; // return the same result.  This is unusual.

        while(true) {
            Iterator it = objects.values().iterator();
            while(it.hasNext()) {
                ObjectRecord rec = (ObjectRecord)it.next();
                if(!rec.isInUse() && factory.checkValidObject(rec.getObject(), parameters)) {
                    try {
                        rec.setInUse(true);
                        Object ob = rec.getObject();
                        result = factory.prepareObject(ob);
                        if(result != ob) rec.setClientObject(result);
                        if(result instanceof PooledObject)
                            ((PooledObject)result).addPoolEventListener(this);
                        log("Pool "+this+" gave out pooled object: "+result);
                        return result;
                    } catch(ConcurrentModificationException e) {
                        // That's OK, just go on and try another object
                    } catch(RuntimeException e) {
                        // Some problem in PrepareObject?
                        try {
                            rec.setInUse(false);
                        } catch(ConcurrentModificationException e2) {}
                        // If this is a problem with the current state,
                        // we don't want to block, as it will continue to
                        // be a problem as far as we know
                        throw e;
                    }
                }
            }

            result = createNewObject(parameters, true);
            if(result != null)
                return result;

            if(blocking) {
                log("Pool "+this+" waiting for a free object");
                synchronized(this) {
                    try {
                        wait();
                    } catch(InterruptedException e) {}
                }
            } else {
                break;
            }
        }

        log("Pool "+this+" couldn't find an object to return!");
        return result;
    }

    /**
     * Sets the last used time for an object in the pool that is currently
     * in use.  If the timestamp parameter is not set, this call does nothing.
     * Otherwise, the object is marked as last used at the current time.
     * @throws java.lang.IllegalArgumentException
     *         Occurs when the object is not recognized by the factory or not
     *         in the pool.
     * @throws java.lang.IllegalStateException
     *         Occurs when the object is not currently in use.
     * @see #setTimestampUsed
     */
    public void setLastUsed(Object object) {
        if(!trackLastUsed) return;
        Object ob = null;
        try {
            ob = factory.translateObject(object);
        } catch(Exception e) {
            throw new IllegalArgumentException("Pool "+getName()+" does not recognize object for last used time: "+object);
        }
        ObjectRecord rec = ob == null ? null : (ObjectRecord)objects.get(ob);
        if(rec == null)
            throw new IllegalArgumentException("Pool "+getName()+" does not recognize object for last used time: "+object);
        if(rec.isInUse())
            rec.setLastUsed();
        else
            throw new IllegalStateException("Cannot set last updated time for an object that's not in use!");
    }

    /**
     * Indicates that an object is no longer valid, and should be removed from
     * the pool entirely.  This should be called before the object is returned
     * to the pool (specifically, before factory.returnObject returns), or else
     * the object may be given out again by the time this is called!  Also, you
     * still need to actually return the object to the pool by calling
     * releaseObject, if you aren't calling this during that process already.
     * @param Object The object to invalidate, which must be the exact object
     *               returned by getObject
     */
    public void markObjectAsInvalid(Object object) {
        if(deadObjects == null)
            throw new IllegalStateException("Tried to use pool before it was Initialized or after it was ShutDown!");
        deadObjects.add(object);
    }

    /**
     * Returns an object to the pool.  This must be the exact object that was
     * given out by getObject, and it must be returned to the same pool that
     * generated it.  If other clients are blocked waiting on an object, the
     * object may be re-released immediately.
     * @throws java.lang.IllegalArgumentException
     *    Occurs when the object is not in this pool.
     */
    public void releaseObject(Object object) {
        if(objects == null)
            throw new IllegalStateException("Tried to use pool before it was Initialized or after it was ShutDown!");
        boolean removed = false;  // Whether we returned to the pool, or threw out entirely

        synchronized(object) {
            Object pooled = null;
            try {
                pooled = factory.translateObject(object);
            } catch(Exception e) {
                return;        // We can't release it if the factory can't recognize it
            }
            if(pooled == null) // We can't release it if the factory can't recognize it
                return;
            ObjectRecord rec = (ObjectRecord)objects.get(pooled);
            if(rec == null) // Factory understands it, but we don't
                throw new IllegalArgumentException("Object "+object+" is not in pool "+poolName+"!");
            if(!rec.isInUse()) return; // Must have been released by GC?
            if(object instanceof PooledObject)
                ((PooledObject)object).removePoolEventListener(this);
            factory.returnObject(object);
            if(deadObjects.contains(object)) {
                removeObject(pooled);
                try {
                    factory.deleteObject(pooled);
                } catch(Exception e) {
                    log("Pool "+this+" factory ("+factory.getClass().getName()+" delete error: "+e);
                }
                rec.close();
                deadObjects.remove(object);
                removed = true;
                if(objects.size() < minSize)
                    createNewObject(null, false);
            } else {
                rec.setInUse(false);
                removed = false;
            }
        }
        if(removed)
            log("Pool "+this+" destroyed object "+object+".");
        else
            log("Pool "+this+" returned object "+object+" to the pool.");
        if(blocking) {
            synchronized(this) {
                notify();
            }
        }
    }

    private int getUsedCount() {
        if(objects == null) return 0;
        int total = 0;
        Iterator it = new HashSet(objects.values()).iterator();
        while(it.hasNext()) {
            ObjectRecord or = (ObjectRecord)it.next();
            if(or.isInUse()) ++total;
        }
        return total;
    }

    /**
     * Returns the pool name and status.
     */
    public String toString() {
        return poolName+" ["+getUsedCount()+"/"+(objects == null ? 0 : objects.size())+"/"+(maxSize == 0 ? "Unlimited" : Integer.toString(maxSize))+"]";
    }


    // ---- PoolEventListener Implementation ----

    /**
     * If the object has been closed, release it.
     */
    public void objectClosed(PoolEvent evt) {
        releaseObject(evt.getSource());
    }

    /**
     * If the invalidateOnError flag is set, the object will be removed from
     * the pool entirely when the client has finished with it.
     */
    public void objectError(PoolEvent evt) {
        if(invalidateOnError || evt.isCatastrophic())
            markObjectAsInvalid(evt.getSource());
    }

    /**
     * If we're tracking the last used times, update the last used time for the
     * specified object.
     */
    public void objectUsed(PoolEvent evt) {
        if(!trackLastUsed) return;
        setLastUsed(evt.getSource());
    }

    long getNextGCMillis(long now) {
        if(!runGC) return Long.MAX_VALUE;
        return lastGC + gcIntervalMillis - now;
    }

    // Allow GC if we're within 10% of the desired interval
    boolean isTimeToGC() {
        return System.currentTimeMillis() >=
               lastGC + Math.round(gcIntervalMillis * 0.9f);
    }

    void runGCandShrink() {
        if(runGC) { // Garbage collection - return any object that's been out too long with no use
            Iterator it = new HashSet(objects.values()).iterator();
            while(it.hasNext()) {
                ObjectRecord rec = (ObjectRecord)it.next();
                if(rec.isInUse() && rec.getMillisSinceLastUse() >= gcMinIdleMillis) {
                    releaseObject(rec.getClientObject());
                }
            }
        }
        if(idleTimeout) { // Shrinking the pool - remove objects from the pool if they have not been used in a long time
             // Find object eligible for removal
            HashSet eligible = new HashSet();
            Iterator it = new HashSet(objects.values()).iterator();
            while(it.hasNext()) {
                ObjectRecord rec = (ObjectRecord)it.next();
                if(!rec.isInUse() && rec.getMillisSinceLastUse() > idleTimeoutMillis)
                    eligible.add(rec);
            }
            // Calculate max number of objects to remove without replacing
            int max = Math.round(eligible.size() * maxIdleShrinkPercent);
            if(max == 0 && eligible.size() > 0) max = 1;
            int count = 0;
            // Attempt to remove that many objects
            it = eligible.iterator();
            while(it.hasNext()) {
                try {
                    // Delete the object
                    ObjectRecord rec = (ObjectRecord)it.next();
                    rec.setInUse(true);  // Don't let someone use it while we destroy it
                    Object pooled = rec.getObject();
                    removeObject(pooled);
                    try {
                        factory.deleteObject(pooled);
                    } catch(Exception e) {
                        log("Pool "+this+" factory ("+factory.getClass().getName()+" delete error: "+e);
                    }
                    rec.close();
                    ++count;

                    if(count > max || objects.size() < minSize)
                        createNewObject(null, false);
                } catch(ConcurrentModificationException e) {
                }
            }
        }
        lastGC = System.currentTimeMillis();
    }

    /**
     * Removes an object from the pool.  Only one thread can add or remove
     * an object at a time.
     */
    private void removeObject(Object pooled) {
        synchronized(resizeLock) {
            objects.remove(pooled);
        }
    }

    /**
     * Creates a new Object.
     * @param forImmediateUse If <b>true</b>, then the object is locked and
     *          translated by the factory, and the resulting object
     *          returned.  If <b>false</b>, then the object is left in the
     *          pool unlocked.
     */
    private Object createNewObject(Object parameters, boolean forImmediateUse) {
        Object ob = null;
        String message = null;
        // Serialize creating new objects
        synchronized(resizeLock) {  // Don't let 2 threads add at the same time
            if(maxSize == 0 || objects.size() < maxSize) {
                ob = factory.createObject(parameters);
                if (ob != null) { // if factory can create object
                    ObjectRecord rec = new ObjectRecord(ob, forImmediateUse);
                    HashMap newMap = (HashMap)objects.clone();
                    newMap.put(ob, rec);
                    if(forImmediateUse) {
                        Object result = factory.prepareObject(ob);
                        if(result != ob) rec.setClientObject(result);
                        if(result instanceof PooledObject)
                            ((PooledObject)result).addPoolEventListener(this);
                        message = "Pool "+this+" gave out new object: "+result;
                        ob = result;
                    } else message = "Pool "+poolName+" created a new object: "+ob;
                    objects = newMap;
                } else message = "Pool "+poolName+" factory "+factory+" unable to create new object!";
            } else message = "Pool "+poolName+" is full ("+objects.size()+"/"+maxSize+")!";
        }
        if(message != null)
            log(message);
        return ob;
    }

    private void log(String message) {
      __log.debug(message);
        if(logWriter != null)
            logWriter.println(message);
    }
}

class BeanFactory extends PoolObjectFactory {
    private Class beanClass;
    private PrintWriter logger;

    public BeanFactory(Class beanClass) {
        try {
            beanClass.getConstructor(new Class[0]);
        } catch(NoSuchMethodException e) {
            throw new IllegalArgumentException("Bean class doesn't have no-arg constructor!");
        }
        this.beanClass = beanClass;
    }

    public void poolStarted(ObjectPool pool, PrintWriter log) {
        super.poolStarted(pool, log);
        logger = log;
    }

    public Object createObject(Object parameters) {
        try {
            return beanClass.newInstance();
        } catch(Exception e) {
            logger.println("Unable to create instance of "+beanClass.getName()+": "+e);
        }
        return null;
    }
}
