/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel;

import com.fs.pxe.bpel.dao.MessageRouteDAO;
import com.fs.pxe.bpel.dao.ProcessInstanceDAO;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.bpel.hobj.HCorrelatorSelector;

/**
 * Hibernate-based {@link MessageRouteDAO} implementation.
 */
class MessageRouteDaoImpl extends HibernateDao implements MessageRouteDAO {
	
  private HCorrelatorSelector _selector;

	public MessageRouteDaoImpl(SessionManager sm, HCorrelatorSelector hobj) {
		super(sm, hobj);
    _selector = hobj;
	}
	/**
	 * @see com.fs.pxe.bpel.dao.MessageRouteDAO#getTargetInstance()
	 */
	public ProcessInstanceDAO getTargetInstance() {
		return new ProcessInstanceDaoImpl(_sm, _selector.getInstance());
	}

  public String getGroupId() {
    return _selector.getGroupId();
  }

  public int getIndex() {
    return _selector.getIndex();
  }

}
