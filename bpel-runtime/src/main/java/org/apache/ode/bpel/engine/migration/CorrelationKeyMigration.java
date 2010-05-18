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
import org.apache.ode.bpel.engine.ReplacementMapImpl;
import org.apache.ode.bpel.engine.OutstandingRequestManager;
import org.apache.ode.bpel.dao.*;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.o.*;
import org.apache.ode.bpel.runtime.Selector;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.wsdl.Operation;
import java.util.*;
import java.io.ObjectStreamClass;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Migrates the correlation key values to a scheme containing the OModel correlation
 * set id to one using its name. So something like 1~abc~de will become foo~abc~de.
 */
public class CorrelationKeyMigration implements Migration {
    private static final Log __log = LogFactory.getLog(CorrelationKeyMigration.class);

    public boolean migrate(Set<BpelProcess> registeredProcesses, BpelDAOConnection connection) {
        // Map only used to avoid duplicates, set would force to re-implement equals
        HashMap<Long,ProcessInstanceDAO> instances = new HashMap<Long,ProcessInstanceDAO>();

        // Migrate correlation set values
        Collection<CorrelationSetDAO> csets = connection.getActiveCorrelationSets();
        for (CorrelationSetDAO cset : csets) {
            CorrelationKey ckey = cset.getValue();
            instances.put(cset.getInstance().getInstanceId(), cset.getInstance());
            if (ckey != null) {
                __log.debug("Correlation set id " + cset.getCorrelationSetId() + " key " + ckey);
                Integer ckeyInt = asInt(ckey.getCorrelationSetName());
                if (ckeyInt != null) {
                    OScope.CorrelationSet ocset = findCorrelationById(ckeyInt, registeredProcesses, cset.getProcess().getProcessId());
                    if (ocset == null) __log.debug("Correlation set not found, couldn't upgrade set " + ckey.toCanonicalString());
                    else {
                        cset.setValue(null, new CorrelationKey(ocset.name, ckey.getValues()));
                    }
                }
            }
        }

        // Migrate routes and message queue for each correlator
        for (BpelProcess process : registeredProcesses) {
            __log.debug("Migrating correlators for process " + process.getConf().getProcessId());
            ProcessDAO processDao = connection.getProcess(process.getConf().getProcessId());

            for (OPartnerLink plink : process.getOProcess().getAllPartnerLinks()) {
                if (plink.hasMyRole()) {
                    for (Iterator opI = plink.myRolePortType.getOperations().iterator(); opI.hasNext();) {
                        Operation op = (Operation)opI.next();
                        try {
                            CorrelatorDAO corr = processDao.getCorrelator(plink.getName() + "." + op.getName());
                            // Changing all routes
                            if (corr != null) {
                                for (MessageRouteDAO routeDAO : corr.getAllRoutes()) {
                                    CorrelationKey oldKey = routeDAO.getCorrelationKey();
                                    if (oldKey != null) {
                                        Integer ckeyInt = asInt(oldKey.getCorrelationSetName());
                                        if (ckeyInt != null) {
                                            OScope.CorrelationSet ocset = findCorrelationById(ckeyInt, registeredProcesses, process.getConf().getProcessId());
                                            if (ocset == null) __log.debug("Correlation set not found, couldn't upgrade route " + oldKey.toCanonicalString());
                                            else {
                                                routeDAO.setCorrelationKey(new CorrelationKey(ocset.name, oldKey.getValues()));
                                            }
                                        }
                                    }
                                }

                                // Changing all queued messages
                                for (CorrelatorMessageDAO corrMsgDAO : corr.getAllMessages()) {
                                    CorrelationKey oldKey = corrMsgDAO.getCorrelationKey();
                                    if (oldKey != null) {
                                        Integer ckeyInt = asInt(oldKey.getCorrelationSetName());
                                        if (ckeyInt != null) {
                                            OScope.CorrelationSet ocset = findCorrelationById(ckeyInt, registeredProcesses, process.getConf().getProcessId());
                                            if (ocset == null) __log.debug("Correlation set not found, couldn't upgrade route " + oldKey.toCanonicalString());
                                            else {
                                                corrMsgDAO.setCorrelationKey(new CorrelationKey(ocset.name, oldKey.getValues()));
                                            }
                                        }
                                    }
                                }
                            __log.debug("Migrated routes and message queue for correlator " + plink.getName() + "." + op.getName());
                            }
                        } catch (IllegalArgumentException e) {
                            __log.debug("Correlator with id " + plink.getId() + "." +
                                    op.getName() + " couldn't be found, skipping.");
                        }
                    }
                }
            }
        }

        return true;
    }

    private Integer asInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private OScope.CorrelationSet findCorrelationById(int ckeyInt, Set<BpelProcess> processes, QName processId) {
        for (BpelProcess process : processes) {
            if (process.getConf().getProcessId().equals(processId)) {
                OBase ocset = process.getOProcess().getChild(ckeyInt);
                if (ocset instanceof OScope.CorrelationSet) return (OScope.CorrelationSet)ocset;
            }
        }
        return null;
    }

}
