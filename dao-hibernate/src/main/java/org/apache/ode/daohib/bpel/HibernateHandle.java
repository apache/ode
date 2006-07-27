/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.daohib.bpel;

import java.io.Serializable;

/**
 * Serializable class for obtain a reference to a hibernate POJO
 */
class HibernateHandle implements Serializable{

  private static final long serialVersionUID = 1L;
  private Class _daoCls;
	private Class _hibCls;
  private Serializable _id;
  /**
	 * 
	 */
	public HibernateHandle(Class daoCls, Class hibCls, Serializable id) {
		_daoCls = daoCls;
    _hibCls = hibCls;
    _id = id;
	}
  
  public Class getHibernateClass(){
  	return _hibCls;
  }
  
  public Class getDAOClass(){
  	return _daoCls;
  }
  
  public Serializable getId(){
  	return _id;
  }
}
