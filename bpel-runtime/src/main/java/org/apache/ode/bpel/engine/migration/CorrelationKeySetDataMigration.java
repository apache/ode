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

package org.apache.ode.bpel.engine.migration;

import org.apache.ode.bpel.engine.BpelProcess;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Operation;
import java.util.Set;
import java.util.Iterator;

/**
 * Changes the scheme of the correlation key value in the routes to use @2[...]
 */
public class CorrelationKeySetDataMigration implements Migration {
    private static final Log __log = LogFactory.getLog(CorrelationKeyMigration.class);

    public boolean migrate(Set<BpelProcess> registeredProcesses, BpelDAOConnection connection) {
        for (BpelProcess process : registeredProcesses) {
            __log.debug("Migrating correlators data for process " + process.getConf().getProcessId());
            ProcessDAO processDao = connection.getProcess(process.getConf().getProcessId());

            for (OPartnerLink plink : process.getOProcess().getAllPartnerLinks()) {
                if (plink.hasMyRole()) {
                    for (Iterator opI = plink.myRolePortType.getOperations().iterator(); opI.hasNext();) {
                        Operation op = (Operation)opI.next();
                        CorrelatorDAO corr = processDao.getCorrelator(plink.getName() + "." + op.getName());
                        // Changing all routes
                        if (corr != null) {
                            for (MessageRouteDAO routeDAO : corr.getAllRoutes()) {
                                CorrelationKey oldKey = routeDAO.getCorrelationKey();
                                if (oldKey != null) {
                                    CorrelationKeySet keySet = new CorrelationKeySet();
                                    keySet.add(oldKey);
                                    routeDAO.setCorrelationKeySet(keySet);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
