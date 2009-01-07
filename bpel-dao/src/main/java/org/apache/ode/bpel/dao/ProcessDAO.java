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
package org.apache.ode.bpel.dao;

import org.apache.ode.bpel.common.CorrelationKey;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * BPEL process data access objects. Contains references to active process instances ({@link ProcessInstanceDAO} and messages bound
 * for instances yet to be created or not yet correlated..
 */
public interface ProcessDAO {
    /**
     * Get the unique process identifier.
     * 
     * @return process identifier
     */
    QName getProcessId();

    /**
     * Get the BPEL process name.
     * 
     * @return qualified BPEL process name.
     */
    QName getType();

    /**
     * Get the process version
     * 
     * @return version
     */
    long getVersion();

    /**
     * Get a message correlator instance.
     * 
     * @param correlatorId
     *            correlator identifier
     * @return correlator corresponding to the given identifier
     */
    CorrelatorDAO getCorrelator(String correlatorId);

    /**
     * Create a new process instance object.
     * 
     * @param instantiatingCorrelator
     *            instantiating {@link CorrelatorDAO}
     * @return newly generated instance DAO
     */
    ProcessInstanceDAO createInstance(CorrelatorDAO instantiatingCorrelator);

    /**
     * Get a process instance (by identifier).
     * 
     * @param iid
     *            unique instance identifier.
     * @return DAO corresponding to the process instance
     */
    ProcessInstanceDAO getInstance(Long iid);

    /**
     * Locates a process instance based on a correlation key.
     * 
     * @param cckey
     *            correlation key
     * @return collection of {@link ProcessInstanceDAO} that match correlation key, ordered by date
     */
    Collection<ProcessInstanceDAO> findInstance(CorrelationKey cckey);
    Collection<ProcessInstanceDAO> findInstance(CorrelationKey cckey, boolean wait);

    /**
     * Remove the routes with the given Id for all the correlators in the process.
     * 
     * @todo remove this method.
     * @param routeId
     */
    void removeRoutes(String routeId, ProcessInstanceDAO target);

    /**
     * Callback indicating that a process instance has completed its duties.
     * 
     * @param instance
     *            the completed {@link ProcessInstanceDAO}
     */
    void instanceCompleted(ProcessInstanceDAO instance);

    /**
     * Remove the process from the database (along with any instance, variable data, etc...).
     */
    void delete();

    CorrelatorDAO addCorrelator(String correlator);

    String getGuid();

    int getNumInstances();
}
