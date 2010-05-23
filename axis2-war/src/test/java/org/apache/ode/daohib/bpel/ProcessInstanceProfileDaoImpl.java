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
import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceProfileDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HActivityRecovery;
import org.apache.ode.daohib.bpel.hobj.HCorrelationSet;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorSelector;
import org.apache.ode.daohib.bpel.hobj.HFaultData;
import org.apache.ode.daohib.bpel.hobj.HMessage;
import org.apache.ode.daohib.bpel.hobj.HMessageExchange;
import org.apache.ode.daohib.bpel.hobj.HPartnerLink;
import org.apache.ode.daohib.bpel.hobj.HScope;
import org.apache.ode.daohib.bpel.hobj.HXmlData;
import org.hibernate.Query;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Hibernate based {@link ProcessInstanceProfileDao} implementation
 */
public class ProcessInstanceProfileDaoImpl extends ProcessProfileDaoImpl implements ProcessInstanceProfileDAO {
    @SuppressWarnings("unused")
    private static final Log __log = LogFactory.getLog(ProcessInstanceProfileDaoImpl.class);
    
    private ProcessInstanceDaoImpl instance;

    public ProcessInstanceProfileDaoImpl(SessionManager sm, ProcessInstanceDaoImpl instance) {
        super(sm, (ProcessDaoImpl)instance.getProcess());
        entering("ProcessInstanceProfileDaoImpl.ProcessInstanceProfileDaoImpl");
        this.instance = instance;
    }
    
    public SessionManager getSessionManager() {
        return _sm;
    }
    
    public ProcessDAO getProcess() {
        return process;
    }
    
    public List<MessageExchangeDAO> findMessageExchangesByInstance() {
        return findByInstance("from HMessageExchange as x where x.instance = :instance)", MessageExchangeDaoImpl.class, HMessageExchange.class);
    }

    public List<MessageRouteDAO> findMessageRoutesByInstance() {
        return findByInstance("from HCorrelatorSelector as s where s.instance = :instance", MessageRouteDaoImpl.class, HCorrelatorSelector.class);
    }

    public List<MessageDAO> findMessagesByInstance() {
        return findByInstance("from HMessage as m where m.messageExchange.instance = :instance)", MessageDaoImpl.class, HMessage.class);
    }

    public List<PartnerLinkDAO> findPartnerLinksByInstance() {
        return findByInstance("from HPartnerLink as p where p.scope.instance = :instance)", PartnerLinkDAOImpl.class, HPartnerLink.class);
    }

    public List<ScopeDAO> findScopesByInstance() {
        return findByInstance("from HScope as s where s.instance = :instance)", ScopeDaoImpl.class, HScope.class);
    }

    public List<XmlDataDAO> findXmlDataByInstance() {
        return findByInstance("from HXmlData as x where x.instance = :instance", XmlDataDaoImpl.class, HXmlData.class);
    }

    public List<ActivityRecoveryDAO> findActivityRecoveriesByInstance() {
        return findByInstance("from HActivityRecovery as a where a.instance = :instance", ActivityRecoveryDaoImpl.class, HActivityRecovery.class);
    }

    public List<CorrelationSetDAO> findCorrelationSetsByInstance() {
        return findByInstance("from HCorrelationSet as s where s.instance = :instance", CorrelationSetDaoImpl.class, HCorrelationSet.class);
    }

    public List<FaultDAO> findFaultsByInstance() {
        return findByInstance("from HFaultData as f where f in (select i.fault from HProcessInstance as i where i = :instance and i.fault is not null)", FaultDAOImpl.class, HFaultData.class);
    }

    public int countEventsByInstance() {
        Query query = getSession().createQuery("select count(id) from HBpelEvent as e where e.instance = :instance");
        query.setParameter("instance", instance._hobj);
        
        return ((Long)query.uniqueResult()).intValue();
    }
    
    @SuppressWarnings("unchecked")
    protected <D, H> List<D> findByInstance(String queryString, Class daoClass, Class hibClass) {
        List<D> results = new ArrayList<D>();

        try {
            Query query = getSession().createQuery(queryString);
            query.setParameter("instance", instance._hobj);
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