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

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HCorrelator;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorMessage;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorSelector;
import org.apache.ode.daohib.bpel.hobj.HMessageExchange;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;
import org.apache.ode.utils.ArrayUtils;
import org.hibernate.Query;

/**
 * Hibernate-based {@link CorrelatorDAO} implementation.
 */
class CorrelatorDaoImpl extends HibernateDao implements CorrelatorDAO {

    private static final String QRY_MESSAGE = "select this from " + HCorrelatorMessage.class.getName()
            + " as hk where hk.correlationKey = ?".intern();

    /** filter for finding a matching selector. */
    private static final String FLTR_SELECTORS = "where this.correlationKey = ?" + " and (this.instance.state = "
            + ProcessState.STATE_ACTIVE + "      or this.instance.state = " + ProcessState.STATE_READY + ")".intern();

    /** Query for removing routes. */
    private static final String QRY_DELSELECTORS = "delete from " + HCorrelatorSelector.class.getName()
            + " where groupId = ? " + "and instance = ?".intern();

    private static final String QRY_DELMESSAGES = "delete from " + HCorrelatorMessage.class.getName()
            + " where messageExchange = ?".intern();

    static Log __log = LogFactory.getLog(CorrelatorDaoImpl.class);

    private HCorrelator _hobj;

    public CorrelatorDaoImpl(SessionManager sm, HCorrelator hobj) {
        super(sm, hobj);
        _hobj = hobj;
    }

    public MessageExchangeDAO dequeueMessage(CorrelationKey key) {
        Query qry = getSession().createFilter(_hobj.getMessageCorrelations(), QRY_MESSAGE);
        qry.setString(0, CorrelationKeySerializer.toCanonicalString(key));
        HCorrelatorMessage mcor = (HCorrelatorMessage) qry.uniqueResult();

        if (mcor == null)
            return null;
        
        removeEntries(mcor.getMessageExchange());

        return new MessageExchangeDaoImpl(_sm, mcor.getMessageExchange());
    }

    public MessageRouteDAO findRoute(CorrelationKey key) {
        if (__log.isTraceEnabled())
            __log.trace("findRoute(key=" + key + ")");

        Query q = getSession().createFilter(_hobj.getSelectors(), FLTR_SELECTORS);
        q.setString(0, CorrelationKeySerializer.toCanonicalString(key));
        HCorrelatorSelector selector = (HCorrelatorSelector) q.uniqueResult();
        return selector == null ? null :  new MessageRouteDaoImpl(_sm, selector);
    }

    /**
     * @see org.apache.ode.bpel.dao.CorrelatorDAO#enqueueMessage(byte[],
     *      CorrelationKey[])
     */
    public void enqueueMessage(MessageExchangeDAO mex, CorrelationKey[] correlationKeys) {
        String[] keys = canonifyKeys(correlationKeys);
        if (__log.isDebugEnabled()) {
            __log.debug("enqueueProcessInvocation: mex=" + mex + " keys="
                    + ArrayUtils.makeCollection(ArrayList.class, keys));
        }

        for (String key : keys) {
            HCorrelatorMessage mcor = new HCorrelatorMessage();
            mcor.setCorrelator(_hobj);
            mcor.setCreated(new Date());
            mcor.setCorrelationKey(key);
            mcor.setMessageExchange((HMessageExchange) ((MessageExchangeDaoImpl) mex)._hobj);
            getSession().save(mcor);
        }

    }

    private String[] canonifyKeys(CorrelationKey[] keys) {
        String[] ret = new String[keys.length];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = CorrelationKeySerializer.toCanonicalString(keys[i]);
        }
        return ret;
    }

    public void addRoute(String routeGroupId, ProcessInstanceDAO target, int idx, CorrelationKey correlationKey) {

        HCorrelatorSelector hsel = new HCorrelatorSelector();
        hsel.setGroupId(routeGroupId);
        hsel.setIndex(idx);
        hsel.setCorrelationKey(CorrelationKeySerializer.toCanonicalString(correlationKey));
        hsel.setInstance((HProcessInstance) ((ProcessInstanceDaoImpl) target).getHibernateObj());
        hsel.setCorrelator(_hobj);
        hsel.setCreated(new Date());
        _hobj.getSelectors().add(hsel);
        getSession().save(hsel);

    }

    public String getCorrelatorId() {
        return _hobj.getCorrelatorId();
    }

    public void removeRoutes(String routeGroupId, ProcessInstanceDAO target) {
        Query q = getSession().createQuery(QRY_DELSELECTORS);
        q.setString(0, routeGroupId);  // groupId
        q.setEntity(1, ((ProcessInstanceDaoImpl) target).getHibernateObj()); // instance
        q.executeUpdate();
    }

    
    public void removeEntries(HMessageExchange mex) {
        Query q = getSession().createQuery(QRY_DELMESSAGES);
        q.setEntity(0, mex);  // messageExchange
        q.executeUpdate();
    }
    

}
