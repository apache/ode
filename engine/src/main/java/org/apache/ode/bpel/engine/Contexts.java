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

package org.apache.ode.bpel.engine;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.evar.ExternalVariableModule;
import org.apache.ode.bpel.extension.ExtensionBundleRuntime;

/**
 * Aggregation of all the contexts provided to the BPEL engine by the integration layer.
 */
class Contexts {
    private static final Log __log = LogFactory.getLog(Contexts.class);
    
    TransactionManager txManager;
    MessageExchangeContext mexContext;
    Scheduler scheduler;
    EndpointReferenceContext eprContext;
    BindingContext bindingContext;
    BpelDAOConnectionFactory dao;

    /** Global Message-Exchange interceptors. Must be copy-on-write!!! */
    final List<MessageExchangeInterceptor> globalIntereceptors = new CopyOnWriteArrayList<MessageExchangeInterceptor>();

    /** Global event listeners. Must be copy-on-write!!! */
    final List<BpelEventListener> eventListeners = new CopyOnWriteArrayList<BpelEventListener>();

    /** Global extension bundle registry **/
    final Map<String, ExtensionBundleRuntime> extensionRegistry = new ConcurrentHashMap<String, ExtensionBundleRuntime>();
    
    /** Mapping from external variable engine identifier to the engine implementation. */
    final HashMap<QName, ExternalVariableModule> externalVariableEngines = new HashMap<QName, ExternalVariableModule>();

    public boolean isTransacted() {
        try {
            return txManager.getStatus() == Status.STATUS_ACTIVE;
        } catch (SystemException e) {
            throw new BpelEngineException(e);
        }
    }

    public void execTransaction(final Runnable transaction) {
        try {
            execTransaction(new Callable<Void>() {

                public Void call() throws Exception {
                    transaction.run();
                    return null;
                }

            });
        } catch (Exception e) {
            throw new BpelEngineException(e);
        }

    }

    public <T> T execTransaction(Callable<T> transaction) throws Exception{
        try {
            txManager.begin();
        } catch (Exception ex) {
            String errmsg = "Internal Error, could not begin transaction.";
            throw new BpelEngineException(errmsg, ex);
        }
        boolean success = false;
        try {
            T retval = transaction.call();
            success = (txManager.getStatus() != Status.STATUS_MARKED_ROLLBACK);
            return retval;
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (success)
                try {
                    txManager.commit();
                } catch (Exception ex) {
                    __log.error("Commit failed.", ex);                    
                    throw new BpelEngineException("Commit failed.", ex);
                }
            else
                try {
                    txManager.rollback();
                } catch (Exception ex) {
                    __log.error("Transaction rollback failed.", ex);
                }
        }
    }
    
    public void setRollbackOnly() {
        try {
            txManager.setRollbackOnly();
        } catch (SystemException se) {
            __log.error("Transaction set rollback only failed.", se);
        }
    }

    public void registerCommitSynchronizer(final Runnable runnable) {
        try {
            txManager.getTransaction().registerSynchronization(new Synchronization() {

                public void afterCompletion(int status) {
                    if (status == Status.STATUS_COMMITTED)
                        runnable.run();
                }

                public void beforeCompletion() {

                }
                
            });
        } catch (Exception ex) {
            throw new BpelEngineException("Error registering synchronizer." ,ex);
        }
    }

}
