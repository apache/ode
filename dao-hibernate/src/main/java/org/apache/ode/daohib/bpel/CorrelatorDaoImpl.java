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
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
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
import org.apache.ode.utils.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;

import javax.xml.namespace.QName;

/**
 * Hibernate-based {@link CorrelatorDAO} implementation.
 */
class CorrelatorDaoImpl extends HibernateDao implements CorrelatorDAO {

    /** 
     * Note: the hk.messageExchange=null is a hack to get around a Hibernate bug where the query
     * does not properly discriminate for the proper subclass.
     */
    private static final String QRY_MESSAGE = " where this.correlationKey = ?".intern();

    /** filter for finding a matching selector. */
    private static final String FLTR_SELECTORS = ("from " + HCorrelatorSelector.class.getName()
            + " hs where hs.correlationKey = ? and hs.processType = ? and hs.correlator.correlatorId = ?").intern();
    private static final String FLTR_SELECTORS_SUBQUERY = ("from  " + HCorrelatorSelector.class.getName()
            + " hs where hs.correlationKey = ? and hs.processType = ? and hs.correlatorId = " +
            "(select hc.id from " + HCorrelator.class.getName() + " hc where hc.correlatorId = ? )").intern();


    private static final String LOCK_SELECTORS = "update from " + HCorrelatorSelector.class.getName() +
        " set lock = lock+1 where correlationKey = ? and processType = ?".intern();
    
    /** Query for removing routes. */
    private static final String QRY_DELSELECTORS = "delete from " + HCorrelatorSelector.class.getName()
            + " where groupId = ? and instance = ?".intern();

    private static final String QRY_DELMESSAGES = "delete from " + HCorrelatorMessage.class.getName()
            + " where messageExchange = ?".intern();

    static Log __log = LogFactory.getLog(CorrelatorDaoImpl.class);

    private HCorrelator _hobj;

    public CorrelatorDaoImpl(SessionManager sm, HCorrelator hobj) {
        super(sm, hobj);
        entering("CorrelatorDaoImpl.CorrelatorDaoImpl");
        _hobj = hobj;
    }

    public MessageExchangeDAO dequeueMessage(CorrelationKey key) {
        entering("CorrelatorDaoImpl.dequeueMessage");
        String hdr = "dequeueMessage(" + key + "): ";
        __log.debug(hdr);

        Query qry = getSession().createFilter(_hobj.getMessageCorrelations(), QRY_MESSAGE);
        qry.setString(0, key.toCanonicalString());
        
        // We really should consider the possibility of multiple messages matching a criteria.
        // When the message is handled, its not too convenient to attempt to determine if the
        // received message conflicts with one already received.
        Iterator mcors = qry.iterate();
        try {
            if (!mcors.hasNext()) {
                __log.debug(hdr + "did not find a MESSAGE entry.");
                return null;
            }
    
            HCorrelatorMessage mcor = (HCorrelatorMessage) mcors.next();
            __log.debug(hdr + "found MESSAGE entry " + mcor.getMessageExchange());
            removeEntries(mcor.getMessageExchange());
            return new MessageExchangeDaoImpl(_sm, mcor.getMessageExchange());
        } finally {
            Hibernate.close(mcors);
        }
    }

    public MessageRouteDAO findRoute(CorrelationKey key) {
        entering("CorrelatorDaoImpl.findRoute");
        String hdr = "findRoute(key=" + key + "): ";
        if (__log.isDebugEnabled())
            __log.debug(hdr);

        // Make sure we obtain a lock for the selector we want to find. Note that a SELECT FOR UPDATE
        // will not necessarily work, as different DB vendors attach a different meaning to this syntax.
        // In particular it is not clear how long the lock should be held, for the lifetime of the 
        // resulting cursor, or for the lifetime of the transaction. So really, an UPDATE of the row
        // is a much safer alternative.
        String processType = new QName(_hobj.getProcess().getTypeNamespace(), _hobj.getProcess().getTypeName()).toString();
        Query lockQry = getSession().createQuery(LOCK_SELECTORS);
        lockQry.setString(0, key == null ? null : key.toCanonicalString());
        lockQry.setString(1, processType);
        if (lockQry.executeUpdate() > 0) {
            
            Query q = getSession().createQuery(_sm.canJoinForUpdate() ? FLTR_SELECTORS : FLTR_SELECTORS_SUBQUERY);
            q.setString(0, key == null ? null : key.toCanonicalString());
            q.setString(1, processType);
            q.setString(2, _hobj.getCorrelatorId());
            q.setLockMode("hs", LockMode.UPGRADE);

            HCorrelatorSelector selector;
            try {
                selector = (HCorrelatorSelector) q.uniqueResult();
            } catch (Exception ex) {
                __log.debug("Strange, could not get a unique result for findRoute, trying to iterate instead.");

                Iterator i = q.iterate();
                if (i.hasNext()) selector = (HCorrelatorSelector) i.next();
                else selector = null;
                Hibernate.close(i);
            }
    
            __log.debug(hdr + "found " + selector);
            return selector == null ? null : new MessageRouteDaoImpl(_sm, selector);
        } 
        
        return null;
    }

