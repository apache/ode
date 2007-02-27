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
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
        return _em.find(ProcessInstanceDAOImpl.class, iid);
	}

    public void close() {
        _em = null;
    }

    public MessageExchangeDAO createMessageExchange(char dir) {
        MessageExchangeDAOImpl ret = new MessageExchangeDAOImpl(dir);
        _em.persist(ret);
        return ret;
    }

    public ProcessDAO createProcess(QName pid, QName type, String guid, long version) {
        ProcessDAOImpl ret = new ProcessDAOImpl(pid,type,guid,this,version);
        System.out.println("########## " + _em.contains(ret));
        _em.persist(ret);
        return ret;
    }

    public ProcessDAO getProcess(QName processId) {
        return _em.find(ProcessDAOImpl.class, processId.toString());
    }

    public ScopeDAO getScope(Long siidl) {
        return _em.find(ScopeDAOImpl.class, siidl);
    }

    public void insertBpelEvent(BpelEvent event, ProcessDAO process, ProcessInstanceDAO instance) {
        EventDAOImpl eventDao = new EventDAOImpl();
        eventDao.setTstamp(new Timestamp(System.currentTimeMillis()));
        eventDao.setType(BpelEvent.eventName(event));
        eventDao.setDetail(event.toString());
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
        // TODO
        return _em.createQuery("select x from ProcessInstanceDAOImpl as x").getResultList();
	}

   
	public Collection<ProcessInstanceDAO> instanceQuery(String expression) {
	    return instanceQuery(new InstanceFilter(expression));
	}

	public void setEntityManger(EntityManager em) {
		_em = em;
	}
	
	void removeProcess(ProcessDAOImpl p) {
		if ( _em != null ) {
			_em.remove(p);
			_em.flush();
		}
	}

    public MessageExchangeDAO getMessageExchange(String mexid) {
        return _em.find(MessageExchangeDAOImpl.class, mexid);
    }

    public EntityManager getEntityManager() {
        return _em;
    }
}
