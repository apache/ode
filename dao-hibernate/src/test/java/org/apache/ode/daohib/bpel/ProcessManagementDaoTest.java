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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessManagementDAO.FailedSummaryValue;
import org.apache.ode.bpel.dao.ProcessManagementDAO.InstanceSummaryKey;

/**
 * Testing BpelDAOConnectionImpl.listInstance. We're just producing a lot of
 * different filter combinations and test if they execute ok. To really test
 * that the result is the one expected would take a huge test database (with at
 * least a process and an instance for every possible combination).
 */
public class ProcessManagementDaoTest extends BaseTestDAO {

    protected void setUp() throws Exception {
        initTM();
    }

    protected void tearDown() throws Exception {
        stopTM();
    }

    public void testInstanceSummary() throws Exception {
        Set<String> pids = new HashSet<String>();
        QName pid = QName.valueOf("{ns}pid");
        pids.add(pid.toString());
        ProcessDAO p = daoConn.createProcess(pid, QName.valueOf("{ns}type"), "abc", 1);
        ProcessInstanceDAO i = p.createInstance(p.addCorrelator("cor"));
        i.setState(ProcessState.STATE_COMPLETED_OK);
        Map<InstanceSummaryKey, Long> r = daoConn.getProcessManagement().countInstancesSummary(pids);
        System.out.println("resultSummary:" + r);
        assertEquals(1, r.size());
    }

    public void testInstanceSummaryFailures() throws Exception {
        Set<String> pids = new HashSet<String>();
        QName pid = QName.valueOf("{ns}pid");
        pids.add(pid.toString());
        ProcessDAO p = daoConn.createProcess(pid, QName.valueOf("{ns}type"), "abc", 1);
        ProcessInstanceDAO i = p.createInstance(p.addCorrelator("cor"));
        Map<String, FailedSummaryValue> r = daoConn.getProcessManagement().findFailedCountAndLastFailedDateForProcessIds(pids);
        System.out.println("resultSummary:" + r);
        assertEquals(0, r.size());
    }
}
