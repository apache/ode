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

import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.ActivityRecoveryDAO;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.utils.QNameUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A very simple, in-memory implementation of the {@link ProcessInstanceDAO}
 * interface.
 */
public class ProcessInstanceDaoImpl extends DaoBaseImpl implements ProcessInstanceDAO {
    private static final Collection<ScopeDAO> EMPTY_SCOPE_DAOS = Collections.emptyList();

    private short _previousState;
    private short _state;
    private Long _instanceId;
    private ProcessDaoImpl _processDao;
    private Object _soup;
    private Map<Long, ScopeDAO> _scopes = new HashMap<Long, ScopeDAO>();
    private Map<String, List<ScopeDAO>> _scopesByName = new HashMap<String, List<ScopeDAO>>();
    private Map<String, byte[]> _messageExchanges = new HashMap<String, byte[]>();
    private ScopeDAO _rootScope;
    private FaultDAO _fault;
    private CorrelatorDAO _instantiatingCorrelator;
    private BpelDAOConnection _conn;
    private int _failureCount;
    private Date _failureDateTime;
    private Map<String, ActivityRecoveryDAO> _activityRecoveries = new HashMap<String, ActivityRecoveryDAO>();

    // TODO: Remove this, we should be using the main event store...
    private List<ProcessInstanceEvent> _events = new ArrayList<ProcessInstanceEvent>();
    private Date _lastActive;
    private int _seq;

    ProcessInstanceDaoImpl(BpelDAOConnection conn, ProcessDaoImpl processDao, CorrelatorDAO correlator) {
        _state = 0;
        _processDao = processDao;
        _instantiatingCorrelator = correlator;
        _soup = null;
        _instanceId = IdGen.newProcessId();
        _conn = conn;
    }

    public XmlDataDAO[] getVariables(String variableName, int scopeModelId) {
        ArrayList<XmlDataDAO> res = new ArrayList<XmlDataDAO>();
        for (ScopeDAO scope : _scopes.values()) {
            if (scope.getModelId() == scopeModelId) {
                XmlDataDAO xmld = scope.getVariable(variableName);
                if (xmld != null)
                    res.add(xmld);
            }
        }
        return res.toArray(new XmlDataDAO[res.size()]);
    }

    public Set<CorrelationSetDAO> getCorrelationSets() {
        HashSet<CorrelationSetDAO> res = new HashSet<CorrelationSetDAO>();
        for (ScopeDAO scopeDAO : _scopes.values()) {
            res.addAll(scopeDAO.getCorrelationSets());
        }
        return res;
    }

    public CorrelationSetDAO getCorrelationSet(String name) {
        for (ScopeDAO scopeDAO : _scopes.values()) {
            if (scopeDAO.getCorrelationSet(name) != null)
                return scopeDAO.getCorrelationSet(name);
        }
        return null;
    }

    public void setFault(QName name, String explanation, int lineNo, int activityId, Element faultData) {
        _fault = new FaultDaoImpl(QNameUtils.fromQName(name), explanation, faultData, lineNo, activityId);
    }

    public void setFault(FaultDAO fault) {
        _fault = fault;
    }

    public FaultDAO getFault() {
        return _fault;
    }

    /**
     * @see ProcessInstanceDAO#getExecutionState()
     */
    public byte[] getExecutionState() {
        throw new IllegalStateException("In-memory instances are never serialized");
    }

    public void setExecutionState(byte[] bytes) {
        throw new IllegalStateException("In-memory instances are never serialized");
    }

    public Object getSoup() {
        return _soup;
    }

    public void setSoup(Object soup) {
        _soup = soup;
    }

    public byte[] getMessageExchange(String identifier) {
        byte[] mex = _messageExchanges.get(identifier);
        assert (mex != null);
        return mex;
    }

    /**
     * @see ProcessInstanceDAO#getProcess()
     */
    public ProcessDAO getProcess() {
        return _processDao;
    }

    /**
     * @see ProcessInstanceDAO#getRootScope()
     */
    public ScopeDAO getRootScope() {
        return _rootScope;
    }

    /**
     * @see ProcessInstanceDAO#setState(short)
     */
    public void setState(short state) {
        _previousState = _state;
        _state = state;
        if (state == ProcessState.STATE_TERMINATED) {
            for (CorrelatorDAO correlatorDAO : _processDao.getCorrelators()) {
                correlatorDAO.removeRoutes(null, this);
            }
        }
    }

    /**
     * @see ProcessInstanceDAO#getState()
     */
    public short getState() {
        return _state;
    }

    public void addMessageExchange(String identifier, byte[] data) {
        assert (!_messageExchanges.containsKey(identifier));
        _messageExchanges.put(identifier, data);
    }

    public ScopeDAO createScope(ScopeDAO parentScope, String scopeType, int scopeModelId) {
        ScopeDaoImpl newScope = new ScopeDaoImpl(this, parentScope, scopeType, scopeModelId);
        _scopes.put(newScope.getScopeInstanceId(), newScope);
        List<ScopeDAO> namedScopes = _scopesByName.get(scopeType);
        if (namedScopes == null) {
            namedScopes = new LinkedList<ScopeDAO>();
            _scopesByName.put(scopeType, namedScopes);
        }
        namedScopes.add(newScope);
        if (parentScope == null) {
            assert _rootScope == null;
            _rootScope = newScope;
        }

        return newScope;
    }

