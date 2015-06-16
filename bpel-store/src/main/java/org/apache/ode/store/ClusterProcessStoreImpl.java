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
import org.apache.ode.bpel.clapi.ClusterManager;
import org.apache.ode.bpel.clapi.ProcessStoreDeployedEvent;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.il.config.OdeConfigProperties;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClusterProcessStoreImpl extends ProcessStoreImpl{
    private static final Log __log = LogFactory.getLog(ClusterProcessStoreImpl.class);

    private final ArrayList<ProcessConfImpl> loaded = new ArrayList<ProcessConfImpl>();
    private ClusterManager _clusterManager;
    private  ProcessStoreDeployedEvent deployedEvent;

    public ClusterProcessStoreImpl(EndpointReferenceContext eprContext, DataSource ds, String persistenceType, OdeConfigProperties props, boolean createDatamodel, ClusterManager clusterManager) {
        super(eprContext,ds,persistenceType,props,createDatamodel);
        _clusterManager = clusterManager;
        _clusterManager.setClusterProcessStore(this);
    }

    public Collection<QName> deploy(final File deploymentUnitDirectory) {
        Collection<QName> deployed = super.deploy(deploymentUnitDirectory);
        Map<QName, ProcessConfImpl> _processes = getProcessesMap();
        for (QName key :_processes.keySet()) {
            if(!loaded.contains(_processes.get(key))) loaded.add(_processes.get(key));
        }
        publishProcessStoreDeployedEvent(deploymentUnitDirectory.getName());
        return deployed;
    }

    private void publishProcessStoreDeployedEvent(String duName){
        deployedEvent = new ProcessStoreDeployedEvent(duName);
        _clusterManager.publishProcessStoreEvent(deployedEvent);
    }

    public void publishService(final String duName) {
        final ArrayList<ProcessConfImpl> confs = new ArrayList<ProcessConfImpl>();;
        ProcessState state = ProcessState.ACTIVE;

        Pattern duNamePattern = getPreviousPackageVersionPattern(duName);

        for (Iterator<ProcessConfImpl> iterator = loaded.iterator(); iterator.hasNext();) {
            ProcessConfImpl pconf = iterator.next();
            Matcher matcher = duNamePattern.matcher(pconf.getPackage());
            if (matcher.matches() && pconf.getState().equals(state)) {
                  pconf.setState(ProcessState.RETIRED);
                  confs.add(pconf);
            }
        }

        try {
            exec(new Callable<Object>() {
                public Object call(ConfStoreConnection conn) {
                    DeploymentUnitDAO dudao = conn.getDeploymentUnit(duName);
                    if (dudao != null) {
                        List<ProcessConfImpl> load = load(dudao);
                        loaded.addAll(load);
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
                fireStateChange(p.getProcessId(), p.getState(), p.getDeploymentUnit().getName());
            } catch (Exception except) {
                __log.error("Error while activating process: pid=" + p.getProcessId() + " package="+p.getDeploymentUnit().getName(), except);
            }
        }
        //loadAll();
    }

    private Pattern getPreviousPackageVersionPattern(String duName) {
        String[] nameParts = duName.split("/");
        /* Replace the version number (if any) with regexp to match any version number */
        nameParts[0] = nameParts[0].replaceAll("([-\\Q.\\E](\\d)+)?\\z", "");
        nameParts[0] += "([-\\Q.\\E](\\d)+)?";
        StringBuilder duNameRegExp = new StringBuilder(duName.length() * 2);
        for (int i = 0, n = nameParts.length; i < n; i++) {
            if (i > 0) duNameRegExp.append("/");
            duNameRegExp.append(nameParts[i]);
        }
        Pattern duNamePattern = Pattern.compile(duNameRegExp.toString());
        return duNamePattern;
    }
}
