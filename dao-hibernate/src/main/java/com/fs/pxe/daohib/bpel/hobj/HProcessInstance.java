/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel.hobj;

import com.fs.pxe.daohib.hobj.HObject;
import com.fs.pxe.daohib.hobj.HLargeData;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Hibernate table representing a BPEL process instance.
 * @hibernate.class table="BPEL_INSTANCE" dynamic-update="true"
 */
public class HProcessInstance extends HObject{
  /** Foreign key to owner {@link HProcess}. */
  private HProcess _process;

  /** Foreign key to the instantiating {@link HCorrelator}. */
  private HCorrelator _instantiatingCorrelator;

  /** Scope instances belonging to this process instnace. */
  private Collection<HScope> _scopes = new HashSet<HScope>();
  
  private Collection<HCorrelationSet> _csets = new HashSet<HCorrelationSet>();

  /** Events belonging to this instance. */
  private Collection<HBpelEvent> _events = new HashSet<HBpelEvent>();
  private Set<HCorrelatorSelector> _correlatorSelectors = new HashSet<HCorrelatorSelector>();
  
  private HFaultData _fault;
  private HLargeData _jacobState;
  private short _previousState;
  private short _state;
  private Date _lastActiveTime;

  private long _seq;
  
  /**
	 * 
	 */
	public HProcessInstance() {
		super();
	}
  
   /**
   * @hibernate.many-to-one
   *    column="INSTANTIATING_CORRELATOR"
   */
	public HCorrelator getInstantiatingCorrelator() {
		return _instantiatingCorrelator;
	}

	public void setInstantiatingCorrelator(HCorrelator instantiatingCorrelator) {
		_instantiatingCorrelator = instantiatingCorrelator;
	}

  /**
   * @hibernate.many-to-one column="FAULT" cascade="delete"
   *  column="FAULT"
   */
	public HFaultData getFault() {
		return _fault;
	}

  public void setFault(HFaultData fault) {
		_fault = fault;
	}

  /**
   * @hibernate.many-to-one column="JACOB_STATE" cascade="delete"
   */
	public HLargeData getJacobState() {
		return _jacobState;
	}

  public void setJacobState(HLargeData jacobState) {
		_jacobState = jacobState;
	}
  

  /**
   * @hibernate.bag
   *  lazy="true"
   *  inverse="true"
   *  cascade="delete"
   * @hibernate.collection-key
   *  column="IID"
   * @hibernate.collection-one-to-many
   *  class="com.fs.pxe.daohib.bpel.hobj.HBpelEvent"
   */
  public Collection<HBpelEvent> getEvents() {
    return _events;
  }

  
  public void setEvents(Collection<HBpelEvent> events) {
    _events = events;
  }
  
  /**
   * @hibernate.set
   *    lazy="true"
   *    inverse="true"
   *    cascade="delete"
   * @hibernate.collection-key
   *    column="PIID"
   * @hibernate.collection-one-to-many
   *    class="com.fs.pxe.daohib.bpel.hobj.HCorrelatorSelector"
   */
  public Set<HCorrelatorSelector> getCorrelatorSelectors() {
    return _correlatorSelectors;
  }
  
  /**
   * @param selectors the _correlatorSelectors to set
   */
  public void setCorrelatorSelectors(Set<HCorrelatorSelector> selectors) {
    _correlatorSelectors = selectors;
  }
  /**
    * @hibernate.property
    *  column="PREVIOUS_STATE"
    */
	public short getPreviousState() {
		return _previousState;
	}

  public void setPreviousState(short previousState) {
		_previousState = previousState;
	}

  /**
   * @hibernate.many-to-one
   *  column="PROCESS_ID"
   */
	public HProcess getProcess() {
		return _process;
	}
  
	public void setProcess(HProcess process) {
		_process = process;
	}

  /**
   * @hibernate.bag
   *  lazy="true"
   *  inverse="true"
   *  cascade="delete"
   * @hibernate.collection-key
   *  column="PIID"
   * @hibernate.collection-one-to-many
   *  class="com.fs.pxe.daohib.bpel.hobj.HScope"
   */
	public Collection<HScope> getScopes() {
		return _scopes;
	}

	public void setScopes(Collection<HScope> scopes) {
		_scopes = scopes;
	}

  /**
   * @hibernate.bag
   *  lazy="true"
   *  inverse="true"
   *  cascade="delete"
   * @hibernate.collection-key
   *  column="PIID"
   * @hibernate.collection-one-to-many
   *  class="com.fs.pxe.daohib.bpel.hobj.HCorrelationSet"
   */
	public Collection<HCorrelationSet> getCorrelationSets() {
		return _csets;
	}

	public void setCorrelationSets(Collection<HCorrelationSet> csets) {
		_csets = csets;
	}

  /**
    * @hibernate.property
    *  column="STATE"
    */
	public short getState() {
		return _state;
	}

  public void setState(short state) {
		_state = state;
	}

  /**
   * @hibernate.property
   *  column="LAST_ACTIVE_DT"
   */
	public Date getLastActiveTime() {
		return _lastActiveTime;
	}
  
	public void setLastActiveTime(Date lastActiveTime) {
		_lastActiveTime = lastActiveTime;
	}

  public void setSequence(long seq) {
    _seq = seq;
  }
  
  /**
   * @hibernate.property column="SEQUENCE"
   */
  public long getSequence() {
    return _seq;
  }
  
  

}
