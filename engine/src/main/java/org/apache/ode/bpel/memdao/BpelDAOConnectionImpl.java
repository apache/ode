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

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.Filter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessFilter;
import org.apache.ode.bpel.dao.*;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.utils.ISO8601DateParser;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.UnaryFunction;

/**
 * A very simple, in-memory implementation of the {@link BpelDAOConnection} interface.
 */
class BpelDAOConnectionImpl implements BpelDAOConnection {
    private static final Log __log = LogFactory.getLog(BpelDAOConnectionImpl.class);
    public static long TIME_TO_LIVE = 2*60*1000;

    private TransactionManager _txm;

    private Map<QName, ProcessDaoImpl> _store;

    private List<BpelEvent> _events = new LinkedList<BpelEvent>();

    private final List<MessageExchangeDAOImpl> _mexList = new LinkedList<MessageExchangeDAOImpl>();
    private final Map<String, MessageExchangeDAOImpl> _mexStore = new HashMap<String, MessageExchangeDAOImpl>();

    private final HashMap<String,ResourceRouteDAOImpl> _resRouteStore = new HashMap<String,ResourceRouteDAOImpl>();

    private static AtomicLong counter = new AtomicLong(Long.MAX_VALUE / 2);
    private static volatile long _lastRemoval = 0;

    BpelDAOConnectionImpl(Map<QName, ProcessDaoImpl> store, TransactionManager txm) {
        _store = store;
        _txm = txm;
    }

    public ProcessDAO getProcess(QName processId) {
        return _store.get(processId);
    }

