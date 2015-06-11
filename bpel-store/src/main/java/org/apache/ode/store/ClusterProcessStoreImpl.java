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

public class ClusterProcessStoreImpl extends ProcessStoreImpl{
    private static final Log __log = LogFactory.getLog(ClusterProcessStoreImpl.class);

    private HazelcastClusterImpl _hazelcastClusterImpl;

    public ClusterProcessStoreImpl(EndpointReferenceContext eprContext, DataSource ds, String persistenceType, OdeConfigProperties props, boolean createDatamodel, HazelcastClusterImpl hazelcastClusterImpl) {
        super();
        _hazelcastClusterImpl = hazelcastClusterImpl;
    }

    public Collection<QName> deploy(final File deploymentUnitDirectory) {
        Collection<QName> deployed = super.deploy(deploymentUnitDirectory);
        publishProcessStoreDeployedEvent(deploymentUnitDirectory.getName());
        return deployed;
    }

    public void publishProcessStoreDeployedEvent(String duName){
       String returnedDuName = _hazelcastClusterImpl.publishProcessStoreEvent("Deployed " +duName);
       publishService(returnedDuName);
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

}
