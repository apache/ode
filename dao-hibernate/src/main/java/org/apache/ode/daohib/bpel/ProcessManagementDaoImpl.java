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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.InstanceFilter.StatusKeys;
import org.apache.ode.bpel.dao.ProcessManagementDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;
import org.hibernate.Query;

public class ProcessManagementDaoImpl extends HibernateDao implements ProcessManagementDAO {
    protected ProcessManagementDaoImpl(SessionManager sessionManager) {
        super(sessionManager, null);
    }

    public Map<InstanceSummaryKey, Long> countInstancesSummary(Set<String> pids) {
        Map<InstanceSummaryKey, Long> result = new HashMap<InstanceSummaryKey, Long>();
        if (!pids.isEmpty()) {
            for (StatusKeys status : InstanceFilter.StatusKeys.values()) {
                Query query = getSession().getNamedQuery(HProcessInstance.COUNT_INSTANCES_BY_PROCESSES_IDS_AND_STATES);
                query.setParameterList("states", new InstanceFilter("status=" + status.toString()).convertFilterState());
                query.setParameterList("processIds", pids);
                for (Object o : query.list()) {
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
            Query query = getSession().getNamedQuery(HProcessInstance.COUNT_FAILED_INSTANCES_BY_PROCESSES_IDS_AND_STATES);
            query.setParameterList("processIds", pids);
            for (Object o : query.list()) {
                Object[] row = (Object[]) o;
                result.put(row[0].toString(), new FailedSummaryValue((Long) row[1], (Date) row[2]));
            }
        }
        return result;
    }
}
