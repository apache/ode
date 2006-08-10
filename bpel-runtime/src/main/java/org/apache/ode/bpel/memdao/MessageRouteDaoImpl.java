/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;


/**
 * A very simple, in-memory implementation of the {@link MessageRouteDAO} interface.
 */
class MessageRouteDaoImpl extends DaoBaseImpl implements MessageRouteDAO {
  ProcessInstanceDaoImpl _instance;
  String _groupId;
  CorrelationKey _ckey;
  int _idx;

  MessageRouteDaoImpl(ProcessInstanceDaoImpl owner, String groupId, CorrelationKey ckey, int idx) {
    _instance = owner;
    _groupId = groupId;
    _ckey = ckey;
    _idx = idx;
  }

  public ProcessInstanceDAO getTargetInstance() {
    return _instance;
  }

  public String getGroupId() {
    return _groupId;
  }

  public int getIndex() {
    return _idx;
  }
}
