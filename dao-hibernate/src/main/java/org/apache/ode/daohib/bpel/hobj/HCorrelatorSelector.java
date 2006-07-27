/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.daohib.bpel.hobj;

import org.apache.ode.daohib.hobj.HObject;

/**
 * @hibernate.class table="BPEL_CORRELATOR_SELECTOR"
 */
public class HCorrelatorSelector extends HObject{
  private HCorrelator _correlator;
  private HProcessInstance _instance;
  private String _groupId;
  private String _correlationKey;
  private int _idx;


  /** Constructor. */
	public HCorrelatorSelector() {
		super();
	}

  /**
   * @hibernate.many-to-one column="PIID"
   */
	public HProcessInstance getInstance() {
		return _instance;
	}

	public void setInstance(HProcessInstance instance) {
		_instance = instance;
	}

  /**
   * @hibernate.many-to-one 
   * @hibernate.column name="CORRELATOR" index="IDX_CORRELATORSELECTOR_CORRELATOR"
   */
  public HCorrelator getCorrelator() {
    return _correlator;
  }

  public void setCorrelator(HCorrelator owner) {
    _correlator = owner;
  }

  /**
   * @hibernate.property column="SELGRPID"
   * @hibernate.column name="SELGRPID" index="IDX_SELECTOR_SELGRPID"
   */
  public String getGroupId() {
    return _groupId;
  }

  public void setGroupId(String groupId) {
    _groupId = groupId;
  }

  /**
   * @hibernate.property column="CKEY"
   * @hibernate.column name="CKEY" index="IDX_SELECTOR_CKEY"
   */
  public String getCorrelationKey() {
    return _correlationKey;
  }

  public void setCorrelationKey(String correlationKey) {
    _correlationKey = correlationKey;
  }

  /**
   * @hibernate.property column="IDX"
   */
  public int getIndex() {
    return _idx;
  }

  public void setIndex(int idx) {
    _idx = idx;
  }
}
