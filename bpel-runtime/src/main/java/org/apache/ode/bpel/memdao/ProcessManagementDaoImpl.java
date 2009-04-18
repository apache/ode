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

import java.util.Collection;
import java.util.Date;

import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessManagementDAO;

public class ProcessManagementDaoImpl extends DaoBaseImpl implements ProcessManagementDAO {
	public Object[] findFailedCountAndLastFailedDateForProcessId(BpelDAOConnection conn, String status, String processId) {
        Date lastFailureDt = null;
        int failureInstances = 0;

        InstanceFilter instanceFilter = new InstanceFilter("status=" + status + " pid="+ processId);
        for (ProcessInstanceDAO instance : conn.instanceQuery(instanceFilter)) {
            int count = instance.getActivityFailureCount();
            if (count > 0) {
                ++failureInstances;
                Date failureDt = instance.getActivityFailureDateTime();
                if (lastFailureDt == null || lastFailureDt.before(failureDt))
                    lastFailureDt = failureDt;
            }
        }

        return new Object[] {failureInstances, lastFailureDt};
	}
	
	public void prefetchActivityFailureCounts(Collection<ProcessInstanceDAO> instances) {
		// do nothing 
	}
}
