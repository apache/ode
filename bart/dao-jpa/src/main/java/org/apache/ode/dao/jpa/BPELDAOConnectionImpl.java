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

package org.apache.ode.dao.jpa;

import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.evt.ScopeEvent;

import javax.persistence.EntityManager;
import javax.xml.namespace.QName;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class BPELDAOConnectionImpl implements BpelDAOConnection {
	
	EntityManager _em;

    public BPELDAOConnectionImpl(EntityManager em) {
        _em = em;
    }

    public List<BpelEvent> bpelEventQuery(InstanceFilter ifilter,
                                          BpelEventFilter efilter) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public List<Date> bpelEventTimelineQuery(InstanceFilter ifilter,
                                             BpelEventFilter efilter) {
        // TODO
        throw new UnsupportedOperationException();
    }
	
	public ProcessInstanceDAO getInstance(Long iid) {
        ProcessInstanceDAOImpl instance = _em.find(ProcessInstanceDAOImpl.class, iid);
        return instance;
    }

    public void close() {
        _em = null;
    }

    public MessageExchangeDAO createMessageExchange(String mexId, char dir) {
        MessageExchangeDAOImpl ret = new MessageExchangeDAOImpl(mexId, dir);
        _em.persist(ret);
        return ret;
    }

    public ProcessDAO createProcess(QName pid, QName type, String guid, long version) {
        ProcessDAOImpl ret = new ProcessDAOImpl(pid,type,guid,version);
        _em.persist(ret);
        return ret;
    }

    public ProcessDAO getProcess(QName processId) {
        List l = _em.createQuery("select x from ProcessDAOImpl x where x._processId = ?1")
                .setParameter(1, processId.toString()).getResultList();
        if (l.size() == 0) return null;
        ProcessDAOImpl p = (ProcessDAOImpl) l.get(0);
        return p;
    }

    public ScopeDAO getScope(Long siidl) {
        return _em.find(ScopeDAOImpl.class, siidl);
    }

    public void insertBpelEvent(BpelEvent event, ProcessDAO process, ProcessInstanceDAO instance) {
        EventDAOImpl eventDao = new EventDAOImpl();
        eventDao.setTstamp(new Timestamp(System.currentTimeMillis()));
        eventDao.setType(BpelEvent.eventName(event));
        String evtStr = event.toString();
        eventDao.setDetail(evtStr.substring(0, Math.min(254, evtStr.length())));
        if (process != null)
            eventDao.setProcess((ProcessDAOImpl) process);
        if (instance != null)
            eventDao.setInstance((ProcessInstanceDAOImpl) instance);
        if (event instanceof ScopeEvent)
            eventDao.setScopeId(((ScopeEvent) event).getScopeId());
        eventDao.setEvent(event);
        _em.persist(eventDao);
	}

	@SuppressWarnings("unchecked")
    public Collection<ProcessInstanceDAO> instanceQuery(InstanceFilter criteria) {
        StringBuffer query = new StringBuffer();
        query.append("select pi from ProcessInstanceDAOImpl as pi");

        // TODO Continue implementing me
        if (criteria != null) {
            // Building each clause
            ArrayList<String> clauses = new ArrayList<String>();
            if (criteria.getPidFilter() != null)
                clauses.add(" pi._process._processId = '" + criteria.getPidFilter() + "'");
            if (criteria.getStatusFilter() != null) {
                StringBuffer filters = new StringBuffer();
                List<Short> states = criteria.convertFilterState();
                for (int m = 0; m < states.size(); m++) {
                    filters.append(" pi._state = ").append(states.get(m));
                    if (m < states.size() - 1) filters.append(" or");
                }
                clauses.add(" (" + filters.toString() + ")");
            }

            // Preparing the statement
            if (clauses.size() > 0) {
                query.append(" where");
                for (int m = 0; m < clauses.size(); m++) {
                    query.append(clauses.get(m));
                    if (m < clauses.size() - 1) query.append(" and");
                }
            }
        }
        return _em.createQuery(query.toString()).getResultList();
	}

   
	public Collection<ProcessInstanceDAO> instanceQuery(String expression) {
	    return instanceQuery(new InstanceFilter(expression));
	}

	public void setEntityManger(EntityManager em) {
		_em = em;
	}
	
    public MessageExchangeDAO getMessageExchange(String mexid) {
        List l = _em.createQuery("select x from MesssageExchangeDAOImpl x where x._mexId = ?1")
        .setParameter(1, mexid).getResultList();
        if (l.size() == 0) return null;
        MessageExchangeDAOImpl m = (MessageExchangeDAOImpl) l.get(0);
        return m;
    }

    public EntityManager getEntityManager() {
        return _em;
    }
}
