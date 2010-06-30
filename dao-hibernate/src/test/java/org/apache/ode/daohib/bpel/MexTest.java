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
import java.util.List;
import java.util.Map;

import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HCorrelator;

/**
 * Testing BpelDAOConnectionImpl.listInstance. We're just producing a lot of
 * different filter combinations and test if they execute ok. To really test
 * that the result is the one expected would take a huge test database (with at
 * least a process and an instance for every possible combination).
 */
public class MexTest extends BaseTestDAO {

    private Map<String, List> filterElmts;
    private ArrayList<String> order;

    protected void setUp() throws Exception {
        initTM();
    }

    protected void tearDown() throws Exception {
        stopTM();
    }

    public void test() throws Exception {
        MessageExchangeDAO mex = daoConn.createMessageExchange('M');
        mex.lockPremieMessages();
        
        SessionManager sm = ((BpelDAOConnectionImpl) daoConn)._sm;
        HCorrelator correlator = new HCorrelator();
        correlator.setCorrelatorId("abc");
        sm.getSession().save(correlator);
        new CorrelatorDaoImpl(sm, correlator).dequeueMessage(new CorrelationKeySet("@2[12~a~b]"));
    }
}
