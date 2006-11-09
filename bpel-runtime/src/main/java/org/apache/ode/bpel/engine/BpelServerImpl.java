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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.o.OExpressionLanguage;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.apache.ode.bpel.runtime.ExpressionLanguageRuntimeRegistry;
import org.apache.ode.utils.msg.MessageBundle;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The BPEL server implementation. This implementation is intended to be thread
 * safe. The key concurrency mechanism is a "management" read/write lock that
 * synchronizes all management operations (they require "write" access) and
 * prevents concurrent management operations and processing (processing requires
 * "read" access). Write access to the lock is scoped to the method, while read
 * access is scoped to a transaction.
 * @author mriou <mriou at apache dot org>
 */
public class BpelServerImpl implements BpelServer {

    private static final Log __log = LogFactory.getLog(BpelServer.class);
    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    /**
     * Management lock for synchronizing management operations and preventing
     * processing (transactions) from occuring while management operations are
     * in progress.
     */
    private ReadWriteLock _mngmtLock = new ReentrantReadWriteLock();
    private Contexts _contexts = new Contexts();
    BpelEngineImpl _engine;
    private boolean _started;
    private boolean _initialized;
    private BpelDatabase _db;

    public BpelServerImpl() {
    }

    public void start() {
        _mngmtLock.writeLock().lock();
        try {
            if (!_initialized) {
                String err = "start() called before init()!";
                __log.fatal(err);
                throw new IllegalStateException(err);
            }

            if (_started) {
                if (__log.isDebugEnabled()) __log.debug("start() ignored -- already started");
                return;
            }

            if (__log.isDebugEnabled()) __log.debug("BPEL SERVER starting.");

            _engine = new BpelEngineImpl(_contexts);
            Map<QName, byte[]> pids = _contexts.store.getActiveProcesses();
            for (Map.Entry<QName, byte[]> pid : pids.entrySet())
                try {
                    doActivateProcess(pid.getKey(), pid.getValue());
                } catch (Exception ex) {
                    String msg = __msgs.msgProcessActivationError(pid.getKey());
                    __log.error(msg, ex);
                }
            // readState();
            _contexts.scheduler.start();
            _started = true;
            __log.info(__msgs.msgServerStarted());
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    /**
     * Register a global listener to receive {@link BpelEvent}s froom all
     * processes.
     * 
     * @param listener
     */
    public void registerBpelEventListener(BpelEventListener listener) {
        // Do not synchronize, eventListeners is copy-on-write array.
        _contexts.eventListeners.add(listener);
    }

    /**
     * Unregister a global listener from receive {@link BpelEvent}s from all
     * processes.
     * 
     * @param listener
     */
    public void unregisterBpelEventListener(BpelEventListener listener) {
        // Do not synchronize, eventListeners is copy-on-write array.
        _contexts.eventListeners.remove(listener);
    }

    public void stop() {
        _mngmtLock.writeLock().lock();
        try {
            if (!_started) {
                if (__log.isDebugEnabled())
                    __log.debug("stop() ignored -- already stopped");
                return;
            }
            if (__log.isDebugEnabled()) {
                __log.debug("BPEL SERVER STOPPING");
            }
            _contexts.scheduler.stop();
            _engine = null;
            _started = false;

            __log.info(__msgs.msgServerStopped());
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    /**
     * Load the parsed and compiled BPEL process definition from the database.
     * 
     * @param processId
     *            process identifier
     * @return process information from configuration database
     */
    private OProcess loadProcess(QName processId, byte[] serProc) {
        if (__log.isTraceEnabled()) {
            __log.trace("loadProcess: " + processId);
        }
        assert _initialized : "loadProcess() called before init()!";

        InputStream is = new ByteArrayInputStream(serProc);
        OProcess compiledProcess;
        try {
            Serializer ofh = new Serializer(is);
            compiledProcess = ofh.readOProcess();
        } catch (Exception e) {
            String errmsg = __msgs.msgProcessLoadError(processId);
            __log.error(errmsg, e);
            throw new BpelEngineException(errmsg, e);
        }

        return compiledProcess;
    }

    public BpelManagementFacade getBpelManagementFacade() {
        return new BpelManagementFacadeImpl(_db, _engine, this, _contexts.store);
    }

    public void setMessageExchangeContext(MessageExchangeContext mexContext) throws BpelEngineException {
        _contexts.mexContext = mexContext;
    }

    public void setScheduler(Scheduler scheduler) throws BpelEngineException {
        _contexts.scheduler = scheduler;
    }

    public void setEndpointReferenceContext(EndpointReferenceContext eprContext) throws BpelEngineException {
        _contexts.eprContext = eprContext;
    }

    /**
     * Set the DAO connection factory. The DAO is used by the BPEL engine to
     * persist information about active processes.
     * 
     * @param daoCF
     *            {@link BpelDAOConnectionFactory} implementation.
     */
    public void setDaoConnectionFactory(BpelDAOConnectionFactory daoCF) throws BpelEngineException {
        _contexts.dao = daoCF;
    }

    public void setInMemDaoConnectionFactory(BpelDAOConnectionFactory daoCF) {
        _contexts.inMemDao = daoCF;
    }

    public void setBindingContext(BindingContext bc) {
        _contexts.bindingContext = bc;
    }

    public void init() throws BpelEngineException {
        _mngmtLock.writeLock().lock();
        try {
            if (_initialized)
                throw new IllegalStateException("init() called twice.");

            if (__log.isDebugEnabled()) {
                __log.debug("BPEL SERVER initializing ");
            }

            _db = new BpelDatabase(_contexts.dao, _contexts.scheduler);
            _initialized = true;
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    public void shutdown() throws BpelEngineException {
        _mngmtLock.writeLock().lock();
        try {
            if (!_initialized)
                return;

            stop();
        } finally {
            _mngmtLock.writeLock().unlock();
        }

    }

    public BpelEngine getEngine() {
        // Acquire a readlock for the current thread / transaction and then
        // return
        // an engine instance.

        // First check if this thread has already requested the engine for this
        // transaction, if not, acquire the lock.
        // if (!_associated.get()) {
        // if (!_started) {
        // String errmsg = "call on getEngine() on server that has not been
        // started!";
        // __log.debug(errmsg);
        // throw new IllegalStateException(errmsg);
        // }
        //
        // // We need to schedule a task to release the lock.
        // // _contexts.scheduler.scheduleTransactionCallback();
        // _associated.set(Boolean.TRUE);
        // }

        return _engine;
    }

    public void load(final QName pid, boolean sticky) {
        if (__log.isTraceEnabled())
            __log.trace("load: " + pid);

        try {
            _mngmtLock.writeLock().lockInterruptibly();
        } catch (InterruptedException ie) {
            __log.debug("load() interrupted.", ie);
            throw new BpelEngineException(__msgs.msgOperationInterrupted());
        }
        try {
            if (sticky) _contexts.store.markActive(pid, true);
            byte[] serProc = _contexts.store.getActiveProcesses().get(pid);
            doActivateProcess(pid, serProc);
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    public void unload(QName pid, boolean sticky) throws BpelEngineException {
        if (__log.isTraceEnabled())
            __log.trace("unload " + pid);

        try {
            _mngmtLock.writeLock().lockInterruptibly();
        } catch (InterruptedException ie) {
            __log.debug("unload() interrupted.", ie);
            throw new BpelEngineException(__msgs.msgOperationInterrupted());
        }

        try {
            if (sticky) _contexts.store.markActive(pid, false);
            unregisterProcess(pid);
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    /**
     * Activate the process in the engine.
     * 
     * @param pid
     */
    private void doActivateProcess(final QName pid, byte[] serProcess) {
        _mngmtLock.writeLock().lock();
        try {
            // Load the compiled process.
            OProcess compiledProcess = loadProcess(pid, serProcess);
            // Check that process exist, otherwise creates it (lazy creation)
            checkProcessExistence(pid, compiledProcess);
            
            // If the process is already active, do nothing.
            if (_engine.isProcessRegistered(pid)) {
                __log.debug("skipping doActivateProcess(" + pid + ") -- process is already active");
                return;
            }

            if (__log.isDebugEnabled())
                __log.debug("Process " + pid + " is not active, creating new entry.");

            // Create an expression language registry for this process
            ExpressionLanguageRuntimeRegistry elangRegistry = new ExpressionLanguageRuntimeRegistry();
            for (OExpressionLanguage elang : compiledProcess.expressionLanguages) {
                try {
                    elangRegistry.registerRuntime(elang);
                } catch (ConfigurationException e) {
                    String msg = "Expression language registration error.";
                    __log.error(msg, e);
                    throw new BpelEngineException(msg, e);
                }
            }

            // Create local message-exchange interceptors.
            List<MessageExchangeInterceptor> localMexInterceptors = new LinkedList<MessageExchangeInterceptor>();
            for (String mexclass : _contexts.store.getMexInterceptors(pid)) {
                try {
                    Class cls = Class.forName(mexclass);
                    localMexInterceptors.add((MessageExchangeInterceptor) cls.newInstance());
                } catch (Throwable t) {
                    String errmsg = "Error instantiating message-exchange interceptor " + mexclass;
                    __log.error(errmsg, t);
                }
            }

            // Create myRole endpoint name mapping (from deployment descriptor)
            HashMap<OPartnerLink, Endpoint> myRoleEndpoints = new HashMap<OPartnerLink, Endpoint>();
            for (Map.Entry<String, Endpoint> provide : _contexts.store.getProvideEndpoints(pid).entrySet()) {
                OPartnerLink plink = compiledProcess.getPartnerLink(provide.getKey());
                if (plink == null) {
                    String errmsg = "Error in deployment descriptor for process " + pid
                            + "; reference to unknown partner link " + provide.getKey();
                    __log.error(errmsg);
                    throw new BpelEngineException(errmsg);
                }
                myRoleEndpoints.put(plink, provide.getValue());
            }

            // Create partnerRole initial value mapping
            HashMap<OPartnerLink, Endpoint> partnerRoleIntialValues = new HashMap<OPartnerLink, Endpoint>();
            for (Map.Entry<String, Endpoint> invoke : _contexts.store.getInvokeEndpoints(pid).entrySet()) {
                OPartnerLink plink = compiledProcess.getPartnerLink(invoke.getKey());
                if (plink == null) {
                    String errmsg = "Error in deployment descriptor for process " + pid
                            + "; reference to unknown partner link " + invoke.getKey();
                    __log.error(errmsg);
                    throw new BpelEngineException(errmsg);
                }
                __log.debug("Processing <invoke> element for process " + pid + ": partnerlink " +
                        invoke.getKey() + " --> " + invoke.getValue());

                partnerRoleIntialValues.put(plink, invoke.getValue());
            }

            BpelProcess process = new BpelProcess(pid, compiledProcess, myRoleEndpoints, partnerRoleIntialValues,
                    null, elangRegistry, localMexInterceptors, _contexts.store);

            _engine.registerProcess(process);

            __log.info(__msgs.msgProcessActivated(pid));
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    private boolean checkProcessExistence(final QName pid, final OProcess oprocess) {
        try {
            boolean existed = true;
            if (_contexts.store.getProcessConfiguration(pid).isInMemory()) {
                existed = false;
                ProcessDAO newDao = _contexts.inMemDao.getConnection().createProcess(pid, oprocess.getQName());
                for (String correlator : oprocess.getCorrelators()) {
                    newDao.addCorrelator(correlator);
                }
            } else {
                existed = _db.exec(new BpelDatabase.Callable<Boolean>() {
                    public Boolean run(BpelDAOConnection conn) throws Exception {
                        // Hack, but at least for now we need to ensure that we
                        // are
                        // the only process with this process id.
                        ProcessDAO old = conn.getProcess(pid);
                        if (old != null) return true;

                        ProcessDAO newDao = conn.createProcess(pid, oprocess.getQName());
                        for (String correlator : oprocess.getCorrelators()) {
                            newDao.addCorrelator(correlator);
                        }
                        return false;
                    }
                });
            }
            if (__log.isDebugEnabled()) {
                if (existed) __log.debug("Process runtime already exist (" + pid + "), no need to create.");
                else __log.debug("Created new process runtime " + pid);
            }
            return existed;
        } catch (BpelEngineException ex) {
            throw ex;
        } catch (Exception dce) {
            __log.error("", dce);
            throw new BpelEngineException("", dce);
        }
    }

    private boolean unregisterProcess(final QName pid) {
        try {
            if (_engine != null)
                _engine.unregisterProcess(pid);

            if (__log.isDebugEnabled())
                __log.debug("Unregistering process " + pid);

            // Delete it from the database.
            boolean found = _db.exec(new BpelDatabase.Callable<Boolean>() {
                public Boolean run(BpelDAOConnection conn) throws Exception {
                    ProcessDAO proc = conn.getProcess(pid);
                    if (proc != null) {
                        proc.delete();
                        return true;
                    }
                    return false;
                }
            });

            if (found) {
                __log.info(__msgs.msgProcessUnregistered(pid));
                return true;
            }
            return false;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            __log.error(__msgs.msgProcessUnregisterFailed(pid), ex);
            throw new BpelEngineException(ex);
        }
    }

    /**
     * Register a global message exchange interceptor.
     * 
     * @param interceptor
     *            message-exchange interceptor
     */
    public void registerMessageExchangeInterceptor(MessageExchangeInterceptor interceptor) {
        // NOTE: do not synchronize, globalInterceptors is copy-on-write.
        _contexts.globalIntereceptors.add(interceptor);
    }

    /**
     * Unregister a global message exchange interceptor.
     * 
     * @param interceptor
     *            message-exchange interceptor
     */
    public void unregisterMessageExchangeInterceptor(MessageExchangeInterceptor interceptor) {
        // NOTE: do not synchronize, globalInterceptors is copy-on-write.
        _contexts.globalIntereceptors.remove(interceptor);
    }


    /**
     * Inject a ProcessStore implementation.
     * @param store a ProcessStore instance
     */
    public void setProcessStore(ProcessStore store) {
        _contexts.store = store;
    }

}