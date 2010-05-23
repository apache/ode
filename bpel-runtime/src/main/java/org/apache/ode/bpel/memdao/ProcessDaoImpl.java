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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;

/**
 * A very simple, in-memory implementation of the {@link ProcessDAO} interface.
 */
class ProcessDaoImpl extends DaoBaseImpl implements ProcessDAO {
    private static final Log __log = LogFactory.getLog(ProcessDaoImpl.class);

    private QName _processId;
    private QName _type;
    private long _version;
    final Map<String, CorrelatorDaoImpl> _correlators = new ConcurrentHashMap<String, CorrelatorDaoImpl>();
    protected final Map<Long, ProcessInstanceDAO> _instances = new ConcurrentHashMap<Long, ProcessInstanceDAO>();
    protected final Map<Long, Long> _instancesAge = new ConcurrentHashMap<Long, Long>();
    protected final Map<Integer, PartnerLinkDAO> _plinks = new ConcurrentHashMap<Integer, PartnerLinkDAO>();
    private Map<QName, ProcessDaoImpl> _store;
    private BpelDAOConnectionImpl _conn;
    private int _executionCount = 0;
    private Collection<Long> _instancesToRemove = new ConcurrentLinkedQueue<Long>();
    private volatile long _lastRemoval = 0;

    private String _guid;

    public ProcessDaoImpl(BpelDAOConnectionImpl conn, Map<QName, ProcessDaoImpl> store,
                          QName processId, QName type, String guid, long version) {
        if (__log.isDebugEnabled()) {
            __log.debug("Creating ProcessDao object for process \"" + processId + "\".");
        }

        _guid = guid;
        _conn = conn;
        _store = store;
        _processId = processId;
        _type = type;
        _version = version;
    }

    public Serializable getId() {
        return _guid;
    }
    
    public QName getProcessId() {
        return _processId;
    }

    public CorrelatorDAO getCorrelator(String cid) {
        CorrelatorDAO ret = _correlators.get(cid);
        if (ret == null) {
            throw new IllegalArgumentException("no such correlator: " + cid);
        }
        return ret;
    }

    public Collection<CorrelatorDAO> getCorrelators() {
        // Note: _correlators.values() is a Collection<CorrealatorDaoImpl>. We can't just return this object
        // since Collection<CorrelatorDAO> is /not/ assignment compatible with Collection<CorrelatorDaoImpl>. 
        // However, a immutable Collection<CorrelationDAO> is assignment compatible with Collection<CorrelatorDaoImpl>,
        // but.... we need to introduce some ambiguity into the type hierarchy so that Java will infer the correct type.
        
        // Make an ambiguous collection.
        Collection<? extends CorrelatorDAO> foo =  _correlators.values();

        // In order to get a collection of the super-type from a sub-type we must make the collection read-only. 
        return Collections.unmodifiableCollection(foo);
    }
    
    public void removeRoutes(String routeId, ProcessInstanceDAO target) {
        for (CorrelatorDAO correlatorDAO : _correlators.values()) {
            correlatorDAO.removeRoutes(routeId, target);
        }
    }

    public ProcessInstanceDAO createInstance(CorrelatorDAO correlator) {
        final ProcessInstanceDaoImpl newInstance = new ProcessInstanceDaoImpl(_conn, this, correlator);
        _conn.defer(new Runnable() {
            public void run() {
                _instances.put(newInstance.getInstanceId(), newInstance);
                _instancesAge.put(newInstance.getInstanceId(), System.currentTimeMillis());
            }
        });

        discardOldInstances();
        
        // Removing right away on rollback
        final Long iid = newInstance.getInstanceId();
        _conn.onRollback(new Runnable() {
            public void run() {
                _instances.remove(iid);
                _instancesAge.remove(iid);
            }
        });

        _executionCount++;
        return newInstance;
    }

    public ProcessInstanceDAO getInstance(Long instanceId) {
        return _instances.get(instanceId);
    }

    public Collection<ProcessInstanceDAO> findInstance(CorrelationKey key) {
        ArrayList<ProcessInstanceDAO> result = new ArrayList<ProcessInstanceDAO>();
        for (ProcessInstanceDAO instance : _instances.values()) {
            for (CorrelationSetDAO corrSet : instance.getCorrelationSets()) {
                if (corrSet.getValue().equals(key)) result.add(instance);
            }
        }
        return result;
    }

    public void instanceCompleted(ProcessInstanceDAO instance) {
        // Cleaning up
        if (__log.isDebugEnabled())
          __log.debug("Removing completed process instance " + instance.getInstanceId() + " from in-memory store.");
        _instancesAge.remove(instance.getInstanceId());
        ProcessInstanceDAO removed = _instances.remove(instance.getInstanceId());
        if (removed == null) {
            // Checking for leftover instances that should be removed
            ArrayList<Long> removals = new ArrayList<Long>(_instancesToRemove);
            for (Long iid : removals) {
                _instances.remove(iid);
            }
            _instancesToRemove.removeAll(removals);

            // The instance can't be found probably because the transaction isn't committed yet and
            // it doesn't exist. Saving its id for later cleanup.
            _instancesToRemove.add(instance.getInstanceId());
        }
    }

    public void deleteProcessAndRoutes() {
        _store.remove(_processId);
    }
    
    public long getVersion() {
        return _version;
    }

    public String getDeployer() {
        return "nobody";
    }

    public QName getType() {
        return _type;
    }

    public CorrelatorDAO addCorrelator(String correlator) {
        CorrelatorDaoImpl corr = new CorrelatorDaoImpl(correlator, _conn);
        _correlators.put(corr.getCorrelatorId(), corr);
        return corr;
    }

    /**
     * Nothing to do.
     */
    public void update() {
        //TODO Check requirement for persisting.
    }

    public int getNumInstances() {
        // Instances are removed after execution, using a counter instead
        return _executionCount;
    }

    public ProcessInstanceDAO getInstanceWithLock(Long iid) {
        return getInstance(iid);
    }

    public int getActivityFailureCount() {
        return 0;  
    }

    public Date getActivityFailureDateTime() {
        return null;
    }

    public String getGuid() {
        return _guid;
    }
    
    public void setGuid(String guid) {
        _guid = guid;
    }

    public Collection<ProcessInstanceDAO> getActiveInstances() {
        ArrayList<ProcessInstanceDAO> pis = new ArrayList<ProcessInstanceDAO>();
        for (ProcessInstanceDAO processInstanceDAO : _instances.values()) {
            if (processInstanceDAO.getState() == ProcessState.STATE_ACTIVE)
                pis.add(processInstanceDAO);
        }
        return pis;
    }

    /**
     * Discard in-memory instances that exceeded their time-to-live to prevent memory leaks
     */
    void discardOldInstances() {
        long now = System.currentTimeMillis();
        if (now > _lastRemoval + (_conn._mexTtl/10)) {
            _lastRemoval = now;
            Object[] oldInstances = _instancesAge.keySet().toArray();
            for (int i=oldInstances.length-1; i>=0; i--) {
                Long id = (Long) oldInstances[i];
                Long age = _instancesAge.get(id);
                if (age != null && now-age > _conn._mexTtl) {
                    if (_instances.get(id) != null) {
                        __log.warn("Discarding in-memory instance "+id+" because it exceeded its time-to-live: "+_instances.get(id));
                    }
                    _instances.remove(id);
                    _instancesAge.remove(id);
                }
            }
        }
    }
}
