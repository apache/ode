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

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Date;

/**
 * DAO interface for a "deployment unit", a collection of processes deployed as a single
 * unit.
 *
 * @author mszefler
 *
 */
public interface DeploymentUnitDAO {

    /**
     * Get the name of the deployment unit.
     * @return du name
     */
    String getName();

    /**
     * Get the deployment unit directory path.
     * @return deployment unit directory path
     */
    String getDeploymentUnitDir();


    void setDeploymentUnitDir(String dir);

    /**
     * Get the collection of processes that are deployed as part of this deployment unit.
     * @return
     */
    Collection<? extends ProcessConfDAO> getProcesses();

    /**
     * Get the date/time the DU was deployed.
     * @return
     */
    Date getDeployDate();

    /**
     * Get the userid of the user doing the deploying.
     * @return
     */
    String getDeployer();


    /**
     * Delete this deployment unit (deletes all the children).
     */
    void delete();

    ProcessConfDAO createProcess(QName pid, QName type, long version);

    ProcessConfDAO getProcess(QName pid);

}
