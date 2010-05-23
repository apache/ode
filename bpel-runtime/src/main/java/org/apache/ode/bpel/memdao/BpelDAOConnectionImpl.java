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
package org.apache.ode.bpel.memdao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.Filter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessFilter;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.utils.ISO8601DateParser;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.UnaryFunction;

import javax.xml.namespace.QName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A very simple, in-memory implementation of the {@link BpelDAOConnection} interface.
 */
class BpelDAOConnectionImpl implements BpelDAOConnection {
    private static final Log __log = LogFactory.getLog(BpelDAOConnectionImpl.class);

    private Scheduler _scheduler;
    private Map<QName, ProcessDaoImpl> _store;
    private List<BpelEvent> _events = new LinkedList<BpelEvent>();
    long _mexTtl;

    private static Map<String,MessageExchangeDAO> _mexStore = Collections.synchronizedMap(new HashMap<String,MessageExchangeDAO>());
    protected static Map<String, Long> _mexAge = new ConcurrentHashMap<String, Long>();
    private static AtomicLong counter = new AtomicLong(Long.MAX_VALUE / 2);
    private static volatile long _lastRemoval = 0;

    BpelDAOConnectionImpl(Map<QName, ProcessDaoImpl> store, Scheduler scheduler, long mexTtl) {
        _store = store;
        _scheduler = scheduler;
        _mexTtl = mexTtl;
    }

    public ProcessDAO getProcess(QName processId) {
        return _store.get(processId);
    }

    public ProcessDAO createTransientProcess(Serializable id) {
        ProcessDaoImpl process = new ProcessDaoImpl(this, _store, null, null, (String)id, 0);

        return process;
    }
    
    public ProcessDAO createProcess(QName pid, QName type, String guid, long version) {
        ProcessDaoImpl process = new ProcessDaoImpl(this,_store,pid,type, guid,version);
        _store.put(pid,process);
        return process;
    }

    public ProcessInstanceDAO getInstance(Long iid) {
        for (ProcessDaoImpl proc : _store.values()) {
            ProcessInstanceDAO instance = proc._instances.get(iid);
            if (instance != null)
                return instance;
        }
        return null;
    }

    public int getNumInstances(QName processId) {
        ProcessDAO process = getProcess(processId);
        if (process != null)
            return process.getNumInstances();
        else return -1;
    }

    @SuppressWarnings("unchecked")
    public Collection<ProcessInstanceDAO> instanceQuery(InstanceFilter filter) {
        if(filter.getLimit()==0) {
            return Collections.EMPTY_LIST;
        }
        List<ProcessInstanceDAO> matched = new ArrayList<ProcessInstanceDAO>();
        // Selecting
        selectionCompleted:
        for (ProcessDaoImpl proc : _store.values()) {
            boolean pmatch = true;
            if (filter.getNameFilter() != null
                    && !equalsOrWildcardMatch(filter.getNameFilter(), proc.getProcessId().getLocalPart()))
                pmatch = false;
            if (filter.getNamespaceFilter() != null
                    && !equalsOrWildcardMatch(filter.getNamespaceFilter(), proc.getProcessId().getNamespaceURI()))
                pmatch = false;

            if (pmatch) {
                for (ProcessInstanceDAO inst : proc._instances.values()) {
                    boolean match = true;

                    if (filter.getStatusFilter() != null) {
                        boolean statusMatch = false;
                        for (Short status : filter.convertFilterState()) {
                            if (inst.getState() == status.byteValue()) statusMatch = true;
                        }
                        if (!statusMatch) match = false;
                    }
                    if (filter.getStartedDateFilter() != null
                            && !dateMatch(filter.getStartedDateFilter(), inst.getCreateTime(), filter))
                        match = false;
                    if (filter.getLastActiveDateFilter() != null
                            && !dateMatch(filter.getLastActiveDateFilter(), inst.getLastActiveTime(), filter))
                        match = false;

//                    if (filter.getPropertyValuesFilter() != null) {
//                        for (Map.Entry propEntry : filter.getPropertyValuesFilter().entrySet()) {
//                            boolean entryMatched = false;
//                            for (ProcessPropertyDAO prop : proc.getProperties()) {
//                                if (prop.getName().equals(propEntry.getKey())
//                                        && (propEntry.getValue().equals(prop.getMixedContent())
//                                        || propEntry.getValue().equals(prop.getSimpleContent()))) {
//                                    entryMatched = true;
//                                }
//                            }
//                            if (!entryMatched) {
//                                match = false;
//                            }
//                        }
//                    }

                    if (match) {
                        matched.add(inst);
                        if(matched.size()==filter.getLimit()) {
                            break selectionCompleted;
                        }
                    }
                }
            }
        }
        // And ordering
        if (filter.getOrders() != null) {
            final List<String> orders = filter.getOrders();

            Collections.sort(matched, new Comparator<ProcessInstanceDAO>() {
                public int compare(ProcessInstanceDAO o1, ProcessInstanceDAO o2) {
                    for (String orderKey: orders) {
                        int result = compareInstanceUsingKey(orderKey, o1, o2);
                        if (result != 0) return result;
                    }
                    return 0;
                }
            });
        }

        return matched;
    }

