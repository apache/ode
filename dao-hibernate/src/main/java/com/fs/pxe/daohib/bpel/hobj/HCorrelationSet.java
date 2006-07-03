/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel.hobj;

import com.fs.pxe.daohib.hobj.HObject;

import java.util.HashSet;
import java.util.Collection;

/**
 * Hibernate table representing correlation set values.
 * @hibernate.class table="BPEL_CORRELATION_SET"
 */
public class HCorrelationSet extends HObject{
  private HProcess _process;
  private HProcessInstance _instance;
  private Collection<HCorrelationProperty> _properties = new HashSet<HCorrelationProperty>();
  private HScope _scope;
  private String _name;
  private String _value;

  public HCorrelationSet() {
    super();
  }

  public HCorrelationSet(HScope scope, String name) {
		super();
    _scope = scope;
    _instance = scope.getInstance();
    _process = _instance.getProcess();
    _name = name;
	}

  /**
   * @hibernate.property
   *    column="VALUE"
   */
	public String getValue() {
		return _value;
	}

  public void setName(String name) {
    _name = name;
  }

  public void setScope(HScope scope) {
    _scope = scope;
  }

	public void setValue(String value) {
		_value = value;
	}

   /**
   * @hibernate.property
   *  column="CORR_SET_NAME"
   *  length="255"
   */
	public String getName() {
		return _name;
	}

  /**
   * @hibernate.many-to-one
   *  column="SCOPE_ID"
   */
	public HScope getScope() {
		return _scope;
	}

  /**
   * @hibernate.many-to-one
   *  column="PIID"
   */
  public HProcessInstance getInstance() {
    return _instance;
  }

  public void setInstance(HProcessInstance instance) {
    _instance = instance;
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
   * @hibernate.set
   *  lazy="true"
   *  inverse="true"
   *  cascade="delete"
   * @hibernate.collection-key
   *  column="CORR_SET_ID"
   * @hibernate.collection-one-to-many
   *  class="com.fs.pxe.daohib.bpel.hobj.HCorrelationProperty"
   */
  public Collection<HCorrelationProperty> getProperties() {
    return _properties;
  }

  public void setProperties(Collection<HCorrelationProperty> properties) {
    _properties = properties;
  }
}
