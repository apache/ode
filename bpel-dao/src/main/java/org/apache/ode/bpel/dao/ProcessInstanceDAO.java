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

import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.List;

import javax.xml.namespace.QName;


/**
 * BPEL process instance data access object. This object serves as the root
 * object for data related to a particular process instance; this state
 * includes auditing events, scopes, pick/received waiters, and the
 * serialized process instance image.
 */
public interface ProcessInstanceDAO {

    /**
     * Get the time when the process instance was created.
     * @return time of instance creation
     */
    public Date getCreateTime();
    
    /**
     * Get the time when the process instance was last active (re-hydrated).
     * @return time of activity
     */
    public Date getLastActiveTime();
    
    /**
     * Set last activity time for the process instance
     * @param dt tiem of activity
     */
    void setLastActiveTime(Date dt);

    /**
     * The un-caught fault associated with the process. This will be
     * <code>null</code> if no fault occurred or if all faults are caught and
     * processed.
     * @param fault the fault
     */
    void setFault(FaultDAO fault);
    
    void setFault(QName faultName, String explanation, int faultLineNo, int activityId, Element faultMessage);

    /**
     * The un-caught fault associated with the process. This will be
     * <code>null</code> if no fault occurred or if all faults are caught and
     * processed.
     *
     * @return the fault
     */
    FaultDAO getFault();

    /**
     * Get the (opaque) instance execution state.
     * @return opaque execution state
     */
    byte[] getExecutionState();

    /**
     * Set the (opaque) instance execution state.
     * @param execState execuction state
     */
    void setExecutionState(byte[] execState);

    /**
     * Get the process.
     *
     * @return process reference.
     */
    ProcessDAO getProcess();

    /**
     * Get the root (global) scope for the process.
     *
     * @return the root scope
     */
    ScopeDAO getRootScope();

    /**
     * Set the state of the process instance; one of the <code>STATE_XXX</code>
     * constants defined in ProcessState.
     * 
     * This should automatically populate the previous state.
     *
     * @param state new state of the process instance
     */
    void setState(short state);

    /**
     * Get the state of the process instance; one of the <code>STATE_XXX</code>
     * constants defined in ProcessState.
     *
     * @return state of process instance
     */
    short getState();
    
    /**
     * Returns the next to last state. 
     * @return
     */
    short getPreviousState();

    /**
     * Creates a new scope.
     *
     * @param parentScope parent scope of the new scope, or null if this is the
     *        root scope.
     * @param name scope name
     *
     * @return the newly created scope
     */
    ScopeDAO createScope(ScopeDAO parentScope, String name, int scopeModelId);

    /**
     * Get the instance identifier.
     * @return the instance identifier
     */
    Long getInstanceId();

    /**
     * Returns a scope using its instance id.
     * @param scopeInstanceId
     * @return
     */
    ScopeDAO getScope(Long scopeInstanceId);
    
    /** 
     * Returns all the scopes with the associated name.
     * @param scopeName
     * @return
     */
    Collection<ScopeDAO> getScopes(String scopeName);
    
    /** 
     * Returns all the scopes belonging to this isntance.
     * @param scopeName
     * @return
     */
    Collection<ScopeDAO> getScopes();
    
    /**
     * Return the correlator which results in the instantiation of the process instance.
     * @return
     */
    CorrelatorDAO getInstantiatingCorrelator();
    
    /**
     * Returns all variable instances matching the variable name for a specified scope.
     */
    XmlDataDAO[] getVariables(String variableName, int scopeModelId);

    /**
     * Get all the correlation sets for this process.
     * @return {@link Set} of {@link CorrelationSetDAO} objects
     */
    Set<CorrelationSetDAO> getCorrelationSets();

    /**
     * Get a correlation set by its name from this process
     * @param name
     * @return a {@link CorrelationSetDAO} object
     */
    CorrelationSetDAO getCorrelationSet(String name);

    /**
     * A simple callback to allow the ProcessInstance to perform post-completion duties.
     * The DAO's state indicates whether any fault has occured.
     */
    void finishCompletion();

    /**
     * Delete the process instance object from the database.
     */
    void delete(Set<CLEANUP_CATEGORY> cleanupCategories);

    /**
     * Insert a BPEL event to the database (associating with this process).
     * @param event BPEL event
     */
    void insertBpelEvent(ProcessInstanceEvent event);

    /**
     * Get a triple containing the first
     * @return
     */
    EventsFirstLastCountTuple getEventsFirstLastCount();

    /** 
     * Get the next number from a monotonically increasing sequence.
     * @return next number in seqeunce
     */
    public long genMonotonic();

    public BpelDAOConnection getConnection();

    /**
     * Get number of activities in the failure state.
     */
    int getActivityFailureCount();

    /**
     * Get date/time of last activity failure.
     */
    Date getActivityFailureDateTime();

    /**
     * Returns all activity recovery objects for this process instance.
     */
    Collection<ActivityRecoveryDAO> getActivityRecoveries();

    /**
     * Create an activity recovery object for a given activity instance.
     * Specify the reason and optional data associated with the failure.
     * Date/time failure occurred, and the recovery channel and available
     * recovery actions.
     */
    void createActivityRecovery(String channel, long activityId, String reason, Date dateTime, Element data, String[] actions, int retries);

    /**
     * Delete previously registered activity recovery.
     */
    void deleteActivityRecovery(String channel);

    /**
     * Transport object holding the date of the first and last instance event
     * along with the number events.
     */
    public class EventsFirstLastCountTuple {
        public Date first;
        public Date last;
        public int count;
    }

    Collection<String> getMessageExchangeIds();
}
