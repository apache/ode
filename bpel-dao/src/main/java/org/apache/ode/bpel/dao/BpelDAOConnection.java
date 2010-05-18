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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.evt.BpelEvent;


/**
 * Represents the physical resource for connecting to the bpel state store.
 */
public interface BpelDAOConnection {
    /**
     * Return the DAO for a bpel process.
     *
     * @param processId name (identifier) of the process
     *
     * @return DAO
     */
    ProcessDAO getProcess(QName processId);


    /**
     * Retrieve a process instance from the database.
     * @param iid instance identifier
     * @return process instance
     */
    ProcessInstanceDAO getInstance(Long iid);

    /**
     * Retrieve a scope instance from the database.
     * @param siidl scope instance identifier
     * @return scope instance
     */
    ScopeDAO getScope(Long siidl);

    /**
     * Query instances in the database meeting the requested
     * criteria.
     * @param criteria
     * @return Collection<ProcessInstanceDAO>
     */
    Collection<ProcessInstanceDAO> instanceQuery(InstanceFilter criteria);

    Collection<ProcessInstanceDAO> instanceQuery(String expression);

    int getNumInstances(QName processId);

    /**
     * Insert a BPEL event into the database.
     * @param event a BPEL event
     * @param process associated process (optional)
     * @param instance associated instance (optional) 
     */
    void insertBpelEvent(BpelEvent event, ProcessDAO process, 
            ProcessInstanceDAO instance);

    /**
     * Execute a query for the timeline for BPEL events matching the criteria.
     * @param ifilter instance filter (optional)
     * @param efilter event filter (optional)
     * @return List of event timestamps of events matching the criteria
     */
    List<Date> bpelEventTimelineQuery(InstanceFilter ifilter, BpelEventFilter efilter);
    
    /**
     * Execute a query to retrieve the BPEL events matching the criteria.
     * @param ifilter instance filter
     * @param efilter event filter
     * @return
     */
    List<BpelEvent> bpelEventQuery(InstanceFilter ifilter, BpelEventFilter efilter);

    void close();

    Map<Long, Collection<CorrelationSetDAO>> getCorrelationSets(Collection<ProcessInstanceDAO> instances);

    Collection<CorrelationSetDAO> getActiveCorrelationSets();

    ProcessDAO createTransientProcess(Serializable id);
    
    ProcessDAO createProcess(QName pid, QName type, String guid, long version);

    /**
     * Create a message exchange.
     * @param dir type of message exchange
     * @return
     */
    MessageExchangeDAO createMessageExchange(char dir);

    MessageExchangeDAO getMessageExchange(String mexid);

    /**
     * Returns an interface for process and instance management.
     * 
     * @return a ProcessManagement DAO
     */
    ProcessManagementDAO getProcessManagement();
}
