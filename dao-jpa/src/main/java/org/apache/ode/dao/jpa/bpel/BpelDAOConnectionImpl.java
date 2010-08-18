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

package org.apache.ode.dao.jpa.bpel;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.Filter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.dao.bpel.BpelDAOConnection;
import org.apache.ode.dao.bpel.CorrelationSetDAO;
import org.apache.ode.dao.bpel.MessageExchangeDAO;
import org.apache.ode.dao.bpel.ProcessDAO;
import org.apache.ode.dao.bpel.ProcessInstanceDAO;
import org.apache.ode.dao.bpel.ProcessManagementDAO;
import org.apache.ode.dao.bpel.ScopeDAO;
import org.apache.ode.dao.jpa.JpaConnection;
import org.apache.ode.dao.jpa.JpaOperator;
import org.apache.ode.utils.ISO8601DateParser;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class BpelDAOConnectionImpl extends JpaConnection implements BpelDAOConnection {
    static final Log __log = LogFactory.getLog(BpelDAOConnectionImpl.class);

    static final ThreadLocal<BpelDAOConnectionImpl> _connections = new ThreadLocal<BpelDAOConnectionImpl>();

    public BpelDAOConnectionImpl(EntityManager mgr, TransactionManager txMgr, JpaOperator operator) {
        super(mgr, txMgr, operator);
    }

    public static ThreadLocal<BpelDAOConnectionImpl> getThreadLocal() {
        return _connections;
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
    	_txCtx.begin();
        ProcessInstanceDAOImpl instance = _em.find(ProcessInstanceDAOImpl.class, iid);
        _txCtx.commit();
        return instance;
    }

    public MessageExchangeDAO createMessageExchange(char dir) {
    	_txCtx.begin();
        MessageExchangeDAOImpl ret = new MessageExchangeDAOImpl(dir);
        _em.persist(ret);
        _txCtx.commit();
        return ret;
    }

    public ProcessDAO createProcess(QName pid, QName type, String guid, long version) {
    	_txCtx.begin();
        ProcessDAOImpl ret = new ProcessDAOImpl(pid,type,guid,version);
        _em.persist(ret);
        _txCtx.commit();
        return ret;
    }
    
    public ProcessDAO createTransientProcess(Long id) {
        ProcessDAOImpl ret = new ProcessDAOImpl(null, null, null, 0);
        ret.setId(id);
        
        return ret;
    }

    @SuppressWarnings("unchecked")
    public ProcessDAO getProcess(QName processId) {
    	_txCtx.begin();
        List l = _em.createQuery("select x from ProcessDAOImpl x where x._processId = ?1")
                .setParameter(1, processId.toString()).getResultList();
        _txCtx.commit();
        if (l.size() == 0) return null;
        ProcessDAOImpl p = (ProcessDAOImpl) l.get(0);
        return p;
    }

    public int getNumInstances(QName processId) {
        ProcessDAO process = getProcess(processId);
        if (process != null)
            return process.getNumInstances();
        else return -1;
    }

    public ScopeDAO getScope(Long siidl) {
    	_txCtx.begin();
        ScopeDAO scopeDAO = _em.find(ScopeDAOImpl.class, siidl);
        _txCtx.commit();
        return scopeDAO;
    }

    public void insertBpelEvent(BpelEvent event, ProcessDAO process, ProcessInstanceDAO instance) {
    	_txCtx.begin();
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
        _txCtx.commit();
    }

    private static String dateFilter(String filter) {
        String date = Filter.getDateWithoutOp(filter);
        String op = filter.substring(0,filter.indexOf(date));
        Date dt = null;
        try {
            dt = ISO8601DateParser.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Timestamp ts = new Timestamp(dt.getTime());
        return op + " '" + ts.toString() + "'";
    }

    @SuppressWarnings("unchecked")
    public Collection<ProcessInstanceDAO> instanceQuery(InstanceFilter criteria) {
    	_txCtx.begin();
        StringBuffer query = new StringBuffer();
        query.append("select pi from ProcessInstanceDAOImpl as pi left join fetch pi._fault ");

        if (criteria != null) {
            // Building each clause
            ArrayList<String> clauses = new ArrayList<String>();

            // iid filter
            if ( criteria.getIidFilter() != null ) {
                StringBuffer filters = new StringBuffer();
                List<String> iids = criteria.getIidFilter();
                for (int m = 0; m < iids.size(); m++) {
                    filters.append(" pi._instanceId = ").append(iids.get(m));
                    if (m < iids.size() - 1) filters.append(" or");
                }
                clauses.add(" (" + filters + ")");
            }

            // pid filter
            if (criteria.getPidFilter() != null) {
                StringBuffer filters = new StringBuffer();
                List<String> pids = criteria.getPidFilter();
                for (int m = 0; m < pids.size(); m++) {
                    filters.append(" pi._process._processId = '").append(pids.get(m)).append("'");
                    if (m < pids.size() - 1) filters.append(" or");
                }
                clauses.add(" (" + filters + ")");
            }

            // name filter
            if (criteria.getNameFilter() != null) {
                String val = criteria.getNameFilter();
                if (val.endsWith("*")) {
                    val = val.substring(0, val.length()-1) + "%";
                }
                //process type string begins with name space
                //this could possibly match more than you want
                //because the name space and name are stored together
                clauses.add(" pi._process._processType like '%" + val + "'");
            }

            // name space filter
            if (criteria.getNamespaceFilter() != null) {
                //process type string begins with name space
                //this could possibly match more than you want
                //because the name space and name are stored together
                clauses.add(" pi._process._processType like '{" +
                        criteria.getNamespaceFilter() + "%'");
            }

            // started filter
            if (criteria.getStartedDateFilter() != null) {
                for ( String ds : criteria.getStartedDateFilter() ) {
                    clauses.add(" pi._dateCreated " + dateFilter(ds));
                }
            }

            // last-active filter
            if (criteria.getLastActiveDateFilter() != null) {
                for ( String ds : criteria.getLastActiveDateFilter() ) {
                    clauses.add(" pi._lastActive " + dateFilter(ds));
                }
            }

            // status filter
            if (criteria.getStatusFilter() != null) {
                StringBuffer filters = new StringBuffer();
                List<Short> states = criteria.convertFilterState();
                for (int m = 0; m < states.size(); m++) {
                    filters.append(" pi._state = ").append(states.get(m));
                    if (m < states.size() - 1) filters.append(" or");
                }
                clauses.add(" (" + filters.toString() + ")");
            }

            // $property filter
            if (criteria.getPropertyValuesFilter() != null) {
                Map<String,String> props = criteria.getPropertyValuesFilter();
                // join to correlation sets
                query.append(" inner join pi._rootScope._correlationSets as cs");
                int i = 0;
                for (String propKey : props.keySet()) {
                    i++;
                    // join to props for each prop
                    query.append(" inner join cs._props as csp"+i);
                    // add clause for prop key and value
                    clauses.add(" csp"+i+".propertyKey = '"+propKey+
                            "' and csp"+i+".propertyValue = '"+
                            // spaces have to be escaped, might be better handled in InstanceFilter
                            props.get(propKey).replaceAll("&#32;", " ")+"'");
                }
            }

            // order by
            StringBuffer orderby = new StringBuffer("");
            if (criteria.getOrders() != null) {
                orderby.append(" order by");
                List<String> orders = criteria.getOrders();
                for (int m = 0; m < orders.size(); m++) {
                    String field = orders.get(m);
                    String ord = " asc";
                    if (field.startsWith("-")) {
                        ord = " desc";
                    }
                    String fieldName = " pi._instanceId";
                    if ( field.endsWith("name") || field.endsWith("namespace")) {
                        fieldName = " pi._process._processType";
                    }
                    if ( field.endsWith("version")) {
                        fieldName = " pi._process._version";
                    }
                    if ( field.endsWith("status")) {
                        fieldName = " pi._state";
                    }
                    if ( field.endsWith("started")) {
                        fieldName = " pi._dateCreated";
                    }
                    if ( field.endsWith("last-active")) {
                        fieldName = " pi._lastActive";
                    }
                    orderby.append(fieldName + ord);
                    if (m < orders.size() - 1) orderby.append(", ");
                }

            }

            // Preparing the statement
            if (clauses.size() > 0) {
                query.append(" where");
                for (int m = 0; m < clauses.size(); m++) {
                    query.append(clauses.get(m));
                    if (m < clauses.size() - 1) query.append(" and");
                }
            }

            query.append(orderby);
        }

        if (__log.isDebugEnabled()) {
            __log.debug(query.toString());
        }

        // criteria limit
        Query pq = _em.createQuery(query.toString());
        getJPADaoOperator().setBatchSize(pq, criteria.getLimit());
        List<ProcessInstanceDAO> ql = pq.getResultList();

        Collection<ProcessInstanceDAO> list = new ArrayList<ProcessInstanceDAO>();
        int num = 0;
        for (Iterator iterator = ql.iterator(); iterator.hasNext();) {
            if(num++ > criteria.getLimit()) break;
            ProcessInstanceDAO processInstanceDAO = (ProcessInstanceDAO) iterator.next();
            list.add(processInstanceDAO);
        }
        _txCtx.commit();
        return list;
    }


    public Collection<ProcessInstanceDAO> instanceQuery(String expression) {
        return instanceQuery(new InstanceFilter(expression));
    }

    public MessageExchangeDAO getMessageExchange(String mexid) {
        return _em.find(MessageExchangeDAOImpl.class, mexid);
    }

    public void deleteMessageExchange(MessageExchangeDAO mexDao) {
        _em.remove(mexDao);
    }

    public EntityManager getEntityManager() {
        return _em;
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Collection<CorrelationSetDAO>> getCorrelationSets(Collection<ProcessInstanceDAO> instances) {
        if (instances.size() == 0) {
            return new HashMap<Long, Collection<CorrelationSetDAO>>();
        }
        ArrayList<Long> iids = new ArrayList<Long>(instances.size());
        for (ProcessInstanceDAO dao: instances) {
            iids.add(dao.getInstanceId());
        }
        _txCtx.begin();
        Collection<CorrelationSetDAOImpl> csets = _em.createNamedQuery(CorrelationSetDAOImpl.SELECT_CORRELATION_SETS_BY_INSTANCES).setParameter("instances", iids).getResultList();
        _txCtx.commit();
        Map<Long, Collection<CorrelationSetDAO>> map = new HashMap<Long, Collection<CorrelationSetDAO>>();
        for (CorrelationSetDAOImpl cset: csets) {
            Long id = cset.getScope().getProcessInstance().getInstanceId();
            Collection<CorrelationSetDAO> existing = map.get(id);
            if (existing == null) {
                existing = new ArrayList<CorrelationSetDAO>();
                map.put(id, existing);
            }
            existing.add(cset);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public Collection<CorrelationSetDAO> getActiveCorrelationSets() {
    	_txCtx.begin();
        Collection<CorrelationSetDAO> daos = _em.createNamedQuery(CorrelationSetDAOImpl.SELECT_ACTIVE_SETS).setParameter("state", ProcessState.STATE_ACTIVE).getResultList();
        _txCtx.commit();
        return daos;
    }

    public ProcessManagementDAO getProcessManagement() {
        return new ProcessManagementDAOImpl(_em);
    }

}
