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
import org.apache.ode.bpel.engine.OutstandingRequestManager;
import org.apache.ode.bpel.engine.ReplacementMapImpl;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.runtime.Selector;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Set;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.io.ObjectStreamClass;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Migrates the database from using single correlations to multiple correlations support.
 */
public class CorrelationKeySetMigration implements Migration {
    private static final Log __log = LogFactory.getLog(CorrelationKeySetMigration.class);

    public boolean migrate(Set<BpelProcess> registeredProcesses, BpelDAOConnection connection) {
        boolean v1First = true;
        for (BpelProcess process : registeredProcesses) {
            ProcessDAO processDao = connection.getProcess(process.getConf().getProcessId());
            Collection<ProcessInstanceDAO> pis = processDao.getActiveInstances();

            // Migrate the correlation key stored in the jacob state of the instance
            for (ProcessInstanceDAO instance : pis) {
                __log.debug("Migrating correlation key in jacob for instance " + instance.getInstanceId());
                OProcess oproc = findOProcess(registeredProcesses, instance.getProcess().getProcessId());
                if (v1First) {
                    if (!updateV1Key(instance, oproc)) {
                        v1First = false;
                        updateV2Key(instance, oproc);
                    }
                } else {
                    if (!updateV2Key(instance, oproc)) {
                        v1First = true;
                        updateV1Key(instance, oproc);
                    }
                }
            }
        }

        return true;
    }

    private boolean updateV1Key(ProcessInstanceDAO instance, OProcess oproc) {
        ExecutionQueueImpl soup;
        try {
            soup = readOldState(instance, oproc, getClass().getClassLoader(), true);
            if (soup == null) return false;
        } catch (Exception e) {
            __log.debug("  failed to read a v1 state for instance " + instance.getInstanceId());
            ExecutionQueueImpl._classDescriptors.clear();
            return false;
        }
        try {
            OutstandingRequestManager orm = (OutstandingRequestManager) soup.getGlobalData();
            for (OutstandingRequestManager.Entry entry : orm._byChannel.values()) {
                Selector[] newSelectors = new Selector[entry.selectors.length];
                int index = 0;
                for (Object selector : entry.selectors) {
                    OldSelector sel = (OldSelector)selector;
                    Object selCKey = sel.correlationKey;
                    if (selCKey != null) {
                        OldCorrelationKey old = (OldCorrelationKey) selCKey;
                        __log.debug("   Changing V1 key " + old.toCanonicalString());

                        CorrelationKeySet newKeySet = new CorrelationKeySet();
                        newKeySet.add(new CorrelationKey(""+old.getCSetId(), old.getValues()));
                        Selector newSelector = new Selector(sel.idx, sel.plinkInstance, sel.opName,
                                sel.oneWay, sel.messageExchangeId, newKeySet, "one");
                        newSelector.correlationKey = new CorrelationKey(""+old.getCSetId(), old.getValues());
                        newSelectors[index++] = newSelector;
                    }
                }
                entry.selectors = newSelectors;
            }

            writeOldState(instance, soup);
        } finally {
            ExecutionQueueImpl._classDescriptors.clear();
        }
        return true;
    }

    private boolean updateV2Key(ProcessInstanceDAO instance, OProcess oproc) {
        ExecutionQueueImpl soup;
        try {
            soup = readOldState(instance, oproc, getClass().getClassLoader(), false);
            if (soup == null) return false;
        } catch (Exception e) {
            __log.debug("  failed to read a v2 state for instance " + instance.getInstanceId());
            ExecutionQueueImpl._classDescriptors.clear();
            return false;
        }
        OutstandingRequestManager orm = (OutstandingRequestManager) soup.getGlobalData();
        for (OutstandingRequestManager.Entry entry : orm._byChannel.values()) {
            Selector[] newSelectors = new Selector[entry.selectors.length];
            int index = 0;
            for (Object selector : entry.selectors) {
                OldSelector sel = (OldSelector)selector;
                CorrelationKey selCKey = (CorrelationKey) sel.correlationKey;
                if (selCKey != null) {
                    __log.debug("   Changing V2 key " + selCKey.toCanonicalString());

                    CorrelationKeySet newKeySet = new CorrelationKeySet();
                    newKeySet.add(new CorrelationKey(""+selCKey.getCorrelationSetName(), selCKey.getValues()));
                    Selector newSelector = new Selector(sel.idx, sel.plinkInstance, sel.opName,
                            sel.oneWay, sel.messageExchangeId, newKeySet, "one");
                    newSelector.correlationKey = new CorrelationKey(""+selCKey.getCorrelationSetName(), selCKey.getValues());
                    newSelectors[index++] = newSelector;
                }
            }
            entry.selectors = newSelectors;
        }

        writeOldState(instance, soup);
        return true;
    }

    private ExecutionQueueImpl readOldState(ProcessInstanceDAO instance, OProcess oprocess,
                                            ClassLoader cl, boolean changeKey) {
        if (instance.getExecutionState() == null) return null;
        try {
            ExecutionQueueImpl soup = new ExecutionQueueImpl(cl);
            ObjectStreamClass osc;
            if (changeKey) {
                osc = ObjectStreamClass.lookup(Class.forName(
                        "org.apache.ode.bpel.engine.migration.OldCorrelationKey", true, cl));
                ExecutionQueueImpl._classDescriptors.put("org.apache.ode.bpel.common.CorrelationKey", osc);
            }
            osc = ObjectStreamClass.lookup(Class.forName(
                    "org.apache.ode.bpel.engine.migration.OldSelector", true, cl));
            ExecutionQueueImpl._classDescriptors.put("org.apache.ode.bpel.runtime.Selector", osc);
            osc = ObjectStreamClass.lookup(Class.forName(
                    "[Lorg.apache.ode.bpel.engine.migration.OldSelector;", true, getClass().getClassLoader()));
            ExecutionQueueImpl._classDescriptors.put("[Lorg.apache.ode.bpel.runtime.Selector;", osc);

            soup.setReplacementMap(new ReplacementMapImpl(oprocess));
            ByteArrayInputStream iis = new ByteArrayInputStream(instance.getExecutionState());
            soup.read(iis);
            return soup;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeOldState(ProcessInstanceDAO instance, ExecutionQueueImpl soup) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            soup.write(bos);
            bos.close();
            instance.setExecutionState(bos.toByteArray());
            ExecutionQueueImpl._classDescriptors.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OProcess findOProcess(Set<BpelProcess> registeredProcesses, QName name) {
        for (BpelProcess process : registeredProcesses) {
            if (process.getConf().getProcessId().equals(name)) return process.getOProcess();
        }
        return null;
    }

}
