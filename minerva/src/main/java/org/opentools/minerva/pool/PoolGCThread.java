/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.pool;

import java.util.*;

/**
 * Runs garbage collection on all available pools.  Only one GC thread is
 * created, no matter how many pools there are - it just tries to calculate
 * the next time it should run based on the figues for all the pools.  It will
 * run on any pools which are "pretty close" to their requested time.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
class PoolGCThread extends Thread {
    private Set<ObjectPool> pools = new HashSet<ObjectPool>();

    PoolGCThread() {
        super("Minerve ObjectPool GC Thread");
        setDaemon(true);
    }

    public void run() {
        while(true) {
            // Don't do anything while there's nothing to do
            waitForPools();
            // Figure out how long to sleep
            long delay = getDelay();
            // Sleep
            if(delay > 0l) {
                try {
                    sleep(delay);
                } catch(InterruptedException e) {}
            }
            // Run garbage collection on eligible pools
            runGC();
        }
    }

    private synchronized void waitForPools() {
        while(pools.size() == 0) {
            try {
                wait();
            } catch(InterruptedException e) {
            }
        }
    }

    private synchronized long getDelay() {
        long next = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        long current;
        for(Iterator it = pools.iterator(); it.hasNext();) {
            ObjectPool pool = (ObjectPool)it.next();
            current = pool.getNextGCMillis(now);
            if(current < next) next = current;
        }
        return next >= 0l ? next : 0l;
    }

    private synchronized void runGC() {
        for(Iterator<ObjectPool> it = pools.iterator(); it.hasNext();) {
            ObjectPool pool = it.next();
            if(pool.isTimeToGC()) {
                pool.runGCandShrink();
            }
        }
    }

    synchronized void addPool(ObjectPool pool) {
        if(pool.isGCEnabled()) {
            pools.add(pool);
        }
        notify();
    }

    synchronized void removePool(ObjectPool pool) {
        pools.remove(pool);
    }
}