    public void enqueueMessage(MessageExchangeDAO mex, CorrelationKey[] correlationKeys) {
        entering("CorrelatorDaoImpl.enqueueMessage");
        String[] keys = canonifyKeys(correlationKeys);
        String hdr = "enqueueMessage(mex=" + ((MessageExchangeDaoImpl) mex)._hobj.getId() + " keys="
                + CollectionUtils.makeCollection(ArrayList.class, keys) + "): ";

        if (__log.isDebugEnabled())
            __log.debug(hdr);

        for (String key : keys) {
            HCorrelatorMessage mcor = new HCorrelatorMessage();
            mcor.setCorrelator(_hobj);
            mcor.setCreated(new Date());
            mcor.setCorrelationKey(key);
            mcor.setMessageExchange((HMessageExchange) ((MessageExchangeDaoImpl) mex)._hobj);
            getSession().save(mcor);

            if (__log.isDebugEnabled())
                __log.debug(hdr + "saved " + mcor);
        }

    }

    private String[] canonifyKeys(CorrelationKey[] keys) {
        String[] ret = new String[keys.length];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = keys[i].toCanonicalString();
        }
        return ret;
    }

    public void addRoute(String routeGroupId, ProcessInstanceDAO target, int idx, CorrelationKey correlationKey) {
        entering("CorrelatorDaoImpl.addRoute");
        String hdr = "addRoute(" + routeGroupId + ", iid=" + target.getInstanceId() + ", idx=" + idx + ", ckey="
                + correlationKey + "): ";

        __log.debug(hdr);
        HCorrelatorSelector hsel = new HCorrelatorSelector();
        hsel.setGroupId(routeGroupId);
        hsel.setIndex(idx);
        hsel.setLock(0);
        hsel.setCorrelationKey(correlationKey.toCanonicalString());
        hsel.setInstance((HProcessInstance) ((ProcessInstanceDaoImpl) target).getHibernateObj());
        hsel.setProcessType(target.getProcess().getType().toString());
        hsel.setCorrelator(_hobj);
        hsel.setCreated(new Date());
//        _hobj.addSelector(hsel);
        getSession().save(hsel);

        __log.debug(hdr + "saved " + hsel);
    }

    public boolean checkRoute(CorrelationKey ckey) {
        entering("CorrelatorDaoImpl.checkRoute");
        Query lockQry = getSession().createQuery(LOCK_SELECTORS);
        lockQry.setString("ckey", ckey == null ? null : ckey.toCanonicalString());
        lockQry.setEntity("corr",_hobj);
        lockQry.setReadOnly(true);
        return lockQry.list().isEmpty();
        
    }
    public String getCorrelatorId() {
        return _hobj.getCorrelatorId();
    }

    public void removeRoutes(String routeGroupId, ProcessInstanceDAO target) {
        entering("CorrelatorDaoImpl.removeRoutes");
        String hdr = "removeRoutes(" + routeGroupId + ", iid=" + target.getInstanceId() + "): ";
        __log.debug(hdr);
        Session session = getSession();
        Query q = session.createQuery(QRY_DELSELECTORS);
        q.setString(0, routeGroupId); // groupId
        q.setEntity(1, ((ProcessInstanceDaoImpl) target).getHibernateObj()); // instance
        int updates = q.executeUpdate();
        session.flush(); // explicit flush to ensure route removed
        __log.debug(hdr + "deleted " + updates + " rows");

    }

    public void removeEntries(HMessageExchange mex) {
        entering("CorrelatorDaoImpl.removeEntries");
        String hdr = "removeEntries(" + mex + "): ";
        __log.debug(hdr);

        Query q = getSession().createQuery(QRY_DELMESSAGES);
        q.setEntity(0, mex); // messageExchange
        int numMods = q.executeUpdate();
        __log.debug(hdr + " deleted " + numMods + " rows");
    }

}
