/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.pool;

import java.util.*;

/**
 * Stores the properties of an object in a pool.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
class ObjectRecord {
    private long created;
    private long lastUsed;
    private Object object;
    private Object clientObject;
    private boolean inUse;

    /**
     * Created a new record for the specified pooled object.  Objects default to
     * being in use when created, so that they can't be stolen away from the
     * creator by another thread.
     */
    public ObjectRecord(Object ob) {
        this(ob, true);
    }

    /**
     * Created a new record for the specified pooled object.  Sets the initial
     * state to in use or not.
     */
    public ObjectRecord(Object ob, boolean inUse) {
        created = lastUsed = System.currentTimeMillis();
        object = ob;
        this.inUse = inUse;
    }

    /**
     * Gets the date when this connection was originally opened.
     */
    public Date getCreationDate() {
        return new Date(created);
    }

    /**
     * Gets the date when this connection was last used.
     */
    public Date getLastUsedDate() {
        return new Date(lastUsed);
    }

    /**
     * Gets the time (in milliseconds) since this connection was last used.
     */
    public long getMillisSinceLastUse() {
        return System.currentTimeMillis() - lastUsed;
    }

    /**
     * Tells whether this connection is currently in use.  This is not
     * synchronized since you probably want to synchronize at a higher level
     * (if not in use, do something), etc.
     */
    public boolean isInUse() {
        return inUse;
    }

    /**
     * Sets whether this connection is currently in use.
     * @throws java.util.ConcurrentModificationException
     *          Occurs when the connection is already in use and it is set to be
     *          in use, or it is not in use and it is set to be not in use.
     */
    public synchronized void setInUse(boolean inUse) throws ConcurrentModificationException {
        if(this.inUse == inUse)
            throw new ConcurrentModificationException();
        this.inUse = inUse;
        lastUsed = System.currentTimeMillis();
        if(!inUse) clientObject = null;
    }

    /**
     * Sets the last used time to the current time.
     */
    public void setLastUsed() {
        lastUsed = System.currentTimeMillis();
    }

    /**
     * Gets the pooled object associated with this record.
     */
    public Object getObject() {
        return object;
    }

    /**
     * Sets the client object associated with this object.  Not always used.
     */
    public void setClientObject(Object o) {
        clientObject = o;
    }
    /**
     * Gets the client object associated with this object.  If there is none,
     * returns the normal object (which is the default).
     */
    public Object getClientObject() {
        return clientObject == null ? object : clientObject;
    }

    /**
     * Shuts down this object - it will be useless thereafter.
     */
    public void close() {
        object = null;
        clientObject = null;
        created = lastUsed = Long.MAX_VALUE;
        inUse = true;
    }
}
