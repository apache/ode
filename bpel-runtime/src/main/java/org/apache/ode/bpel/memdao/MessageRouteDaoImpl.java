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

import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;


/**
 * A very simple, in-memory implementation of the {@link MessageRouteDAO} interface.
 */
class MessageRouteDaoImpl extends DaoBaseImpl implements MessageRouteDAO {
    ProcessInstanceDaoImpl _instance;
    String _groupId;
    CorrelationKeySet _ckeySet;
    int _idx;
    String _route;

    MessageRouteDaoImpl(ProcessInstanceDaoImpl owner, String groupId, CorrelationKeySet ckeySet, int idx, String routePolicy) {
        _instance = owner;
        _groupId = groupId;
        _ckeySet = ckeySet;
        _idx = idx;
        _route = routePolicy;
    }

    public ProcessInstanceDAO getTargetInstance() {
        return _instance;
    }

    public String getGroupId() {
        return _groupId;
    }

    public int getIndex() {
        return _idx;
    }

    public String getRoute() {
        return _route;
    }

    public CorrelationKeySet getCorrelationKeySet() {
        return _ckeySet;
    }

    public void setCorrelationKey(CorrelationKey key) {
        _ckeySet = new CorrelationKeySet();
        _ckeySet.add(key);
    }

    public CorrelationKey getCorrelationKey() {
        return _ckeySet.iterator().next();
    }

    public void setCorrelationKeySet(CorrelationKeySet keySet) {
        _ckeySet = keySet;
    }
}
