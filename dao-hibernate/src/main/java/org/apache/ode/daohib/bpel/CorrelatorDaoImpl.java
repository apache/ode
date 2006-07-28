/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.daohib.bpel;

import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.*;
import org.apache.ode.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;

/**
 * Hibernate-based {@link CorrelatorDAO} implementation.
 */
class CorrelatorDaoImpl extends HibernateDao implements CorrelatorDAO {
	
  private static final String QRY_MESSAGE =
          "select this from " + HCorrelatorMessageKey.class.getName() +
          " as hk where hk.owner = this and hk.canonical = ? order by this.created";

  /** filter for finding a matching selector. */
  private static final String FLTR_SELECTORS =
          "where this.correlationKey = ?" +
          " and (this.instance.state = " + ProcessState.STATE_ACTIVE +
          "      or this.instance.state = " + ProcessState.STATE_READY + ")" +
          " order by this.created";


  /** Query for removing routes. */
  private static final String QRY_DELROUTES = "delete from "  + 
    HCorrelatorSelector.class.getName() + " where groupId = ? " +
      "and instance = ?";
  
  static Log __log = LogFactory.getLog(CorrelatorDaoImpl.class);
  
  private HCorrelator _hobj;

	public CorrelatorDaoImpl(SessionManager sm, HCorrelator hobj) {
		super(sm,hobj);
		_hobj = hobj;
	}

  public MessageExchangeDAO dequeueMessage(CorrelationKey key) {
    HMessageExchange hmex = null;
    upgrade();

    Query qry = getSession().createFilter(_hobj.getMessageCorrelations(),QRY_MESSAGE);
    qry.setString(0, CorrelationKeySerializer.toCanonicalString(key));
    // We can use the database to do the searching on hashcode AND canonical string
    Iterator iter = qry.iterate();

    while(iter.hasNext()){
      HCorrelatorMessage mcor = (HCorrelatorMessage)iter.next();
      hmex = mcor.getMessageExchange();
      _hobj.getMessageCorrelations().remove(mcor);
      // Lock this row
      getSession().delete(mcor);
      break;
    }

    Hibernate.close(iter);
    if (__log.isDebugEnabled()) {
      if(hmex != null)
        __log.debug("dequeueEarliest: MATCH FOUND, data=" + hmex);
    }

    return hmex == null ? null : new MessageExchangeDaoImpl(_sm,hmex);
	}

	public MessageRouteDAO findRoute(CorrelationKey key) {
		if (__log.isTraceEnabled())
      __log.trace("findRoute(key=" + key + ")");

   upgrade();

    Query q = getSession().createFilter(_hobj.getSelectors(), FLTR_SELECTORS);
    q.setString(0, CorrelationKeySerializer.toCanonicalString(key));
    Iterator iter =  q.iterate();
    if(iter.hasNext()){
      HCorrelatorSelector selector = (HCorrelatorSelector)iter.next();
      Hibernate.close(iter);
      return new MessageRouteDaoImpl(_sm,selector);
    }else{
      Hibernate.close(iter);
      return null;
    }

	}
	/**
	 * @see org.apache.ode.bpel.dao.CorrelatorDAO#enqueueMessage(byte[], CorrelationKey[])
	 */
	public void enqueueMessage(MessageExchangeDAO mex, CorrelationKey[] correlationKeys) {
    String[] keys = canonifyKeys(correlationKeys);
    if (__log.isDebugEnabled()) {
      __log.debug("enqueueProcessInvocation: mex=" +mex + " keys="
                + ArrayUtils.makeCollection(ArrayList.class, keys));
    }

   upgrade();
    
    HCorrelatorMessage mcor = new HCorrelatorMessage();
    mcor.setCorrelator(_hobj);
    mcor.setCreated(new Date());
    _hobj.getMessageCorrelations().add(mcor);
    for (String key : keys) {
      HCorrelatorMessageKey hk = new HCorrelatorMessageKey();
      hk.setCanonical(key);
      hk.setOwner(mcor);
      getSession().save(hk);
      mcor.getCorrelationHashKeys().add(hk);
    }
    
    mcor.setMessageExchange((HMessageExchange)((MessageExchangeDaoImpl)mex)._hobj);
    getSession().save(mcor);

  }

  private String[] canonifyKeys(CorrelationKey[] keys) {
    String[] ret = new String[keys.length];
    for (int i = 0; i < ret.length; ++i) {
      ret[i] = CorrelationKeySerializer.toCanonicalString(keys[i]);
    }
    return ret;
  }


  public void addRoute(String routeGroupId, ProcessInstanceDAO target, int idx, CorrelationKey correlationKey) {
   upgrade();

    HCorrelatorSelector hsel = new HCorrelatorSelector();
    hsel.setGroupId(routeGroupId);
    hsel.setIndex(idx);
    hsel.setCorrelationKey(CorrelationKeySerializer.toCanonicalString(correlationKey));
    hsel.setInstance((HProcessInstance) ((ProcessInstanceDaoImpl)target).getHibernateObj());
    hsel.setCorrelator(_hobj);
    hsel.setCreated(new Date());
    _hobj.getSelectors().add(hsel);
    getSession().save(hsel);

  }

  public String getCorrelatorId() {
    return _hobj.getCorrelatorId();
  }

  public void removeRoutes(String routeGroupId, ProcessInstanceDAO target) {
   upgrade();

    Query q = getSession().createQuery(QRY_DELROUTES);
    q.setString(0,routeGroupId);
    q.setEntity(1, ((ProcessInstanceDaoImpl)target).getHibernateObj());
    q.executeUpdate();
  }

  private void upgrade() {
    // This effectively creates a mutual exclusion lock on this
    // correlator by obtaining a row-lock.
    _hobj.setLock(_hobj.getLock()+1);
    getSession().flush();
  }

}
