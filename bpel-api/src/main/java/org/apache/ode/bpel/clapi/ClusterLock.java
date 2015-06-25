package org.apache.ode.bpel.clapi;

public interface ClusterLock {
    /**
     * Acquire the lock for each file in the file system
     *
     * @param key
     * @return
     */
    boolean lock(String key);

    /**
     * Release the lock acquired by each file
     *
     * @param key
     * @return
     */
    boolean unlock(String key);

    /**
     * Tries to acquire the lock for the specified key.
     *
     * @param key
     * @return
     */
    boolean tryLock(String key);
}