    /**
     * Close this DAO connection.
     */
    public void close() {
    }

    public Collection<ProcessDAO> processQuery(ProcessFilter filter) {
        throw new UnsupportedOperationException("Can't query process configuration using a transient DAO.");
    }

    public MessageExchangeDAO createMessageExchange(char dir) {
        final String id = Long.toString(counter.getAndIncrement());
        MessageExchangeDAO mex = new MessageExchangeDAOImpl(dir,id);
        long now = System.currentTimeMillis();
        _mexStore.put(id,mex);
        _mexAge.put(id, now);

        if (now > _lastRemoval + (_mexTtl / 10)) {
            _lastRemoval = now;
            Object[] oldMexs = _mexAge.keySet().toArray();
            for (int i=oldMexs.length-1; i>0; i--) {
                String oldMex = (String) oldMexs[i];
                Long age = _mexAge.get(oldMex);
                if (age != null && now-age > _mexTtl) {
                    removeMessageExchange(oldMex);
                    _mexAge.remove(oldMex);
                }
            }
        }

        // Removing right away on rollback
        onRollback(new Runnable() {
            public void run() {
                removeMessageExchange(id);
                _mexAge.remove(id);
            }
        });

        return mex;
    }

    public MessageExchangeDAO getMessageExchange(String mexid) {
        return _mexStore.get(mexid);
    }

    private int compareInstanceUsingKey(String key, ProcessInstanceDAO instanceDAO1, ProcessInstanceDAO instanceDAO2) {
        String s1 = null;
        String s2 = null;
        boolean ascending = true;
        String orderKey = key;
        if (key.startsWith("+") || key.startsWith("-")) {
            orderKey = key.substring(1, key.length());
            if (key.startsWith("-")) ascending = false;
        }
        ProcessDAO process1 = getProcess(instanceDAO1.getProcess().getProcessId());
        ProcessDAO process2 = getProcess(instanceDAO2.getProcess().getProcessId());
        if ("pid".equals(orderKey)) {
            s1 = process1.getProcessId().toString();
            s2 = process2.getProcessId().toString();
        } else if ("name".equals(orderKey)) {
            s1 = process1.getProcessId().getLocalPart();
            s2 = process2.getProcessId().getLocalPart();
        } else if ("namespace".equals(orderKey)) {
            s1 = process1.getProcessId().getNamespaceURI();
            s2 = process2.getProcessId().getNamespaceURI();
        } else if ("version".equals(orderKey)) {
            s1 = ""+process1.getVersion();
            s2 = ""+process2.getVersion();
        } else if ("status".equals(orderKey)) {
            s1 = ""+instanceDAO1.getState();
            s2 = ""+instanceDAO2.getState();
        } else if ("started".equals(orderKey)) {
            s1 = ISO8601DateParser.format(instanceDAO1.getCreateTime());
            s2 = ISO8601DateParser.format(instanceDAO2.getCreateTime());
        } else if ("last-active".equals(orderKey)) {
            s1 = ISO8601DateParser.format(instanceDAO1.getLastActiveTime());
            s2 = ISO8601DateParser.format(instanceDAO2.getLastActiveTime());
        }
        if (ascending) return s1.compareTo(s2);
        else return s2.compareTo(s1);
    }

