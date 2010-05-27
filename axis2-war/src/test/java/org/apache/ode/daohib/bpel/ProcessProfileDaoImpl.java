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
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HActivityRecovery;
import org.apache.ode.daohib.bpel.hobj.HCorrelationSet;
import org.apache.ode.daohib.bpel.hobj.HCorrelator;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorSelector;
import org.apache.ode.daohib.bpel.hobj.HFaultData;
import org.apache.ode.daohib.bpel.hobj.HMessage;
import org.apache.ode.daohib.bpel.hobj.HMessageExchange;
import org.apache.ode.daohib.bpel.hobj.HPartnerLink;
import org.apache.ode.daohib.bpel.hobj.HProcess;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;
import org.apache.ode.daohib.bpel.hobj.HScope;
import org.apache.ode.daohib.bpel.hobj.HXmlData;
import org.hibernate.Query;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Hibernate based {@link ProcessProfileDAO} implementation
 */
public class ProcessProfileDaoImpl extends HibernateDao implements ProcessProfileDAO {
    @SuppressWarnings("unused")
    private static final Log __log = LogFactory.getLog(ProcessProfileDaoImpl.class);

    protected ProcessDaoImpl process;

    public ProcessProfileDaoImpl(SessionManager sm, ProcessDaoImpl process) {
        super(sm, process._hobj);
        entering("ProcessProfileDaoImpl.ProcessProfileDaoImpl");
        this.process = process;
    }

    public SessionManager getSessionManager() {
        return _sm;
    }

    @SuppressWarnings("unchecked")
    public boolean doesProcessExist() {
        boolean exists = false;

        Query query = getSession().createQuery("select count(id) from HProcess as p where p.guid = :guid");
        query.setParameter("guid", ((HProcess)process._hobj).getGuid());
        for( Long cnt : (List<Long>)query.list()) {
            exists = cnt.intValue() > 0;
        }

        return exists;
    }

    public List<ProcessInstanceDAO> findInstancesByProcess() {
        return findByProcess("from HProcessInstance as i where i.process = :process)", ProcessInstanceDaoImpl.class, HProcessInstance.class);
    }

    public List<MessageExchangeDAO> findMessageExchangesByProcess() {
        return findByProcess("from HMessageExchange as x where x.instance.process = :process)", MessageExchangeDaoImpl.class, HMessageExchange.class);
    }

    public List<MessageRouteDAO> findMessageRoutesByProcess() {
        return findByProcess("from HCorrelatorSelector as s where s.instance.process = :process)", MessageRouteDaoImpl.class, HCorrelatorSelector.class);
    }

    public List<MessageDAO> findMessagesByProcess() {
        return findByProcess("from HMessage as m where m.messageExchange.process = :process)", MessageDaoImpl.class, HMessage.class);
    }

    public List<PartnerLinkDAO> findPartnerLinksByProcess() {
        return findByProcess("from HPartnerLink as p where p.process = :process)", PartnerLinkDAOImpl.class, HPartnerLink.class);
    }

    public List<ScopeDAO> findScopesByProcess() {
        return findByProcess("from HScope as s where s.instance.process = :process", ScopeDaoImpl.class, HScope.class);
    }

    public List<XmlDataDAO> findXmlDataByProcess() {
        return findByProcess("from HXmlData as x where x.instance.process = :process", XmlDataDaoImpl.class, HXmlData.class);
    }

    public List<ActivityRecoveryDAO> findActivityRecoveriesByProcess() {
        return findByProcess("from HActivityRecovery as a where a.instance.process = :process", ActivityRecoveryDaoImpl.class, HActivityRecovery.class);
    }

    public List<CorrelationSetDAO> findCorrelationSetsByProcess() {
        return findByProcess("from HCorrelationSet as s where s.process = :process", CorrelationSetDaoImpl.class, HCorrelationSet.class);
    }

    public List<CorrelatorDAO> findCorrelatorsByProcess() {
        return findByProcess("from HCorrelator as c where c.process = :process", CorrelatorDaoImpl.class, HCorrelator.class);
    }

    public List<FaultDAO> findFaultsByProcess() {
        return findByProcess("from HFaultData as f where f in (select i.fault from HProcessInstance as i where i.process = :process and i.fault is not null)", FaultDAOImpl.class, HFaultData.class);
    }

    public int countEventsByProcess() {
        Query query = getSession().createQuery("select count(id) from HBpelEvent as e where e.instance.process = :process");
        query.setParameter("process", process._hobj);

        return ((Long)query.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    protected <D, H> List<D> findByProcess(String queryString, Class daoClass, Class hibClass) {
        List<D> results = new ArrayList<D>();

        try {
            Query query = getSession().createQuery(queryString);
            query.setParameter("process", process._hobj);
            for( H hibObj : (Collection<H>)query.list()) {
                Constructor<D> c = daoClass.getConstructor(SessionManager.class, hibClass);
                results.add( c.newInstance(_sm, hibObj) );
            }
        } catch( Exception e ) {
            throw new RuntimeException(e);
        }

        return results;
    }
}