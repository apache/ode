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

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.TopicConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.ode.bpel.clapi.*;

/**
 * This class implements necessary methods to build the cluster using hazelcast
 */
public class HazelcastClusterImpl implements ClusterManager, ProcessStoreClusterListener {
    private static final Logger __log = LoggerFactory.getLogger(HazelcastClusterImpl.class);

    private HazelcastInstance _hazelcastInstance;
    private boolean isMaster = false;
    private String nodeHostName;
    private String nodeID;
    private IMap<String, String> deployment_lock_map;
    private IMap<Long, Long> instance_lock_map;
    private ITopic<ProcessStoreClusterEvent> clusterDeploymentMessageTopic;
    private ClusterProcessStore _clusterProcessStore;
    private HazelcastDeploymentLock hazelcastDeploymentLock;
    private HazelcastInstanceLock hazelcastInstanceLock;
    private ClusterDeploymentMessageListener clusterDeploymentMessageListener;
    private ClusterMemberShipListener clusterMemberShipListener;
    private List<ClusterMemberListener> clusterMemberListenerList = null;

    public HazelcastClusterImpl() {
        clusterMemberShipListener = new ClusterMemberShipListener();
        clusterDeploymentMessageListener = new ClusterDeploymentMessageListener();
        clusterDeploymentMessageListener.registerClusterProcessStoreListener((ProcessStoreClusterListener)this);
        hazelcastDeploymentLock = new HazelcastDeploymentLock();
        hazelcastInstanceLock = new HazelcastInstanceLock();
    }


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
                    Config config = loadConfig(hzXml);
                    _hazelcastInstance = Hazelcast.newHazelcastInstance(config);
                } catch (FileNotFoundException fnf) {
                    __log.error("",fnf);
                }
        }

        if (_hazelcastInstance != null) {
            // Registering this node in the cluster.
            //_hazelcastInstance.getCluster().addMembershipListener(new ClusterMemberShipListener());
            Member localMember = _hazelcastInstance.getCluster().getLocalMember();
            nodeHostName = localMember.getSocketAddress().getHostName() + ":" + localMember.getSocketAddress().getPort();
            nodeID = localMember.getUuid();
            __log.info("Registering HZ localMember:" + nodeHostName);

            deployment_lock_map = _hazelcastInstance.getMap(HazelcastConstants.ODE_CLUSTER_DEPLOYMENT_LOCK);
            instance_lock_map = _hazelcastInstance.getMap(HazelcastConstants.ODE_CLUSTER_PROCESS_INSTANCE_LOCK);
            clusterDeploymentMessageTopic = _hazelcastInstance.getTopic(HazelcastConstants.ODE_CLUSTER_DEPLOYMENT_TOPIC);

            hazelcastDeploymentLock.setLockMap(deployment_lock_map);
            hazelcastInstanceLock.setLockMap(instance_lock_map);
            markAsMaster();
        }
    }

    protected Config loadConfig(File hazelcastConfigFile) throws FileNotFoundException {
        Config config = new FileSystemXmlConfig(hazelcastConfigFile);

        //add Cluster membership listener
        ListenerConfig clusterMemberShipListenerConfig = new ListenerConfig();
        clusterMemberShipListenerConfig.setImplementation(clusterMemberShipListener);
        config.addListenerConfig(clusterMemberShipListenerConfig);

        //set topic message listener
        ListenerConfig topicListenerConfig = new ListenerConfig();
        topicListenerConfig.setImplementation(clusterDeploymentMessageListener);
        TopicConfig topicConfig = config.getTopicConfig(HazelcastConstants.ODE_CLUSTER_DEPLOYMENT_TOPIC);
        topicConfig.addMessageListenerConfig(topicListenerConfig);

        return config;
    }

    class ClusterMemberShipListener implements MembershipListener {

        public ClusterMemberShipListener() {
            clusterMemberListenerList = new ArrayList<ClusterMemberListener>();
        }

        public void registerClusterMemberListener(ClusterMemberListener listener) {
            clusterMemberListenerList.add(listener);
        }

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            String eventNodeID = membershipEvent.getMember().getUuid();
            __log.info("Member Added " + eventNodeID);
            if (isMaster) {
                for (ClusterMemberListener listener : clusterMemberListenerList) {
                    listener.memberAdded(eventNodeID);
                }
            }
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            String eventNodeID = membershipEvent.getMember().getUuid();
            __log.info("Member Removed " + eventNodeID);
            markAsMaster();
            if (isMaster) {
                for (ClusterMemberListener listener : clusterMemberListenerList) {
                    listener.memberRemoved(eventNodeID);
                }
            }
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
            // Noting to do here.
        }
    }

    public void publishProcessStoreClusterEvent(ProcessStoreClusterEvent clusterEvent) {
        clusterEvent.setEventGeneratingNode(nodeID);
        __log.info("Send " + clusterEvent.getInfo() + " Cluster Message " + "for " + clusterEvent.getDuName() + " [" + nodeHostName + "]");
        clusterDeploymentMessageTopic.publish(clusterEvent);
    }


    class ClusterDeploymentMessageListener implements MessageListener<ProcessStoreClusterEvent> {
        List<ProcessStoreClusterListener> clusterProcessStoreListenerList = null;

        public ClusterDeploymentMessageListener() {
            clusterProcessStoreListenerList = new ArrayList<ProcessStoreClusterListener>();
        }

        public void registerClusterProcessStoreListener(ProcessStoreClusterListener listener) {
            clusterProcessStoreListenerList.add(listener);
        }

        @Override
        public void onMessage(Message<ProcessStoreClusterEvent> msg) {
            for (ProcessStoreClusterListener listener : clusterProcessStoreListenerList) {
                listener.onProcessStoreClusterEvent(msg.getMessageObject());
            }
        }
    }

    public void onProcessStoreClusterEvent(ProcessStoreClusterEvent message) {
        if (message instanceof ProcessStoreDeployedEvent) {
            ProcessStoreDeployedEvent event = (ProcessStoreDeployedEvent) message;
            String eventUuid = event.getEventGeneratingNode();
            if (!nodeID.equals(eventUuid)) {
                String duName = event.getDuName();
                __log.info("Receive " + event.getInfo() + " Cluster Message " + "for " + event.getDuName() + " [" + nodeHostName + "]");
                _clusterProcessStore.deployProcesses(duName);
            }
        }

        else if (message instanceof ProcessStoreUndeployedEvent) {
            ProcessStoreUndeployedEvent event = (ProcessStoreUndeployedEvent) message;
            String eventUuid = event.getEventGeneratingNode();
            if (!nodeID.equals(eventUuid)) {
                String duName = event.getDuName();
                __log.info("Receive " + event.getInfo() + "  Cluster Message " + "for " + event.getDuName() + " [" + nodeHostName + "]");
                _clusterProcessStore.undeployProcesses(duName);
            }
        }

    }

    private void markAsMaster() {
        Member member = _hazelcastInstance.getCluster().getMembers().iterator().next();
        if (member.localMember() && isMaster == false) {
            isMaster = true;
            for (ClusterMemberListener listener : clusterMemberListenerList) {
                listener.memberElectedAsMaster(nodeID);
            }
        }
        __log.info("Master node: " +isMaster);
    }

    public boolean isMaster() {
        return isMaster;
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setClusterProcessStore(ClusterProcessStore store) {
        _clusterProcessStore = store;
    }

    public void registerClusterProcessStoreMessageListener(ProcessStoreClusterListener listener) {
        clusterDeploymentMessageListener.registerClusterProcessStoreListener(listener);
    }

    public void registerClusterMemberListener(ClusterMemberListener listener) {
        clusterMemberShipListener.registerClusterMemberListener(listener);
    }

    public void shutdown() {
        if (_hazelcastInstance != null) _hazelcastInstance.shutdown();
    }

    public ClusterLock<String> getDeploymentLock(){
        return (ClusterLock)hazelcastDeploymentLock;
    }

    public ClusterLock<Long> getInstanceLock(){
        return (ClusterLock)hazelcastInstanceLock;
    }

    public List<String> getActiveNodes() {
        List<String> nodeList = new ArrayList<String>();
        for(Member m : _hazelcastInstance.getCluster().getMembers())
          nodeList.add(m.getUuid()) ;
        return nodeList;
    }
}

