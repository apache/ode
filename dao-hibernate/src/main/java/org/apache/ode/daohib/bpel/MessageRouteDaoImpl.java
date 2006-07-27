/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.daohib.bpel;

import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorSelector;

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
	 * @see org.apache.ode.bpel.dao.MessageRouteDAO#getTargetInstance()
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
