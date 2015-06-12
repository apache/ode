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
package org.apache.ode.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.clustering.hazelcast.HazelcastClusterImpl;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.il.config.OdeConfigProperties;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import com.hazelcast.core.*;

public class ClusterProcessStoreImpl extends ProcessStoreImpl{
    private static final Log __log = LogFactory.getLog(ClusterProcessStoreImpl.class);

    private HazelcastInstance _hazelcastInstance;
    private Member deployInitiator;
    private ITopic<String> clusterMessageTopic;

    public ClusterProcessStoreImpl(EndpointReferenceContext eprContext, DataSource ds, String persistenceType, OdeConfigProperties props, boolean createDatamodel, HazelcastClusterImpl hazelcastClusterImpl) {
        super(eprContext,ds,persistenceType,props,createDatamodel);
        _hazelcastInstance = hazelcastClusterImpl.getHazelcastInstance();

        // Register for listening to message listener
        clusterMessageTopic = _hazelcastInstance.getTopic("deployedMsg");
        clusterMessageTopic.addMessageListener(new ClusterMessageListener());
    }

    public Collection<QName> deploy(final File deploymentUnitDirectory) {
        Collection<QName> deployed = super.deploy(deploymentUnitDirectory);
        publishProcessStoreDeployedEvent(deploymentUnitDirectory.getName());
        return deployed;
    }

    public void publishProcessStoreDeployedEvent(String duName){
        deployInitiator = _hazelcastInstance.getCluster().getLocalMember();
        clusterMessageTopic.publish("Deployed " +duName);
    }

    public void publishService(final String duName) {
        final ArrayList<ProcessConfImpl> loaded = new ArrayList<ProcessConfImpl>();
        try {
            exec(new Callable<Object>() {
                public Object call(ConfStoreConnection conn) {
                    DeploymentUnitDAO dudao = conn.getDeploymentUnit(duName);
                    if (dudao != null) {
                        loaded.addAll(load(dudao));
                    }
                    return null;
                }
            });
        } catch (Exception ex) {
            __log.error("Error loading DU from store: " + duName, ex);
        }

        for (ProcessConfImpl p : loaded) {
            try {
                fireStateChange(p.getProcessId(), p.getState(), p.getDeploymentUnit().getName());
            } catch (Exception except) {
                __log.error("Error while activating process: pid=" + p.getProcessId() + " package="+p.getDeploymentUnit().getName(), except);
            }
        }
    }

    class ClusterMessageListener implements MessageListener<String> {
        @Override
        public void onMessage(Message<String> msg) {
            String message = msg.getMessageObject();
            String arr[] = message.split(" ", 2);
            String duName = arr[1];
            if(message.contains("Deployed ")) {
                if(_hazelcastInstance.getCluster().getLocalMember() != deployInitiator) {
                    __log.info("Receive deployment msg to " +_hazelcastInstance.getCluster().getLocalMember() +" for " +duName);
                    publishService(duName);
                }
            }
        }
    }


}
