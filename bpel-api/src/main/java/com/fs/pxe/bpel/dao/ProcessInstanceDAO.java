/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.dao;

import com.fs.pxe.bpel.evt.ProcessInstanceEvent;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

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
  void delete();

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
  * Transport object holding the date of the first and last instance event
  * along with the number events.
  */
  public class EventsFirstLastCountTuple {
    public Date first;
    public Date last;
    public int count;
  }



  

}
