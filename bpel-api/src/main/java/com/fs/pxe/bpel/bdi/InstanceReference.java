/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.bdi;

import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import com.fs.pxe.bpel.evt.BpelEvent;

/**
 * <p>
 * Reference to a process instance with capabilities for introspection and sending
 * signals.
 * </p>
 * <p>
 * <a name="note">Signals</a> are interpreted as follows:
 * </p>
 * <ul>
 * <li>Signals function analgously to the UNIX <code>kill</code> command
 * in that the method will complete normally once the signal is sent.  Determining
 * whether or not the process is actually terminated can be accomplished via a
 * call (or potentially multiple calls) to {@link #getState()}.</li>
 * <li>Inappropriate (stepping a terminated instance) or superfluous signals
 * (terminating a terminated instance) are ignored.</li>
 * </ul>
 */
public interface InstanceReference {
  
  /**
   * Get the time that the instance was generated (first receive) according to the
   * server.
   * @return the time.
   */
  public Date getStartTime() throws DebuggerException;
  
  /** 
   * Get the identifier for the current state of the process.
   * @return the state identifier.
   * @see com.fs.pxe.bpel.common.ProcessState
   */
  public short getState() throws DebuggerException;
  
  /**
   * If the process completed with a fault, get the <code>QName</code> of the 
   * fault.
   * @return the <code>QName</code> of the fault for the completed process or
   * <code>null</code> if none occurred. 
   * @see com.fs.pxe.bpel.common.ProcessState#STATE_COMPLETED_WITH_FAULT
   */
  public QName getCompletedFault() throws DebuggerException;
  
	/**
   * <p>
   * Send a signal to the process to terminate.  See the notes <a href="#notes">above</a> for more
   * information on how signals are handled.
   * </p>
   */
	public void terminate() throws DebuggerException;
	
	/**
   * <p>
   * Send a signal to the process to resume.  See the notes <a href="#notes">above</a> for more
   * information on how signals are handled.
   * </p>
   */
	public void resume() throws DebuggerException;
  
  /**
   * <p>
   * Send a signal to the process to suspend.  See the notes <a href="#notes">above</a> for more
   * information on how signals are handled.
   * </p>
   */
  public void suspend() throws DebuggerException;
  
  /**
   * <p>
   * Send a signal to the process to take a single step, where a step:
   * </p>
   * <ul>
   *   <li>Spans the completion of one activity to the start of another.</li>
   *   <li>The start of one activity to its completion.</li>
   * </ul>
   * <p>
   * See the notes <a href="#notes">above</a> for more information about how signals are handled.
   * </p>
   */
  public void step() throws DebuggerException;
  
  /**
   * Get a (human-readable) identifier for the process instance.  (PID = <em>P</em>rocess
   * <em>ID</em>entifier)
   * @return the identifier.
   */
  public Long getInstanceId() throws DebuggerException;
  
  /**
   * <p>
   * Retrieve the events for this process, starting at the provided index.
   * </p>
   * @param startIdx start index (zero-based)
   * @param count the number of events to return
   * @return an array of events.
   */
  public List<BpelEvent> getEvents(int startIdx, int count) throws DebuggerException;
 
  /**
   * <p>
   * Get the number of events that have occurred in process execution from start up
   * to the present.
   * </p>
   * @return the number of events.
   */
  public int eventCount() throws DebuggerException;
  
  /**
   * <p>
   * Obtain a break point request manager for this instance.  The manager can be used
   * to add and remove specific break points on this process instance.
   * </p>
   * <p>
   * <em>Note:</em> The sets of instance break points and the set of process-level
   * break points are separate but need not be disjoint.
   * </p>
   * @return a manager instance.
   * @see ProcessReference#getBreakpointManager()
   */
  public BreakpointManager getBreakpointManager() throws DebuggerException;
  
  /**
   * <p>
   * Obtain a variable reference based on a specific scope and variable name.
   * </p>
   * @param scopeId the current scope id (not necessarily the scope of the variable, but must be child scope of the variable scope)  
   * @param variableName the variable name
   * @return the reference or <code>null</code> if no such variable or scope instance
   * exists.
   */
  public Variable getVariable(Long scopeId, String variableName) throws DebuggerException;
  
  /**
   * <p>
   * Retrieves all possible variables for a given scope name, i.e., for all instances
   * of a scope.  In particular, multiple results may be returned if the scope is
   * contained within a <code>&lt;while&gt;</code> activity.
   * </p>
   * @param scopeName the name of the scope (as an activity)
   * @param variableName
   * @return all variables for the scope name.
   * @see #getVariable(String, String)
   */
  public Variable[] getVariables(String scopeName, String variableName) throws DebuggerException;
  
  
  /**
   * <p>
   * Retrieves the correlation set data.
   * </p>
   * @param scopeId the current scope id (not necessarily the scope of the variable, but must be child scope of the variable scope)  
   * @param correlationSetName name of the correlation set
   * @return
   */
  public Correlation getCorrelation(Long scopeId, String correlationSetName) throws DebuggerException;
  
  /**
   * Retrieves all correlation set data for a scope referenced by name.
   * @param scopeName the name of the scope  
   * @param correlationSetName name of the correlation set
   * @return
   */
  public Correlation[] getCorrelations(String scopeName, String correlationSetName) throws DebuggerException;
}