    private boolean equalsOrWildcardMatch(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        if (s1.equals(s2)) return true;
        if (s1.endsWith("*")) {
            if (s2.startsWith(s1.substring(0, s1.length() - 1))) return true;
        }
        if (s2.endsWith("*")) {
            if (s1.startsWith(s2.substring(0, s2.length() - 1))) return true;
        }
        return false;
    }

    public boolean dateMatch(List<String> dateFilters, Date instanceDate,  InstanceFilter filter) {
        boolean match = true;
        for (String ddf : dateFilters) {
            String isoDate = ISO8601DateParser.format(instanceDate);
            String critDate = Filter.getDateWithoutOp(ddf);
            if (ddf.startsWith("=")) {
                if (!isoDate.startsWith(critDate)) match = false;
            } else if (ddf.startsWith("<=")) {
                if (!isoDate.startsWith(critDate) && isoDate.compareTo(critDate) > 0) match = false;
            } else if (ddf.startsWith(">=")) {
                if (!isoDate.startsWith(critDate) && isoDate.compareTo(critDate) < 0) match = false;
            } else if (ddf.startsWith("<")) {
                if (isoDate.compareTo(critDate) > 0) match = false;
            } else if (ddf.startsWith(">")) {
                if (isoDate.compareTo(critDate) < 0) match = false;
            }
        }
        return match;
    }


    public ScopeDAO getScope(Long siidl) {
        for (ProcessDaoImpl process : _store.values()) {
            for (ProcessInstanceDAO instance : process._instances.values()) {
                if (instance.getScope(siidl) != null) return instance.getScope(siidl);
            }
        }
        return null;
    }


    public void insertBpelEvent(BpelEvent event, ProcessDAO processConfiguration, ProcessInstanceDAO instance) {
        _events.add(event);
    }


    public List<Date> bpelEventTimelineQuery(InstanceFilter ifilter, BpelEventFilter efilter) {
        // TODO : Provide more correct implementation:
        ArrayList<Date> dates = new ArrayList<Date>();
        CollectionsX.transform(dates, _events, new UnaryFunction<BpelEvent,Date>() {
            public Date apply(BpelEvent x) {
                return x.getTimestamp();
            }
        });
        return dates;
    }


    public List<BpelEvent> bpelEventQuery(InstanceFilter ifilter, BpelEventFilter efilter) {
        // TODO : Provide a more correct (filtering) implementation:
        return _events;
    }

    /**
     * @see org.apache.ode.bpel.dao.BpelDAOConnection#instanceQuery(String)
     */
    public Collection<ProcessInstanceDAO> instanceQuery(String expression) {
        //TODO
        throw new UnsupportedOperationException();
    }

    static void removeMessageExchange(String mexId) {
        // Cleaning up mex
        if (__log.isDebugEnabled()) __log.debug("Removing mex " + mexId + " from memory store.");
        MessageExchangeDAO mex = _mexStore.remove(mexId);
        if (mex == null)
            __log.warn("Couldn't find mex " + mexId + " for cleanup.");
        _mexAge.remove(mexId);
    }

    public void defer(final Runnable runnable) {
        _scheduler.registerSynchronizer(new Scheduler.Synchronizer() {
            public void afterCompletion(boolean success) {
            }
            public void beforeCompletion() {
                runnable.run();
            }
        });
    }
    public void onRollback(final Runnable runnable) {
        _scheduler.registerSynchronizer(new Scheduler.Synchronizer() {
            public void afterCompletion(boolean success) {
                if (!success) runnable.run();
            }
            public void beforeCompletion() {
            }
        });
    }

    public Map<Long, Collection<CorrelationSetDAO>> getCorrelationSets(Collection<ProcessInstanceDAO> instances) {
        Map<Long, Collection<CorrelationSetDAO>> map = new HashMap<Long, Collection<CorrelationSetDAO>>();
        for (ProcessInstanceDAO instance: instances) {
            Long id = instance.getInstanceId();
            Collection<CorrelationSetDAO> existing = map.get(id);
            if (existing == null) {
                existing = new ArrayList<CorrelationSetDAO>();
                map.put(id, existing);
            }
            existing.addAll(instance.getCorrelationSets());
        }
        return map;
    }

    public Collection<CorrelationSetDAO> getActiveCorrelationSets() {
        throw new UnsupportedOperationException();
    }

    public ProcessManagementDaoImpl getProcessManagement() {
        return new ProcessManagementDaoImpl();
    }
}
