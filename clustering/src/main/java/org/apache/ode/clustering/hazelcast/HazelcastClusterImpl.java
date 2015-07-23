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
import com.hazelcast.config.FileSystemXmlConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.ode.bpel.clapi.*;

/**
 * This class implements necessary methods to build the cluster using hazelcast
 */
public class HazelcastClusterImpl implements ClusterManager {
    private static final Log __log = LogFactory.getLog(HazelcastClusterImpl.class);

    private HazelcastInstance _hazelcastInstance;
    private boolean isMaster = false;
    private String nodeID;
    private String uuid;
    private Member leader;
    private IMap<String, String> deployment_lock_map;
    private IMap<Long, Long> instance_lock_map;
    private ITopic<ProcessStoreClusterEvent> clusterMessageTopic;
    private ClusterProcessStore _clusterProcessStore;
    private ClusterMemberListener _listener;
    private ClusterLock<String> _hazelcastDeploymentLock;
    private ClusterLock<Long> _hazelcastInstanceLock;

    public void init(File configRoot) {

        /*First,looks for the hazelcast.config system property. If it is set, its value is used as the path.
        Else it will load the hazelcast.xml file using FileSystemXmlConfig()*/

        String hzConfig = System.getProperty("hazelcast.config");
        if (hzConfig != null) _hazelcastInstance = Hazelcast.newHazelcastInstance();
        else {
            File hzXml = new File(configRoot, "hazelcast.xml");
            if (!hzXml.isFile())
                __log.error("hazelcast.xml does not exist or is not a file");
            else
                try {
                    _hazelcastInstance = Hazelcast.newHazelcastInstance(new FileSystemXmlConfig(hzXml));
                } catch (FileNotFoundException fnf) {
                    __log.error(fnf);
                }
        }

        if (_hazelcastInstance != null) {
            // Registering this node in the cluster.
            _hazelcastInstance.getCluster().addMembershipListener(new ClusterMemberShipListener());
            Member localMember = _hazelcastInstance.getCluster().getLocalMember();
            nodeID = localMember.getInetSocketAddress().getHostName() +":" +localMember.getInetSocketAddress().getPort();
            uuid = localMember.getUuid();
            __log.info("Registering HZ localMember ID " + nodeID);

            markAsMaster();

            deployment_lock_map = _hazelcastInstance.getMap(HazelcastConstants.ODE_CLUSTER_DEPLOYMENT_LOCK);
            instance_lock_map = _hazelcastInstance.getMap(HazelcastConstants.ODE_CLUSTER_PROCESS_INSTANCE_LOCK);
            clusterMessageTopic = _hazelcastInstance.getTopic(HazelcastConstants.ODE_CLUSTER_MSG);

            _hazelcastDeploymentLock = (ClusterLock) new HazelcastDeploymentLock(deployment_lock_map);
            _hazelcastInstanceLock = (ClusterLock) new HazelcastInstanceLock(instance_lock_map);
        }
    }

    class ClusterMemberShipListener implements MembershipListener {
        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            String nodeId =  membershipEvent.getMember().getUuid();
            __log.info("Member Added " +nodeId);
            if(isMaster && _listener != null) _listener.memberAdded(nodeId);
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            String nodeId = membershipEvent.getMember().getUuid();
            __log.info("Member Removed " + nodeId);
            markAsMaster();
            if(isMaster && _listener != null) _listener.memberRemoved(nodeId);
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
            // Noting to do here.
        }
    }

    public void publishProcessStoreClusterEvent(ProcessStoreClusterEvent clusterEvent) {
        clusterEvent.setUuid(uuid);
        __log.info("Send " +clusterEvent.getInfo() +" Cluster Message " +"for " +clusterEvent.getDuName() +" [" +nodeID +"]");
        clusterMessageTopic.publish(clusterEvent);
    }


    class ClusterMessageListener implements MessageListener<ProcessStoreClusterEvent> {
        @Override
        public void onMessage(Message<ProcessStoreClusterEvent> msg) {
            handleEvent(msg.getMessageObject());
        }
    }

    private void handleEvent(ProcessStoreClusterEvent message) {
        if (message instanceof ProcessStoreDeployedEvent) {
            ProcessStoreDeployedEvent event = (ProcessStoreDeployedEvent) message;
            String eventUuid =  event.getUuid();
            if (!uuid.equals(eventUuid)) {
                String duName = event.getDuName();
                __log.info("Receive " +event.getInfo() +" Cluster Message " +"for " +event.getDuName() +" [" +nodeID +"]");
                _clusterProcessStore.deployProcesses(duName);
            }
        }

        else if (message instanceof ProcessStoreUndeployedEvent) {
            ProcessStoreUndeployedEvent event = (ProcessStoreUndeployedEvent) message;
            String eventUuid =  event.getUuid();
            if (!uuid.equals(eventUuid)) {
                String duName = event.getDuName();
                __log.info("Receive " +event.getInfo() +"  Cluster Message " +"for " +event.getDuName() +" [" +nodeID +"]");
                _clusterProcessStore.undeployProcesses(duName);
            }
        }

    }

    private void markAsMaster() {
        leader = _hazelcastInstance.getCluster().getMembers().iterator().next();
        if (leader.localMember() && isMaster == false) {
            isMaster = true;
            if(_listener != null) _listener.memberElectedAsMaster(uuid);
        }
        __log.info(isMaster);
    }

    public boolean getIsMaster() {
        return isMaster;
    }

    public String getUuid() {
        return uuid;
    }

    public void setClusterProcessStore(ClusterProcessStore store) {
        _clusterProcessStore = store;
    }

    public void registerClusterProcessStoreMessageListener() {
        clusterMessageTopic.addMessageListener(new ClusterMessageListener());
    }

    public void registerClusterMemberListener(ClusterMemberListener listener) {
        _listener = listener;
    }

    public void shutdown() {
        if(_hazelcastInstance != null) _hazelcastInstance.getLifecycleService().shutdown();
    }

    public ClusterLock<String> getDeploymentLock(){
        return _hazelcastDeploymentLock;
    }

    public ClusterLock<Long> getInstanceLock(){
        return _hazelcastInstanceLock;
    }

    public List<String> getActiveNodes() {
        List<String> nodeList = new ArrayList<String>();
        for(Member m : _hazelcastInstance.getCluster().getMembers())
          nodeList.add(m.getUuid()) ;
        return nodeList;
    }
}

