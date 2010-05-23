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

package org.apache.ode.dao.bpel;

import java.util.List;

import org.apache.ode.dao.bpel.ActivityRecoveryDAO;
import org.apache.ode.dao.bpel.CorrelationSetDAO;
import org.apache.ode.dao.bpel.FaultDAO;
import org.apache.ode.dao.bpel.MessageDAO;
import org.apache.ode.dao.bpel.MessageExchangeDAO;
import org.apache.ode.dao.bpel.MessageRouteDAO;
import org.apache.ode.dao.bpel.PartnerLinkDAO;
import org.apache.ode.dao.bpel.ProcessDAO;
import org.apache.ode.dao.bpel.ScopeDAO;
import org.apache.ode.dao.bpel.XmlDataDAO;

public interface ProcessInstanceProfileDAO extends ProcessProfileDAO {
    ProcessDAO getProcess();
    
    List<ActivityRecoveryDAO> findActivityRecoveriesByInstance();

    List<CorrelationSetDAO> findCorrelationSetsByInstance();

    List<FaultDAO> findFaultsByInstance();

    List<MessageDAO> findMessagesByInstance();

    List<MessageExchangeDAO> findMessageExchangesByInstance();

    List<MessageRouteDAO> findMessageRoutesByInstance();

    List<PartnerLinkDAO> findPartnerLinksByInstance();

    List<ScopeDAO> findScopesByInstance();

    List<XmlDataDAO> findXmlDataByInstance();
    
    int countEventsByInstance();
}