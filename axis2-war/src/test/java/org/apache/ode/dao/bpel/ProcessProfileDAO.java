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
import org.apache.ode.dao.bpel.CorrelatorDAO;
import org.apache.ode.dao.bpel.FaultDAO;
import org.apache.ode.dao.bpel.MessageDAO;
import org.apache.ode.dao.bpel.MessageExchangeDAO;
import org.apache.ode.dao.bpel.MessageRouteDAO;
import org.apache.ode.dao.bpel.PartnerLinkDAO;
import org.apache.ode.dao.bpel.ProcessInstanceDAO;
import org.apache.ode.dao.bpel.ScopeDAO;
import org.apache.ode.dao.bpel.XmlDataDAO;

public interface ProcessProfileDAO {
    boolean doesProcessExist();
    
    List<ProcessInstanceDAO> findInstancesByProcess();

    List<ActivityRecoveryDAO> findActivityRecoveriesByProcess();

    List<CorrelationSetDAO> findCorrelationSetsByProcess();

    List<CorrelatorDAO> findCorrelatorsByProcess();

    List<FaultDAO> findFaultsByProcess();

    List<MessageDAO> findMessagesByProcess();

    List<MessageExchangeDAO> findMessageExchangesByProcess();

    List<MessageRouteDAO> findMessageRoutesByProcess();

    List<PartnerLinkDAO> findPartnerLinksByProcess();

    List<ScopeDAO> findScopesByProcess();

    List<XmlDataDAO> findXmlDataByProcess();

    int countEventsByProcess();
}