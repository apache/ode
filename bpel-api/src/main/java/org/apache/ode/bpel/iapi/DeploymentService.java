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

package org.apache.ode.bpel.iapi;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Collection;

/**
 * Provides service deployment and undeployment as well as simple query
 * methods to check which packages have been deployed and the related
 * process names.
 */
public interface DeploymentService {

    /**
     * Deploys a process from the filesystem.
     * @param deploymentUnitDirectory directory containing all deployment files
     * @return a collection of process ids (deployed processes)
     */
    Collection<QName> deploy(File deploymentUnitDirectory);

    /**
     * Undeploys a package.
     * @param file package
     * @return successful or not
     */
    boolean undeploy(File file);

    /**
     * Lists the names of all the packages that have been deployed (corresponds
     * to a directory name on the file system).
     * @return an array of package names
     */
    String[] listDeployedPackages();

    /**
     * Lists all processe ids in a given package.
     * @return an array of process id QNames
     */
    QName[] listProcesses(String packageName);

    /**
     * Gets the name of the package into which a process is deployed.
     * @param processId
     * @return package name
     */
    String getProcessPackage(QName processId);
}