    public Long getInstanceId() {
        return _instanceId;
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getScope(java.lang.Long)
     */
    public ScopeDAO getScope(Long scopeInstanceId) {
        return _scopes.get(scopeInstanceId);
    }

    public List<ProcessInstanceEvent> getEvents(int idx, int count) {
        int sidx = Math.max(idx, 0);
        sidx = Math.min(sidx, _events.size() - 1);
        int eidx = Math.min(sidx + count, _events.size());
        return _events.subList(sidx, eidx);
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#insertBpelEvent(org.apache.ode.bpel.evt.ProcessInstanceEvent)
     */
    public void insertBpelEvent(ProcessInstanceEvent event) {
        _events.add(event);
    }

    public int getEventCount() {
        return _events.size();
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getInstantiatingCorrelator()
     */
    public CorrelatorDAO getInstantiatingCorrelator() {
        return _instantiatingCorrelator;
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getScopes(java.lang.String)
     */
    public Collection<ScopeDAO> getScopes(String scopeName) {
        List<ScopeDAO> scopes = _scopesByName.get(scopeName);
        return (scopes == null ? EMPTY_SCOPE_DAOS : scopes);
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getPreviousState()
     */
    public short getPreviousState() {
        return _previousState;
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#getLastActiveTime()
     */
    public Date getLastActiveTime() {
        return _lastActive;
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#setLastActiveTime(java.util.Date)
     */
    public void setLastActiveTime(Date dt) {
        _lastActive = dt;
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessInstanceDAO#finishCompletion()
     */
    public void finishCompletion() {
        // make sure we have completed.
        assert (ProcessState.isFinished(this.getState()));
        // let our process know that we've done our work.
        this.getProcess().instanceCompleted(this);
    }

    public void delete(Set<CLEANUP_CATEGORY> cleanupCategories) {
        delete(cleanupCategories, true);
    }

    public void delete(Set<CLEANUP_CATEGORY> cleanupCategories, boolean deleteMyRoleMex) {
        _processDao._instances.remove(_instanceId);
    }

    public Collection<ScopeDAO> getScopes() {
        return _scopes.values();
    }

    public EventsFirstLastCountTuple getEventsFirstLastCount() {
        EventsFirstLastCountTuple ret = new EventsFirstLastCountTuple();
        ret.count = _events.size();
        Date first = new Date();
        Date last = new Date(0);
        for (ProcessInstanceEvent event : _events) {
            if (event.getTimestamp().before(first))
                first = event.getTimestamp();
            if (event.getTimestamp().after(last))
                last = event.getTimestamp();
        }
        ret.first = first;
        ret.last = last;
        return ret;
    }

    public int getActivityFailureCount() {
        return _failureCount;
    }

    public Date getActivityFailureDateTime() {
        return _failureDateTime;
    }

    public Collection<ActivityRecoveryDAO> getActivityRecoveries() {
        return _activityRecoveries.values();
    }

    public void createActivityRecovery(String channel, long activityId, String reason, Date dateTime, Element data,
                                       String[] actions, int retries) {
        _activityRecoveries
                .put(channel, new ActivityRecoveryDAOImpl(channel, activityId, reason, dateTime, data, actions, retries));
        _failureCount = _activityRecoveries.size();
        _failureDateTime = dateTime;
    }

    public void deleteActivityRecovery(String channel) {
        _activityRecoveries.remove(channel);
        _failureCount = _activityRecoveries.size();
    }

    public synchronized long genMonotonic() {
        return ++_seq;
    }

    static class ActivityRecoveryDAOImpl implements ActivityRecoveryDAO {

        private long _activityId;

        private String _channel;

        private String _reason;

        private Element _details;

        private Date _dateTime;

        private String _actions;

        private int _retries;

        ActivityRecoveryDAOImpl(String channel, long activityId, String reason, Date dateTime, Element details, String[] actions,
                                int retries) {
            _activityId = activityId;
            _channel = channel;
            _reason = reason;
            _details = details;
            _dateTime = dateTime;
            _actions = actions[0];
            for (int i = 1; i < actions.length; ++i)
                _actions += " " + actions[i];
            _retries = retries;
        }

        public long getActivityId() {
            return _activityId;
        }

        public String getChannel() {
            return _channel;
        }

        public String getReason() {
            return _reason;
        }

        public Element getDetails() {
            return _details;
        }

        public Date getDateTime() {
            return _dateTime;
        }

        public String getActions() {
            return _actions;
        }

        public String[] getActionsList() {
            return _actions.split(" ");
        }

        public int getRetries() {
            return _retries;
        }

    }

    void removeRoutes(String routeGroupId) {
        for (CorrelatorDaoImpl correlator : _processDao._correlators.values())
            correlator._removeRoutes(routeGroupId, this);
    }

    public BpelDAOConnection getConnection() {
        return _conn;
    }

    public String toString() {
        return "mem.instance(type=" + _processDao.getType() + " iid=" + _instanceId + ")";
    }

    public Collection<String> getMessageExchangeIds() {
        return _messageExchanges.keySet();
    }
}
