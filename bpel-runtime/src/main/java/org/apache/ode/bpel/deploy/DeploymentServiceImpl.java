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

package org.apache.ode.bpel.deploy;

import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.DeploymentService;
import org.apache.ode.bpel.iapi.DeploymentUnit;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;

/**
 * Implementation of {@link DeploymentService}
 */
public class DeploymentServiceImpl implements DeploymentService {

    private BpelServerImpl _server;

    public DeploymentServiceImpl(BpelServerImpl server) {
        _server = server;
    }

    public Collection<QName> deploy(File deploymentUnitDirectory) {
        return _server.deploy(deploymentUnitDirectory);
    }

    public boolean undeploy(File file) {
        return _server.undeploy(file);
    }

    public String[] listDeployedPackages() {
        String[] names = new String[_server.getDeploymentUnits().size()];
        int m = 0;
        HashSet<DeploymentUnit> packageSet = new HashSet<DeploymentUnit>(_server.getDeploymentUnits());
        for (DeploymentUnit deploymentUnit : packageSet) {
            names[m++] = deploymentUnit.getDeployDir().getName();
        }
        return names;
    }

    public QName[] listProcesses(String packageName) {
        DeploymentUnit du = null;
        for (DeploymentUnit deploymentUnit : _server.getDeploymentUnits()) {
            if (deploymentUnit.getDeployDir().getName().equals(packageName))
                du = deploymentUnit;
        }
        if (du == null)
            throw new BpelEngineException("Couldn't find a deployed package named " + packageName);
        return du.getProcessNames().toArray(new QName[] {});
    }

    public String getProcessPackage(QName processId) {
        return _server.getDeploymentUnit(processId).getDeployDir().getName();
    }
}
