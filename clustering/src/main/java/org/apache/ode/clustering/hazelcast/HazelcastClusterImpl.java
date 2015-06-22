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

import org.apache.ode.bpel.clapi.ClusterManager;
import org.apache.ode.bpel.clapi.ProcessStoreClusterEvent;
import org.apache.ode.bpel.clapi.ProcessStoreDeployedEvent;
import org.apache.ode.bpel.clapi.ProcessStoreUndeployedEvent;
import org.apache.ode.store.ClusterProcessStoreImpl;

/**
 * This class implements necessary methods to build the cluster using hazelcast
 */
public class HazelcastClusterImpl implements ClusterManager {
    private static final Log __log = LogFactory.getLog(HazelcastClusterImpl.class);

    private HazelcastInstance _hazelcastInstance;
    private boolean isMaster = false;
    private Member leader;
    private IMap<String, String> lock_map;
    private ITopic<Object> clusterMessageTopic;
    private ClusterProcessStoreImpl _clusterProcessStore;

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
            __log.info("Registering HZ localMember ID " + localMember);
            markAsMaster();
            lock_map = _hazelcastInstance.getMap(HazelcastConstants.ODE_CLUSTER_LOCK_MAP);

            // Register for listening to message listener
            clusterMessageTopic = _hazelcastInstance.getTopic("deployedMsg");
            clusterMessageTopic.addMessageListener(new ClusterMessageListener());
        }
    }

    public void putIfAbsent(String key, String keyVal) {
        lock_map.putIfAbsent(key, keyVal);
    }

    public boolean lock(String key) {
        lock_map.lock(key);
        __log.info("ThreadID:" + Thread.currentThread().getId() + " duLocked value for " + key + " file" + " after locking: " + true);
        return true;
    }

    public boolean unlock(String key) {
        lock_map.unlock(key);
        __log.info("ThreadID:" + Thread.currentThread().getId() + " duLocked value for " + key + " file" + " after unlocking: " + false);
        return true;
    }

    public boolean tryLock(String key) {
        boolean state = lock_map.tryLock(key);
        __log.info("ThreadID:" + Thread.currentThread().getId() + " duLocked value for " + key + " file" + " after locking: " + state);
        return state;
    }

    class ClusterMemberShipListener implements MembershipListener {
        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            // Noting to do here.
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            markAsMaster();
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
            // Noting to do here.
        }
    }

    public void publishProcessStoreEvent(Object event) {
        if (event instanceof ProcessStoreClusterEvent) {
            ProcessStoreClusterEvent e = (ProcessStoreClusterEvent) event;
        e.setUuid(_hazelcastInstance.getCluster().getLocalMember().getUuid());
        clusterMessageTopic.publish(e);
        }
    }


    class ClusterMessageListener implements MessageListener<Object> {
        @Override
        public void onMessage(Message<Object> msg) {
            handleEvent(msg.getMessageObject());
        }
    }

    private void handleEvent(Object message) {
        if (message instanceof ProcessStoreDeployedEvent) {
            ProcessStoreDeployedEvent event = (ProcessStoreDeployedEvent) message;

            if (_hazelcastInstance.getCluster().getLocalMember().getUuid() != event.getUuid()) {
                String duName = event.getDuName();
                __log.info("Receive deployment msg to " + _hazelcastInstance.getCluster().getLocalMember() + " for " + duName);
                _clusterProcessStore.publishService(duName);
            }
        }

        else if (message instanceof ProcessStoreUndeployedEvent) {
            ProcessStoreUndeployedEvent event = (ProcessStoreUndeployedEvent) message;

            if (_hazelcastInstance.getCluster().getLocalMember().getUuid() != event.getUuid()) {
                String duName = event.getDuName();
                __log.info("Receive undeployment msg to " + _hazelcastInstance.getCluster().getLocalMember() + " for " + duName);
                _clusterProcessStore.undeployProcesses(duName);
            }
        }

    }

    private void markAsMaster() {
        leader = _hazelcastInstance.getCluster().getMembers().iterator().next();
        if (leader.localMember()) {
            isMaster = true;
        }
        __log.info(isMaster);
    }

    public boolean getIsMaster() {
        return isMaster;
    }

    public void setClusterProcessStore(Object store) {
        if (store instanceof ClusterProcessStoreImpl)
            _clusterProcessStore = (ClusterProcessStoreImpl) store;
    }
}

