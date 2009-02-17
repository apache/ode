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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.ActivityRecoveryDAO;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessProfileDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * OpenJPA based {@link ProcessProfileDAO} implementation
 */
public class ProcessProfileDAOImpl extends OpenJPADAO implements ProcessProfileDAO {
    @SuppressWarnings("unused")
    private static final Log __log = LogFactory.getLog(ProcessProfileDAOImpl.class);
    
    protected EntityManager em;
    protected ProcessDAOImpl process;

    public ProcessProfileDAOImpl(EntityManager em, ProcessDAOImpl process) {
        this.process = process;
        this.em = em;
    }
    
    public boolean doesProcessExist() {
        Query query = em.createQuery("select count(p._id) from ProcessDAOImpl as p where p._guid = :guid");
        query.setParameter("guid", process.getGuid());
        
        return ((Long)query.getSingleResult()) > 0;
    }
    
    public List<ProcessInstanceDAO> findInstancesByProcess() {
        return findByProcess("select i from ProcessInstanceDAOImpl as i where i._process = :process");
    }

    public List<MessageExchangeDAO> findMessageExchangesByProcess() {
        return findByProcess("select x from MessageExchangeDAOImpl as x where x._processInst._process = :process");
    }

    public List<MessageRouteDAO> findMessageRoutesByProcess() {
        return findByProcess("select r from MessageRouteDAOImpl as r where r._processInst._process = :process");
    }

    public List<MessageDAO> findMessagesByProcess() {
        return findByProcess("select m from MessageDAOImpl as m where m._messageExchange._process = :process");
    }

    public List<PartnerLinkDAO> findPartnerLinksByProcess() {
        return findByProcess("select p from PartnerLinkDAOImpl as p where p._scope._processInstance._process = :process");
    }

    public List<ScopeDAO> findScopesByProcess() {
        return findByProcess("select s from ScopeDAOImpl as s where s._processInstance._process = :process");
    }

    public List<XmlDataDAO> findXmlDataByProcess() {
        return findByProcess("select x from XmlDataDAOImpl as x where x._scope._processInstance._process = :process");
    }
    
    public List<ActivityRecoveryDAO> findActivityRecoveriesByProcess() {
        return findByProcess("select a from ActivityRecoveryDAOImpl as a where a._instance._process = :process");
    }

    public List<CorrelationSetDAO> findCorrelationSetsByProcess() {
        return findByProcess("select s from CorrelationSetDAOImpl as s where s._scope._processInstance._process = :process");
    }

    public List<CorrelatorDAO> findCorrelatorsByProcess() {
        return findByProcess("select c from CorrelatorDAOImpl as c where c._process = :process");
    }

    public List<FaultDAO> findFaultsByProcess() {
        return findByProcess("select f from FaultDAOImpl as f where f._id in(select i._fault from ProcessInstanceDAOImpl as i where i._process = :process and i._fault is not null)");
    }

    public int countEventsByProcess() {
        Query query = em.createQuery("select count(e._id) from EventDAOImpl as e where e._instance._process = :process");
        query.setParameter("process", process);
        
        return ((Long)query.getSingleResult()).intValue();
    }
    
    @SuppressWarnings("unchecked")
    protected <D> List<D> findByProcess(String queryString) {
        Query query = em.createQuery(queryString);
        query.setParameter("process", process);
        
        return query.getResultList();
    }
}