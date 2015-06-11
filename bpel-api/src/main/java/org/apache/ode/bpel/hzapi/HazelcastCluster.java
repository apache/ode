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
package org.apache.ode.bpel.hzapi;

import com.hazelcast.core.Member;

import java.util.List;

public interface HazelcastCluster {

    /**
     * Initialization of the cluster
     */
    void init();

    /**
     * Get hostName + port nu of Member
     * @param member
     * @return
     */
    String getHazelCastNodeID(Member member);

    /**
     * Check whether current node is the leader or not.
     */
     void markAsMaster();

    /**
     * returns Current Nodes in the cluster.
     * @return
     */
    List<String> getKnownNodes();

    /**
     * Return isMaster
     * @return
     */
    boolean getIsMaster();

}