    public ProcessDAO createProcess(QName pid, QName type, String guid, long version) {
        ProcessDaoImpl process = new ProcessDaoImpl(this, _store, pid, type, guid, version);
        _store.put(pid, process);
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

    public Collection<ProcessInstanceDAO> instanceQuery(InstanceFilter filter) {
        if (filter.getLimit() == 0) {
            return Collections.EMPTY_LIST;
        }
        List<ProcessInstanceDAO> matched = new ArrayList<ProcessInstanceDAO>();
        // Selecting
        selectionCompleted: for (ProcessDaoImpl proc : _store.values()) {
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
                            if (inst.getState() == status.byteValue())
                                statusMatch = true;
                        }
                        if (!statusMatch)
                            match = false;
                    }
                    if (filter.getStartedDateFilter() != null
                            && !dateMatch(filter.getStartedDateFilter(), inst.getCreateTime(), filter))
                        match = false;
                    if (filter.getLastActiveDateFilter() != null
                            && !dateMatch(filter.getLastActiveDateFilter(), inst.getLastActiveTime(), filter))
                        match = false;

                    // if (filter.getPropertyValuesFilter() != null) {
                    // for (Map.Entry propEntry : filter.getPropertyValuesFilter().entrySet()) {
                    // boolean entryMatched = false;
                    // for (ProcessPropertyDAO prop : proc.getProperties()) {
                    // if (prop.getName().equals(propEntry.getKey())
                    // && (propEntry.getValue().equals(prop.getMixedContent())
                    // || propEntry.getValue().equals(prop.getSimpleContent()))) {
                    // entryMatched = true;
                    // }
                    // }
                    // if (!entryMatched) {
                    // match = false;
                    // }
                    // }
                    // }

                    if (match) {
                        matched.add(inst);
                        if (matched.size() == filter.getLimit()) {
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
                    for (String orderKey : orders) {
                        int result = compareInstanceUsingKey(orderKey, o1, o2);
                        if (result != 0)
                            return result;
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

    public MessageExchangeDAO createMessageExchange(final String mexId, char dir) {
        MessageExchangeDAOImpl mex = new MessageExchangeDAOImpl(dir, mexId);
        mex.createTime  = new Date();
        
        // FIXME: Why is this necessary? We should explicitly remove these thigs -mbs
        
        synchronized (_mexStore) {
            _mexStore.put(mexId, mex);
            _mexList.add(mex);
        }
        cleanupDeadWood();
        
        // Removing right away on rollback
        onRollback(new Runnable() {
            public void run() {
                synchronized (_mexStore) {
                    MessageExchangeDAOImpl mexdao = _mexStore.remove(mexId);
                    if (mexdao != null) _mexList.remove(mexdao);
                }
            }
        });

        return mex;
    }
    
    /**
     * Remove old message exchanges from the Mex store.
     * 
     */
    private void cleanupDeadWood() {
        long now = System.currentTimeMillis();
        
        if (now  > _lastRemoval + (TIME_TO_LIVE/4)) {
            _lastRemoval = now;
            
            synchronized (_mexStore) {
                LinkedList trash = new LinkedList<MessageExchangeDAOImpl>();
                for (MessageExchangeDAOImpl mexdao : _mexList) {
                    long createtime = mexdao._createTime.getTime();
                    if (now-createtime> TIME_TO_LIVE) {
                        trash.add(mexdao);
                    } else 
                        break;
                }
                
                _mexList.removeAll(trash);
                _mexStore.values().removeAll(trash);
            }
        }

        
    }

    public MessageExchangeDAO getMessageExchange(String mexid) {
        synchronized (_mexStore) {
            return _mexStore.get(mexid);
        }
    }

    public ResourceRouteDAO getResourceRoute(String url, String method) {
        return _resRouteStore.get(url+"~"+method);
    }

    public void addResourceRoute(ResourceRouteDAOImpl rroute) {
        _resRouteStore.put(rroute.getUrl()+"~"+rroute.getMethod(), rroute);
    }

    public void cleanupResourceRoutes(Long piid) {
        Iterator<ResourceRouteDAOImpl> rrIter = _resRouteStore.values().iterator();
        while (rrIter.hasNext()) {
            ResourceRouteDAOImpl rr = rrIter.next();
            if (rr.getInstance().getInstanceId().equals(piid)) rrIter.remove();
        }
    }

    private int compareInstanceUsingKey(String key, ProcessInstanceDAO instanceDAO1, ProcessInstanceDAO instanceDAO2) {
        String s1 = null;
        String s2 = null;
        boolean ascending = true;
        String orderKey = key;
        if (key.startsWith("+") || key.startsWith("-")) {
            orderKey = key.substring(1, key.length());
            if (key.startsWith("-"))
                ascending = false;
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
            s1 = "" + process1.getVersion();
            s2 = "" + process2.getVersion();
        } else if ("status".equals(orderKey)) {
            s1 = "" + instanceDAO1.getState();
            s2 = "" + instanceDAO2.getState();
        } else if ("started".equals(orderKey)) {
            s1 = ISO8601DateParser.format(instanceDAO1.getCreateTime());
            s2 = ISO8601DateParser.format(instanceDAO2.getCreateTime());
        } else if ("last-active".equals(orderKey)) {
            s1 = ISO8601DateParser.format(instanceDAO1.getLastActiveTime());
            s2 = ISO8601DateParser.format(instanceDAO2.getLastActiveTime());
        }
        if (ascending)
            return s1.compareTo(s2);
        else
            return s2.compareTo(s1);
    }

    private boolean equalsOrWildcardMatch(String s1, String s2) {
        if (s1 == null || s2 == null)
            return false;
        if (s1.equals(s2))
            return true;
        if (s1.endsWith("*")) {
            if (s2.startsWith(s1.substring(0, s1.length() - 1)))
                return true;
        }
        if (s2.endsWith("*")) {
            if (s1.startsWith(s2.substring(0, s2.length() - 1)))
                return true;
        }
        return false;
    }

    public boolean dateMatch(List<String> dateFilters, Date instanceDate, InstanceFilter filter) {
        boolean match = true;
        for (String ddf : dateFilters) {
            String isoDate = ISO8601DateParser.format(instanceDate);
            String critDate = Filter.getDateWithoutOp(ddf);
            if (ddf.startsWith("=")) {
                if (!isoDate.startsWith(critDate))
                    match = false;
            } else if (ddf.startsWith("<=")) {
                if (!isoDate.startsWith(critDate) && isoDate.compareTo(critDate) > 0)
                    match = false;
            } else if (ddf.startsWith(">=")) {
                if (!isoDate.startsWith(critDate) && isoDate.compareTo(critDate) < 0)
                    match = false;
            } else if (ddf.startsWith("<")) {
                if (isoDate.compareTo(critDate) > 0)
                    match = false;
            } else if (ddf.startsWith(">")) {
                if (isoDate.compareTo(critDate) < 0)
                    match = false;
            }
        }
        return match;
    }

    public ScopeDAO getScope(Long siidl) {
        for (ProcessDaoImpl process : _store.values()) {
            for (ProcessInstanceDAO instance : process._instances.values()) {
                if (instance.getScope(siidl) != null)
                    return instance.getScope(siidl);
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
        CollectionsX.transform(dates, _events, new UnaryFunction<BpelEvent, Date>() {
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
        // TODO
        throw new UnsupportedOperationException();
    }

    public void defer(final Runnable runnable) {
        try {
            _txm.getTransaction().registerSynchronization(new Synchronization() {
                public void afterCompletion(int status) {
                }

                public void beforeCompletion() {
                    runnable.run();
                }
            });
       
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    
    public void onRollback(final Runnable runnable) {
        try {
            _txm.getTransaction().registerSynchronization(new Synchronization() {
                public void afterCompletion(int status) {
                    if (status != Status.STATUS_COMMITTED) runnable.run();
                }
                
                public void beforeCompletion() {}
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
