/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel;

import java.io.Serializable;

import org.hibernate.Session;

import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.hobj.HObject;

/**
 * Base class for our DAO objects.
 */
abstract class HibernateDao {
  protected final SessionManager _sm;
  protected final HObject _hobj;

	protected HibernateDao(SessionManager sessionManager, HObject hobj) {
    _sm = sessionManager;
		_hobj = hobj;
	}

	/**
	 * @see com.fs.utils.dao.DAO#getDHandle()
	 */
	public Serializable getDHandle() {
    return new HibernateHandle(getClass(), _hobj.getClass(), getSession().getIdentifier(_hobj));
	}
  
  protected Session getSession(){
  	return _sm.getSession();
  }
  
  public HObject getHibernateObj(){
  	return _hobj;
  }
  
  public boolean equals(Object obj){
  	assert obj instanceof HibernateDao;
    return _hobj.getId().equals(((HibernateDao)obj)._hobj.getId());
  }
  
  public int hashCode(){
  	return _hobj.getId().hashCode();
  }
  
  protected void update() {
    _sm.getSession().update(_hobj);
  }
}
