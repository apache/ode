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

public interface ClusterManager {

    /**
     * Initialization of the cluster
     * @param file
     */
    void init(File file);

    /**
     * shutdown the cluster instance
     */
    void shutdown();

    /**
     * Return whether the local member is Master or not
     * @return
     */
    boolean getIsMaster();

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
     * Register the cluster for message listener
     */
    void registerClusterProcessStoreMessageListener();

    /**
     * Return deployment lock for cluster
     */
    ClusterLock getDeploymentLock();

    /**
     * Return instance lock for cluster
     */
    ClusterLock getInstanceLock();
}
