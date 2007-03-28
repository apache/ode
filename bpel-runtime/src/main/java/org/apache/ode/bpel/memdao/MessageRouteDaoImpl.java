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

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;


/**
 * A very simple, in-memory implementation of the {@link MessageRouteDAO} interface.
 */
class MessageRouteDaoImpl extends DaoBaseImpl implements MessageRouteDAO {
  ProcessInstanceDaoImpl _instance;
  String _groupId;
  CorrelationKey _ckey;
  int _idx;

  MessageRouteDaoImpl(ProcessInstanceDaoImpl owner, String groupId, CorrelationKey ckey, int idx) {
    _instance = owner;
    _groupId = groupId;
    _ckey = ckey;
    _idx = idx;
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
}
