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
package org.apache.ode.clustering.hazelcast;

import com.hazelcast.core.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.ode.bpel.hzapi.HazelcastCluster;

/**
 * This class implements necessary methods to build the cluster using hazelcast
 */
public class HazelcastClusterImpl implements HazelcastCluster{
    private static final Log __log = LogFactory.getLog(HazelcastClusterImpl.class);

    private HazelcastInstance _hazelcastInstance;
    private boolean isMaster = false;
    private String message = "";
    private Member leader;

    private IMap<String, String> lock_map;

    public HazelcastClusterImpl(HazelcastInstance hazelcastInstance) {
        _hazelcastInstance = hazelcastInstance;
        init();
    }

    public void init() {
        // Registering this node in the cluster.
        _hazelcastInstance.getCluster().addMembershipListener(new ClusterMemberShipListener());

        // Register for listening to message listener
        ITopic<String> clusterMessageTopic = _hazelcastInstance.getTopic("clusterMsg");
        clusterMessageTopic.addMessageListener(new ClusterMessageListener());

        Member localMember = _hazelcastInstance.getCluster().getLocalMember();
        String localMemberID = getHazelCastNodeID(localMember);
        __log.info("Registering HZ localMember ID " + localMemberID);
        _hazelcastInstance.getMap(HazelcastConstants.ODE_CLUSTER_NODE_MAP)
                .put(localMemberID, isMaster);

        lock_map = _hazelcastInstance.getMap(HazelcastConstants.ODE_CLUSTER_LOCK_MAP);
    }

    public String getHazelCastNodeID(Member member) {
        String hostName = member.getSocketAddress().getHostName();
        int port = member.getSocketAddress().getPort();
        return hostName + ":" + port;
    }

    public void lock(String key) {
        lock_map.lock(key);
    }

    public void unlock(String key) {
        lock_map.unlock(key);
    }

    class ClusterMemberShipListener implements MembershipListener {

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            // Noting to do here.
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            isLeader();
            // Allow Leader to update distributed map.
            if (isMaster) {
                String leftMemberID = getHazelCastNodeID(membershipEvent.getMember());
                _hazelcastInstance.getMap(HazelcastConstants.ODE_CLUSTER_NODE_MAP).remove(leftMemberID);
                _hazelcastInstance.getMap(HazelcastConstants.ODE_CLUSTER_NODE_MAP).replace(getHazelCastNodeID(leader), isMaster);
            }
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
            // Noting to do here.
        }
    }

    class ClusterMessageListener implements MessageListener<String> {
        @Override
        public void onMessage(Message<String> msg) {
            message = msg.getMessageObject();
        }
    }


    public void isLeader() {
        leader = _hazelcastInstance.getCluster().getMembers().iterator().next();
        if (leader.localMember()) {
            isMaster = true;
        }
        __log.info(isMaster);
    }

    public List<String> getKnownNodes() {
        List<String> nodeList = new ArrayList<String>();
        for (Object s : _hazelcastInstance.getMap(HazelcastConstants.ODE_CLUSTER_NODE_MAP).keySet()) {
            nodeList.add((String) _hazelcastInstance.getMap(HazelcastConstants.ODE_CLUSTER_NODE_MAP).get(s));
        }
        return nodeList;
    }

    public boolean getIsMaster() {
        return isMaster;
    }

    public String getMessage() {
        return message;
    }

}
