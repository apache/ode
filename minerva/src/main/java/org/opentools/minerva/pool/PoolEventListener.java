/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.pool;

/**
 * A listener for object pool events.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public interface PoolEventListener {
    /**
     * The pooled object was closed and should be returned to the pool.
     */
    public void objectClosed(PoolEvent evt);
    /**
     * The pooled object had an error and should be returned to the pool.
     */
    public void objectError(PoolEvent evt);
    /**
     * The pooled object was used and its timestamp should be updated.
     */
    public void objectUsed(PoolEvent evt);
}
