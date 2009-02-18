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

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.dao.*;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HCorrelator;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorMessage;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorSelector;
import org.apache.ode.daohib.bpel.hobj.HMessageExchange;
import org.apache.ode.daohib.bpel.hobj.HProcess;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Query;

import javax.xml.namespace.QName;

/**
 * Hibernate-based {@link CorrelatorDAO} implementation.
 */
class CorrelatorDaoImpl extends HibernateDao implements CorrelatorDAO {
    static Log __log = LogFactory.getLog(CorrelatorDaoImpl.class);

    /** filter for finding a matching selector. */
    private static final String LOCK_SELECTORS = "update from HCorrelatorSelector as hs set hs.lock = hs.lock+1 where hs.processType = :processType";
    private static final String FLTR_SELECTORS = "from HCorrelatorSelector as hs where hs.processType = :processType and hs.correlator.correlatorId = :correlatorId";

    /** Query for removing routes. */
    private static final String QRY_DELSELECTORS = "delete from HCorrelatorSelector where groupId = ? and instance = ?";

    private HCorrelator _hobj;

    public CorrelatorDaoImpl(SessionManager sm, HCorrelator hobj) {
        super(sm, hobj);
        entering("CorrelatorDaoImpl.CorrelatorDaoImpl");
        _hobj = hobj;
    }

    @SuppressWarnings("unchecked")
    public MessageExchangeDAO dequeueMessage(CorrelationKeySet keySet) {
        entering("CorrelatorDaoImpl.dequeueMessage");
        
        MessageExchangeDAO mex = null;
        
        String hdr = "dequeueMessage(" + keySet + "): ";
        __log.debug(hdr);

        List<CorrelationKeySet> subSets = keySet.findSubSets();
        Query qry = getSession().createFilter(_hobj.getMessageCorrelations(), 
        		generateUnmatchedQuery(subSets));
    	for( int i = 0; i < subSets.size(); i++ ) {
    		qry.setString("s" + i, subSets.get(i).toCanonicalString());
    	}
        
        // We really should consider the possibility of multiple messages matching a criteria.
        // When the message is handled, its not too convenient to attempt to determine if the
        // received message conflicts with one already received.
        Iterator mcors = qry.iterate();
        try {
            if (!mcors.hasNext()) {
                __log.debug(hdr + "did not find a MESSAGE entry.");
            } else {
	            HCorrelatorMessage mcor = (HCorrelatorMessage) mcors.next();
	            __log.debug(hdr + "found MESSAGE entry " + mcor.getMessageExchange());
	            mex = new MessageExchangeDaoImpl(_sm, mcor.getMessageExchange());
            }
        } finally {
            Hibernate.close(mcors);
        }
        
        return mex;
    }

    @SuppressWarnings("unchecked")
    public List<MessageRouteDAO> findRoute(CorrelationKeySet keySet) {
    	List<MessageRouteDAO> routes = new ArrayList<MessageRouteDAO>();
    	
        entering("CorrelatorDaoImpl.findRoute");
        String hdr = "findRoute(keySet=" + keySet + "): ";
        if (__log.isDebugEnabled())
            __log.debug(hdr);

        // Make sure we obtain a lock for the selector we want to find. Note that a SELECT FOR UPDATE
        // will not necessarily work, as different DB vendors attach a different meaning to this syntax.
        // In particular it is not clear how long the lock should be held, for the lifetime of the 
        // resulting cursor, or for the lifetime of the transaction. So really, an UPDATE of the row
        // is a much safer alternative.
        String processType = new QName(_hobj.getProcess().getTypeNamespace(), _hobj.getProcess().getTypeName()).toString();
    	List<CorrelationKeySet> subSets = keySet.findSubSets();
        Query lockQry = getSession().createQuery(generateSelectorQuery(LOCK_SELECTORS, subSets));
        lockQry.setString("processType", processType);
    	for( int i = 0; i < subSets.size(); i++ ) {
    		lockQry.setString("s" + i, subSets.get(i).toCanonicalString());
    	}
        if (lockQry.executeUpdate() > 0) {
            Query q = getSession().createQuery(generateSelectorQuery(FLTR_SELECTORS, subSets));
            
            q.setString("processType", processType);
            q.setString("correlatorId", _hobj.getCorrelatorId());

        	for( int i = 0; i < subSets.size(); i++ ) {
        		q.setString("s" + i, subSets.get(i).toCanonicalString());
        	}
            q.setLockMode("hs", LockMode.UPGRADE);

            HCorrelatorSelector selector;
            try {
            	boolean routed = false;
            	Iterator selectors = q.iterate();
            	while (selectors.hasNext()) {
                    selector = (HCorrelatorSelector) selectors.next();
                    if (selector != null) {
                    	if ("all".equals(selector.getRoute())) {
                        	routes.add(new MessageRouteDaoImpl(_sm, selector));
                    	} else {
                    		if (!routed){
                            	routes.add(new MessageRouteDaoImpl(_sm, selector));
                    		}
                    		routed = true;
                    	}
                    }
            	}
            } catch (Exception ex) {
                __log.debug("Strange, could not get a unique result for findRoute, trying to iterate instead.");

                Iterator i = q.iterate();
                if (i.hasNext()) selector = (HCorrelatorSelector) i.next();
                else selector = null;
                Hibernate.close(i);
            }
    
            __log.debug(hdr + "found " + routes);
            return routes;
        } 
        
        return null;
    }

