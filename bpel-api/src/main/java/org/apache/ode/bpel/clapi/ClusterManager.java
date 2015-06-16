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
     * Check whether current node is the leader or not.
     */
     void markAsMaster();

    /**
     * Return isMaster
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
    void setClusterProcessStore(Object ps);

    /**
     * Publish Deploy event to the cluster by deploy initiator
     * @param event
     */
    void publishProcessStoreEvent(Object event);

    /**
     * Handle event according to received event
     * @param message
     */
    void handleEvent(Object message);
}
