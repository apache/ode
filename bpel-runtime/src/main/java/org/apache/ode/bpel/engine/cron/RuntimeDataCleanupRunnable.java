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
package org.apache.ode.bpel.engine.cron;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.FilteredInstanceDeletable;
import org.apache.ode.bpel.engine.Contexts;
import org.apache.ode.bpel.engine.BpelServerImpl.ContextsAware;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.bpel.iapi.ProcessConf.CleanupInfo;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.bpel.iapi.Scheduler.MapSerializableRunnable;

public class RuntimeDataCleanupRunnable implements MapSerializableRunnable, ContextsAware {
    private final Log __log = LogFactory.getLog(RuntimeDataCleanupRunnable.class);

    private static final long serialVersionUID = 1L;

    private transient Contexts _contexts;

    private int _transactionSize;
    private CleanupInfo _cleanupInfo;
    private QName _pid;
    private Set<QName> _pidsToExclude;

    public RuntimeDataCleanupRunnable() {
    }

    @SuppressWarnings("unchecked")
    public void restoreFromDetails(JobDetails details) {
        _cleanupInfo = (CleanupInfo)details.getDetailsExt().get("cleanupInfo");
        _transactionSize = (Integer)details.getDetailsExt().get("transactionSize");
        _pid = details.getProcessId();
        _pidsToExclude = (Set<QName>)details.getDetailsExt().get("pidsToExclude");
    }

    public void storeToDetails(JobDetails details) {
        // we don't serialize
    }

    public void setContexts(Contexts contexts) {
        _contexts = contexts;
    }

    public void run() {
        __log.info("CRON CLEAN.run().");

        for( String filter : _cleanupInfo.getFilters() ) {
            if( _pid != null ) {
                filter += " pid=" + _pid;
            } else if( _pidsToExclude != null ) {
                StringBuffer pids = new StringBuffer();
                for( QName pid : _pidsToExclude ) {
                    if( pids.length() > 0 ) {
                        pids.append("|");
                    }
                    pids.append(pid);
                }
                filter += " pid<>" + pids.toString();
            }

            if( filter.trim().length() > 0 ) {
                __log.info("CRON CLEAN.run(" + filter + ")");
                long numberOfDeletedInstances = 0;
                do {
                    numberOfDeletedInstances = cleanInstances(filter, _cleanupInfo.getCategories(), _transactionSize);
                } while( numberOfDeletedInstances == _transactionSize );
            }
        }
    }

    int cleanInstances(String filter, final Set<CLEANUP_CATEGORY> categories, int limit) {
        if (__log.isDebugEnabled()) {
            __log.debug("CRON CLEAN using filter: " + filter + ", limit: " + limit);
        }

        final InstanceFilter instanceFilter = new InstanceFilter(filter, "", limit);
        try {
            if( _contexts.scheduler != null ) {
                return _contexts.scheduler.execTransaction(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        BpelDAOConnection con = _contexts.dao.getConnection();
                        if( con instanceof FilteredInstanceDeletable ) {
                            return ((FilteredInstanceDeletable)con).deleteInstances(instanceFilter, categories);
                        }
                        return 0;
                    }
                });
            } else {
                return 0;
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Exception while listing instances: ",  e);
        }
    }
}
