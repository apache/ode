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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ode.bpel.clapi.ClusterManager;
import org.apache.ode.bpel.clapi.ClusterProcessStore;
import org.apache.ode.bpel.clapi.ProcessStoreDeployedEvent;
import org.apache.ode.bpel.clapi.ProcessStoreUndeployedEvent;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.il.config.OdeConfigProperties;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClusterProcessStoreImpl extends ProcessStoreImpl implements ClusterProcessStore {
    private static final Logger __log = LoggerFactory.getLogger(ClusterProcessStoreImpl.class);

    private ClusterManager _clusterManager;
    private  ProcessStoreDeployedEvent deployedEvent;
    private  ProcessStoreUndeployedEvent undeployedEvent;

    public ClusterProcessStoreImpl(EndpointReferenceContext eprContext, DataSource ds, String persistenceType, OdeConfigProperties props, boolean createDatamodel, ClusterManager clusterManager) {
        super(eprContext,ds,persistenceType,props,createDatamodel);
        _clusterManager = clusterManager;
    }

    public Collection<QName> deploy(final File deploymentUnitDirectory) {
        Collection<QName> deployed = super.deploy(deploymentUnitDirectory);
        publishProcessStoreDeployedEvent(deploymentUnitDirectory.getName());
        return deployed;
    }

    private void publishProcessStoreDeployedEvent(String duName){
        deployedEvent = new ProcessStoreDeployedEvent(duName);
        _clusterManager.publishProcessStoreClusterEvent(deployedEvent);
        __log.info("Completed actual deployment for " +duName +" by " +deployedEvent.getEventGeneratingNode());
    }

    public void deployProcesses(final String duName) {
        final ArrayList<ProcessConfImpl> confs = new ArrayList<ProcessConfImpl>();
        ProcessState state = ProcessState.ACTIVE;

        Pattern duNamePattern = getPreviousPackageVersionPattern(duName);

        for (String packageName : _deploymentUnits.keySet()) {
            Matcher matcher = duNamePattern.matcher(packageName);
            if (matcher.matches()) {
                DeploymentUnitDir duDir = _deploymentUnits.get(packageName);
                if (duDir == null) throw new ContextException("Could not find package " + packageName);
                for (QName processName : duDir.getProcessNames()) {
                    QName pid = toPid(processName, duDir.getVersion());
                    ProcessConfImpl pconf = _processes.get(pid);
                    if (pconf.getState().equals(state)) {
                        pconf.setState(ProcessState.RETIRED);
                        __log.info("Set state of " + pconf.getProcessId() + "to " + pconf.getState());
                        confs.add(pconf);
                    }
                }
            }
        }

        try {
            exec(new Callable<Object>() {
                public Object call(ConfStoreConnection conn) {
                    DeploymentUnitDAO dudao = conn.getDeploymentUnit(duName);
                    if (dudao != null) {
                        List<ProcessConfImpl> load = load(dudao);
                        __log.info("Loading DU from store: " + duName);
                        confs.addAll(load);
                    }
                    return null;
                }
            });
        } catch (Exception ex) {
            __log.error("Error loading DU from store: " + duName, ex);
        }

        for (ProcessConfImpl p : confs) {
            try {
                __log.info("Fire event of " + p.getProcessId() + " " + p.getState());
                fireStateChange(p.getProcessId(), p.getState(), p.getDeploymentUnit().getName());
            } catch (Exception except) {
                __log.error("Error with process retiring or activating : pid=" + p.getProcessId() + " package=" + p.getDeploymentUnit().getName(), except);
            }
        }
    }


    public Collection<QName> undeploy(final File dir) {
        Collection<QName> undeployed = super.undeploy(dir);
        publishProcessStoreUndeployedEvent(dir.getName());
        return undeployed;
    }

    private void publishProcessStoreUndeployedEvent(String duName){
        undeployedEvent = new ProcessStoreUndeployedEvent(duName);
        _clusterManager.publishProcessStoreClusterEvent(undeployedEvent);
        __log.info("Completed actual undeployment for " +duName +" by " +undeployedEvent.getEventGeneratingNode());
    }

    /**
     * Use to unregister processes when deployment unit is undeployed
     * @param duName
     * @return
     */
    public Collection<QName> undeployProcesses(final String duName) {
        Collection<QName> undeployed = super.undeployProcesses(duName);
        return undeployed;
    }
}
