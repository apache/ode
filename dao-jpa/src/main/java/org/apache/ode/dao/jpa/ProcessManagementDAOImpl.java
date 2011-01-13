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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.InstanceFilter.StatusKeys;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessManagementDAO;

public class ProcessManagementDAOImpl implements ProcessManagementDAO {
    private static final Log __log = LogFactory.getLog(ProcessManagementDAOImpl.class);

    private EntityManager em;

    public ProcessManagementDAOImpl(EntityManager em) {
        this.em = em;
    }

    public Object[] findFailedCountAndLastFailedDateForProcessId(BpelDAOConnection conn, String status, String processId) {
        Query query = em.createNamedQuery(ProcessInstanceDAOImpl.COUNT_FAILED_INSTANCES_BY_STATUS_AND_PROCESS_ID);
        query.setParameter("states", new InstanceFilter("status=" + status).convertFilterState());
        query.setParameter("processId", processId);

        return (Object[])query.getSingleResult();
    }

    public void prefetchActivityFailureCounts(Collection<ProcessInstanceDAO> instances) {
        if(__log.isTraceEnabled()) __log.trace("Prefetching activity failure counts for " + instances.size() + " instances.");

        if( instances.isEmpty() ) return;

        Query query = em.createNamedQuery(ActivityRecoveryDAOImpl.COUNT_ACTIVITY_RECOVERIES_BY_INSTANCES);
        query.setParameter("instances", instances);

        Map<Long, Long> countsByInstanceId = new HashMap<Long, Long>();
        for( Object instanceIdAndCount : query.getResultList() ) {
            Object instanceId = ((Object[])instanceIdAndCount)[0];
            Object count = ((Object[])instanceIdAndCount)[0];
            countsByInstanceId.put((Long)instanceId, (Long)count);
        }

        for( ProcessInstanceDAO instance : instances ) {
            Long count = countsByInstanceId.get(instance.getInstanceId());
            if( count != null ) {
                ((ProcessInstanceDAOImpl)instance).setActivityFailureCount(count.intValue());
            }
        }
    }

    public int countInstancesByPidAndString(BpelDAOConnection conn, QName pid, String status) {
        InstanceFilter instanceFilter = new InstanceFilter("status=" + status + " pid="+ pid);

        // TODO: this is grossly inefficient
        return conn.instanceQuery(instanceFilter).size();
    }

    public Map<InstanceSummaryKey, Long> countInstancesSummary(Set<String> pids) {
        Map<InstanceSummaryKey, Long> result = new HashMap<InstanceSummaryKey, Long>();
        if (!pids.isEmpty()) {
            for (StatusKeys status : InstanceFilter.StatusKeys.values()) {
                Query query = em.createNamedQuery(ProcessInstanceDAOImpl.COUNT_INSTANCES_BY_PROCESSES_IDS_AND_STATES);
                query.setParameter("states", new InstanceFilter("status=" + status.toString()).convertFilterState());
                query.setParameter("processIds", pids);
                for (Object o : query.getResultList()) {
                    Object[] row = (Object[]) o;
                    InstanceSummaryKey key = new InstanceSummaryKey(row[0].toString(), status.toString());
                    result.put(key, (Long) row[1]);
                }
            }
        }

        return result;
    }

    public Map<String, FailedSummaryValue> findFailedCountAndLastFailedDateForProcessIds(Set<String> pids) {
        Map<String, FailedSummaryValue> result = new HashMap<String, FailedSummaryValue>();
        if (!pids.isEmpty()) {
            Query query = em.createNamedQuery(ProcessInstanceDAOImpl.COUNT_FAILED_INSTANCES_BY_STATUS_AND_PROCESS_IDS);
            query.setParameter("processIds", pids);
            for (Object o : query.getResultList()) {
                System.out.println(o);
                Object[] row = (Object[]) o;
                result.put(row[0].toString(), new FailedSummaryValue((Long) row[1], (Date) row[2]));
            }
        }
        return result;

    }
}
