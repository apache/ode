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

import java.io.File;
import java.util.List;

public interface ClusterManager {

    /**
     * Initialization of the cluster
     * @param file
     */
    void init(File file);

    /**
     * Return whether the local member is Master or not
     * @return
     */
    boolean getIsMaster();

    /**
     * Acquire the lock for each file in the file system
     * @param key
     * @return
     */
    boolean lock(String key);

    /**
     * Release the lock acquired by each file
     * @param key
     * @return
     */
    boolean unlock(String key);

    /**
     * Tries to acquire the lock for the specified key.
     * @param key
     * @return
     */
    boolean tryLock(String key);

    /**
     * Set the Process Store object which uses for clustering
     * @param ps
     */
    void setClusterProcessStore(ClusterProcessStore ps);

    /**
     * Publish Deploy event to the cluster by deploy initiator
     * @param clusterEvent
     */
    void publishProcessStoreClusterEvent(ProcessStoreClusterEvent clusterEvent);

    /**
     * Check whether the map has a value for given key, if absent put the value to map
     * @param key
     * @param keyVal
     */
    void putIfAbsent(String key, String keyVal);

    /**
     * Register the cluster for message listener
     */
    void registerClusterProcessStoreMessageListener();
}
