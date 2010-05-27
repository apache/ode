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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.ActivityRecoveryDAO;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceProfileDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * OpenJPA based {@link ProcessInstanceProfileDAO} implementation
 */
public class ProcessInstanceProfileDAOImpl extends ProcessProfileDAOImpl implements ProcessInstanceProfileDAO {
    @SuppressWarnings("unused")
    private static final Log __log = LogFactory.getLog(ProcessInstanceProfileDAOImpl.class);

    private ProcessInstanceDAOImpl instance;

    public ProcessInstanceProfileDAOImpl(EntityManager em, ProcessInstanceDAOImpl instance) {
        super(em, (ProcessDAOImpl)instance.getProcess());
        this.instance = instance;
    }

    public ProcessDAO getProcess() {
        return process;
    }

    public List<MessageExchangeDAO> findMessageExchangesByInstance() {
        return findByInstance("select x from MessageExchangeDAOImpl as x where x._processInst = :instance");
    }

    public List<MessageRouteDAO> findMessageRoutesByInstance() {
        return findByInstance("select r from MessageRouteDAOImpl as r where r._processInst = :instance");
    }

    public List<MessageDAO> findMessagesByInstance() {
        return findByInstance("select m from MessageDAOImpl as m where m._messageExchange._processInst = :instance");
    }

    public List<PartnerLinkDAO> findPartnerLinksByInstance() {
        return findByInstance("select p from PartnerLinkDAOImpl as p where p._scope._processInstance = :instance");
    }

    public List<ScopeDAO> findScopesByInstance() {
        return findByInstance("select s from ScopeDAOImpl as s where s._processInstance = :instance");
    }

    public List<XmlDataDAO> findXmlDataByInstance() {
        return findByInstance("select x from XmlDataDAOImpl as x where x._scope._processInstance = :instance");
    }

    public List<ActivityRecoveryDAO> findActivityRecoveriesByInstance() {
        return findByInstance("select a from ActivityRecoveryDAOImpl as a where a._instance = :instance");
    }

    public List<CorrelationSetDAO> findCorrelationSetsByInstance() {
        return findByInstance("select s from CorrelationSetDAOImpl as s where s._scope._processInstance = :instance");
    }

    public List<FaultDAO> findFaultsByInstance() {
        return findByInstance("select f from FaultDAOImpl as f where f._id in (select i._fault from ProcessInstanceDAOImpl as i where i = :instance and i._fault is not null)");
    }

    public int countEventsByInstance() {
        Query query = em.createQuery("select count(e._id) from EventDAOImpl as e where e._instance = :instance");
        query.setParameter("instance", instance);

        return ((Long)query.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    protected <D> List<D> findByInstance(String queryString) {
        Query query = em.createQuery(queryString);
        query.setParameter("instance", instance);

        return query.getResultList();
    }
}