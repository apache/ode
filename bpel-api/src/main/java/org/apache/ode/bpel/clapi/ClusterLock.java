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
package org.apache.ode.bpel.clapi;

import java.util.concurrent.TimeUnit;

public interface ClusterLock<E> {
    /**
     * Acquire the lock for specified key
     *
     * @param key
     * @return
     */
    void lock(E key);

    /**
     * Acquire the lock for specified key and time period
     *
     *
     * @param key
     * @return
     */
    void lock(E key,int time,TimeUnit tu) throws InterruptedException, TimeoutException;

    /**
     * Release the lock acquired for specified key
     *
     * @param key
     * @return
     */
    void unlock(E key);

    /**
     * Tries to acquire the lock for the specified key
     * @param key
     * @return
     */
    boolean tryLock(E key);

    /**
     * Tries to acquire the lock for the specified key and time period.
     * @param key
     * @param time
     * @param tu
     * @return
     */
    boolean tryLock(E key, int time, TimeUnit tu);

    /**
     * Check whether the map has a value for given key, if absent put the value to map
     * @param key
     * @param keyVal
     */
    void putIfAbsent(E key, E keyVal);

    /** Exception class indicating a time-out occured  while obtaining a lock. */
    public static final class  TimeoutException extends Exception {
        private static final long serialVersionUID = 7247629086692580285L;
    }
}
