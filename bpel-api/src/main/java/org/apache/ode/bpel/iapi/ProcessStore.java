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

import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * @author mriou <mriou at apache dot org>
 */
public interface ProcessStore {
    /**
     * Deploys a process from the filesystem.
     * @param deploymentUnitDirectory directory containing all deployment files
     * @return a collection of process ids (deployed processes)
     */
    Collection<QName> deploy(File deploymentUnitDirectory);

    /**
     * Undeploys a package.
     * @param file package
     * @return collection of successfully deployed process names
     */
    Collection<QName> undeploy(File file);

    /**
     * Lists the names of all the packages that have been deployed (corresponds
     * to a directory name on the file system).
     * @return an array of package names
     */
    Collection<String> getPackages();

    /**
     * Lists all processe ids in a given package.
     * @return an array of process id QNames
     */
    List<QName> listProcesses(String packageName);

    /**
     * Get the list of processes known to the store.
     * @return list of  processes qnames with their compiled definition
     */
    List<QName> getProcesses();


    /**
     * Gets all the details of a process configuration (properties, deploy dates, ...)
     * @param processId
     * @return process configuration details
     */
    ProcessConf getProcessConfiguration(QName processId);


    /**
     * Register a configuration store listener.
     * @param psl  {@link ProcessStoreListener}
     */
    void registerListener(ProcessStoreListener psl);

    /**
     * Unregister a configuration store listener.
     * @param psl {@link ProcessStoreListener}
     */
    void unregisterListener(ProcessStoreListener psl);


    /**
     * Set a process property.
     * @param pid
     * @param propName
     * @param value
     */
    void setProperty(QName pid, QName propName, String value);

    void setProperty(QName pid, QName propName, Node value);

    /**
     * Marks a process as active / retired or disabled
     * @param pid
     * @param state true for active, false for inactive
     */
    void setState(QName pid, ProcessState state);

    /**
     * Retires all processes in a given package.
     * @param packageName
     * @param retired
     */
    void setRetiredPackage(String packageName, boolean retired);

    /**
     * Gets the version used by the store for the last deployment.
     * @return
     */
    long getCurrentVersion();

    void refreshSchedules(String packageName);
}