    private String generateUnmatchedQuery(List<CorrelationKeySet> subSets) {
    	StringBuffer filterQuery = new StringBuffer();
    	
    	if( subSets.size() == 1 ) {
    		filterQuery.append(" where this.correlationKey = :s0");
    	} else if( subSets.size() > 1 ) {
    		filterQuery.append(" where this.correlationKey in(");
        	for( int i = 0; i < subSets.size(); i++ ) {
        		if( i > 0 ) {
        			filterQuery.append(", ");
        		}
        		filterQuery.append(":s").append(i);
        	}
        	filterQuery.append(")");
    	}
    	
    	return filterQuery.toString();
    }
    
    private String generateSelectorQuery(String header, List<CorrelationKeySet> subSets) {
    	StringBuffer filterQuery = new StringBuffer(header);
    	
    	if( subSets.size() == 1 ) {
    		filterQuery.append(" and hs.correlationKey = :s0");
    	} else if( subSets.size() > 1 ) {
    		filterQuery.append(" and hs.correlationKey in(");
        	for( int i = 0; i < subSets.size(); i++ ) {
        		if( i > 0 ) {
        			filterQuery.append(", ");
        		}
        		filterQuery.append(":s").append(i);
        	}
        	filterQuery.append(")");
    	}
    	
    	return filterQuery.toString();
    }
    
    public void enqueueMessage(MessageExchangeDAO mex, CorrelationKeySet correlationKeySet) {
        entering("CorrelatorDaoImpl.enqueueMessage");
        String hdr = "enqueueMessage(mex=" + ((MessageExchangeDaoImpl) mex)._hobj.getId() + " keySet="
                + correlationKeySet.toCanonicalString() + "): ";

        if (__log.isDebugEnabled())
            __log.debug(hdr);

        for( CorrelationKeySet aSubSet : correlationKeySet.findSubSets() ) {
            HCorrelatorMessage mcor = new HCorrelatorMessage();
            mcor.setCorrelator(_hobj);
            mcor.setCreated(new Date());
            mcor.setMessageExchange((HMessageExchange) ((MessageExchangeDaoImpl) mex)._hobj);
            mcor.setCorrelationKey(aSubSet.toCanonicalString());
            getSession().save(mcor);
            
            if (__log.isDebugEnabled())
                __log.debug(hdr + "saved " + mcor);
        }
    }

    public void addRoute(String routeGroupId, ProcessInstanceDAO target, int idx, CorrelationKeySet correlationKeySet, String routePolicy) {
        entering("CorrelatorDaoImpl.addRoute");
        String hdr = "addRoute(" + routeGroupId + ", iid=" + target.getInstanceId() + ", idx=" + idx + ", ckeySet="
                + correlationKeySet + "): ";

        __log.debug(hdr);
        HCorrelatorSelector hsel = new HCorrelatorSelector();
        hsel.setGroupId(routeGroupId);
        hsel.setIndex(idx);
        hsel.setLock(0);
        hsel.setCorrelationKey(correlationKeySet.toCanonicalString());
        hsel.setInstance((HProcessInstance) ((ProcessInstanceDaoImpl) target).getHibernateObj());
        hsel.setProcessType(target.getProcess().getType().toString());
        hsel.setCorrelator(_hobj);
        hsel.setCreated(new Date());
        hsel.setRoute(routePolicy);
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

    public void setCorrelatorId(String newId) {
        _hobj.setCorrelatorId(newId);
    }

    public void removeRoutes(String routeGroupId, ProcessInstanceDAO target) {
        entering("CorrelatorDaoImpl.removeRoutes");
        String hdr = "removeRoutes(" + routeGroupId + ", iid=" + target.getInstanceId() + "): ";
        __log.debug(hdr);
        Query q = getSession().createQuery(QRY_DELSELECTORS);
        q.setString(0, routeGroupId); // groupId
        q.setEntity(1, ((ProcessInstanceDaoImpl) target).getHibernateObj()); // instance
        int updates = q.executeUpdate();
        __log.debug(hdr + "deleted " + updates + " rows");
    }

     public Collection<CorrelatorMessageDAO> getAllMessages() {
         Collection<CorrelatorMessageDAO> msgs = new ArrayList<CorrelatorMessageDAO>();
         for (HCorrelatorMessage correlatorMessage : _hobj.getMessageCorrelations())
             msgs.add(new CorrelatorMessageDaoImpl(_sm, correlatorMessage));
         return msgs;
     }

     public Collection<MessageRouteDAO> getAllRoutes() {
         Collection<MessageRouteDAO> routes = new ArrayList<MessageRouteDAO>();
         for (HCorrelatorSelector selector : _hobj.getSelectors())
             routes.add(new MessageRouteDaoImpl(_sm, selector));
         return routes;
     }

}