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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.dao.DeferredProcessInstanceCleanable;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.engine.cron.CronScheduler;
import org.apache.ode.bpel.engine.migration.MigrationHandler;
import org.apache.ode.bpel.evar.ExternalVariableModule;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.BpelEngine;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.DebuggerContext;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.Scheduler.JobInfo;
import org.apache.ode.bpel.iapi.Scheduler.JobProcessorException;
import org.apache.ode.bpel.iapi.Scheduler.MapSerializableRunnable;
import org.apache.ode.bpel.iapi.Scheduler.Synchronizer;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;
import org.apache.ode.utils.xsl.XslTransformHandler;

/**
 * <p>
 * The BPEL server implementation.
 * </p>
 *
 * <p>
 * This implementation is intended to be thread safe. The key concurrency
 * mechanism is a "management" read/write lock that synchronizes all management
 * operations (they require "write" access) and prevents concurrent management
 * operations and processing (processing requires "read" access). Write access
 * to the lock is scoped to the method, while read access is scoped to a
 * transaction.
 * </p>
 *
 * @author Maciej Szefler <mszefler at gmail dot com>
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class BpelServerImpl implements BpelServer, Scheduler.JobProcessor {
    private static final Log __log = LogFactory.getLog(BpelServerImpl.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    /** Maximum age of a process before it is quiesced */
    private static Long __processMaxAge;

    public final static String DEFERRED_PROCESS_INSTANCE_CLEANUP_DISABLED_NAME =
        "org.apache.ode.disable.deferredProcessInstanceCleanup";
    
    private static boolean DEFERRED_PROCESS_INSTANCE_CLEANUP_DISABLED = 
        Boolean.getBoolean(DEFERRED_PROCESS_INSTANCE_CLEANUP_DISABLED_NAME);
    
    /** 
     * Set of processes that are registered with the server. Includes hydrated and dehydrated processes.
     * Guarded by _mngmtLock.writeLock(). 
     */
    private final Set<BpelProcess> _registeredProcesses = new HashSet<BpelProcess>();

    private State _state = State.SHUTDOWN;
    private final Contexts _contexts = new Contexts();
    private Properties _configProperties;
    private DehydrationPolicy _dehydrationPolicy;
    private boolean _hydrationLazy;
    private int _hydrationLazyMinimumSize;
    
    BpelEngineImpl _engine;
    protected BpelDatabase _db;
    
    /**
     * Management lock for synchronizing management operations and preventing
     * processing (transactions) from occuring while management operations are
     * in progress.
     */
    private ReadWriteLock _mngmtLock = new ReentrantReadWriteLock();

    static {
        // TODO Clean this up and factorize engine configuration
        try {
            String processMaxAge = System.getProperty("ode.process.maxage");
            if (processMaxAge != null && processMaxAge.length() > 0) {
                __processMaxAge = Long.valueOf(processMaxAge);
                __log.info("Process definition max age adjusted. Max age = " + __processMaxAge + "ms.");
            }
        } catch (Throwable t) {
            if (__log.isDebugEnabled()) {
                __log.debug("Could not parse ode.process.maxage environment variable.", t);
            } else {
                __log.info("Could not parse ode.process.maxage environment variable; reaping disabled.");
            }
        }
    }

    private enum State {
        SHUTDOWN, INIT, RUNNING
    }

    public BpelServerImpl() {
    }

    public Contexts getContexts() {
        return _contexts;
    }
    
    public void start() {
        _mngmtLock.writeLock().lock();
        try {
            if (!checkState(State.INIT, State.RUNNING)) {
                __log.debug("start() ignored -- already started");
                return;
            }
            __log.debug("BPEL SERVER starting.");

            // Eventually running some migrations before starting
            new MigrationHandler(_contexts).migrate(_registeredProcesses);

            _state = State.RUNNING;
            __log.info(__msgs.msgServerStarted());
            if (_dehydrationPolicy != null) {
                Thread thread = new Thread(new ProcessDefReaper(), "Dehydrator");
                thread.setDaemon(true);
                thread.start();
                
            }
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }
    
    public void registerExternalVariableEngine(ExternalVariableModule eve) {
        _contexts.externalVariableEngines.put(eve.getName(), eve);
    }

    /**
     * Register a global listener to receive {@link BpelEvent}s froom all
     * processes.
     * @param listener
     */
    public void registerBpelEventListener(BpelEventListener listener) {
        // Do not synchronize, eventListeners is copy-on-write array.
        listener.startup(_configProperties);
        _contexts.eventListeners.add(listener);
    }

    /**
     * Unregister a global listener from receive {@link BpelEvent}s from all
     * processes.
     * @param listener
     */
    public void unregisterBpelEventListener(BpelEventListener listener) {
        // Do not synchronize, eventListeners is copy-on-write array.
        try {
            listener.shutdown();
        } catch (Exception e) {
            __log.warn("Stopping BPEL event listener " + listener.getClass().getName() + " failed, nevertheless it has been unregistered.", e);
        } finally {
            _contexts.eventListeners.remove(listener);
        }
    }
    
    private void unregisterBpelEventListeners() {
        for (BpelEventListener l : _contexts.eventListeners) {
            unregisterBpelEventListener(l);
        }
    }

    public void stop() {
        _mngmtLock.writeLock().lock();
        try {
            if (!checkState(State.RUNNING, State.INIT)) {
                __log.debug("stop() ignored -- already stopped");
                return;
            }

            __log.debug("BPEL SERVER STOPPING");

            _contexts.scheduler.stop();
            _engine = null;
            _state = State.INIT;
            __log.info(__msgs.msgServerStopped());
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    public void init() throws BpelEngineException {
        _mngmtLock.writeLock().lock();
        try {
            if (!checkState(State.SHUTDOWN, State.INIT))
                return;

            __log.debug("BPEL SERVER initializing ");

            _db = new BpelDatabase(_contexts.dao, _contexts.scheduler);
            _state = State.INIT;
            
            _engine = createBpelEngineImpl(_contexts);
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    // enable extensibility
    protected BpelEngineImpl createBpelEngineImpl(Contexts contexts) {
        return new BpelEngineImpl(contexts);
    }
    
    public void shutdown() throws BpelEngineException {
        _mngmtLock.writeLock().lock();
        try {
            stop();
            unregisterBpelEventListeners();

            _db = null;
            _engine = null;
            _state = State.SHUTDOWN;
        } finally {
            _mngmtLock.writeLock().unlock();
        }

    }

    public BpelEngine getEngine() {
        boolean registered = false;
        _mngmtLock.readLock().lock();
        try {
            _contexts.scheduler.registerSynchronizer(new Synchronizer() {
                public void afterCompletion(boolean success) {
                    _mngmtLock.readLock().unlock();
                }
                public void beforeCompletion() {
                }
            });
            registered = true;
        } finally {
            // If we failed to register the synchro,then there was an ex/throwable; we need to unlock now.
            if (!registered)
                _mngmtLock.readLock().unlock();
        }
        return _engine;
    }

    public void register(ProcessConf conf) {
        if (conf == null)
            throw new NullPointerException("must specify non-null process configuration.");

        __log.debug("register: " + conf.getProcessId());

        // Ok, IO out of the way, we will mod the server state, so need to get a
        // lock.
        try {
            _mngmtLock.writeLock().lockInterruptibly();
        } catch (InterruptedException ie) {
            __log.debug("register(...) interrupted.", ie);
            throw new BpelEngineException(__msgs.msgOperationInterrupted());
        }

        try {
            // If the process is already active, do nothing.
            if (_engine.isProcessRegistered(conf.getProcessId())) {
                __log.debug("skipping doRegister" + conf.getProcessId() + ") -- process is already registered");
                return;
            }

            __log.debug("Registering process " + conf.getProcessId() + " with server.");

            BpelProcess process = createBpelProcess(conf);
	    process._classLoader = Thread.currentThread().getContextClassLoader();

            _engine.registerProcess(process);
            _registeredProcesses.add(process);
            if (!isLazyHydratable(process)) {
                process.hydrate();
            } else {
                _engine.setProcessSize(process.getPID(), false);
            }

            __log.info(__msgs.msgProcessRegistered(conf.getProcessId()));
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }
    
    private boolean isLazyHydratable(BpelProcess process) {
        if (process.isHydrationLazySet()) {
            return process.isHydrationLazy();
        }
        if (!_hydrationLazy) {
            return false;
        }
        return process.getEstimatedHydratedSize() < _hydrationLazyMinimumSize;
    }

    // enable extensibility
    protected BpelProcess createBpelProcess(ProcessConf conf) {
        return new BpelProcess(conf);
    }
    
    public void unregister(QName pid) throws BpelEngineException {
        if (__log.isTraceEnabled())
            __log.trace("unregister: " + pid);

        try {
            _mngmtLock.writeLock().lockInterruptibly();
        } catch (InterruptedException ie) {
            __log.debug("unregister() interrupted.", ie);
            throw new BpelEngineException(__msgs.msgOperationInterrupted());
        }

        try {
            BpelProcess p = null;
            if (_engine != null) {
                p = _engine.unregisterProcess(pid);
                if (p != null)
                {
                    _registeredProcesses.remove(p);
                    XslTransformHandler.getInstance().clearXSLSheets(p.getProcessType());
                    __log.info(__msgs.msgProcessUnregistered(pid));
                }
            }
        } catch (Exception ex) {
            __log.error(__msgs.msgProcessUnregisterFailed(pid), ex);
            throw new BpelEngineException(ex);
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    /**
     * Register a global message exchange interceptor.
     * @param interceptor message-exchange interceptor
     */
    public void registerMessageExchangeInterceptor(MessageExchangeInterceptor interceptor) {
        // NOTE: do not synchronize, globalInterceptors is copy-on-write.
        _contexts.globalInterceptors.add(interceptor);
    }

    /**
     * Unregister a global message exchange interceptor.
     * @param interceptor message-exchange interceptor
     */
    public void unregisterMessageExchangeInterceptor(MessageExchangeInterceptor interceptor) {
        // NOTE: do not synchronize, globalInterceptors is copy-on-write.
        _contexts.globalInterceptors.remove(interceptor);
    }

    /**
     * Check a state transition from state "i" to state "j".
     */
    private boolean checkState(State i, State j) {
        if (_state == i)
            return true;
        if (_state == j)
            return false;
        return false;
    }

    protected boolean deleteProcessDAO(final QName pid, boolean isInMemory) {
        try {
            if (isInMemory) {
                return deleteProcessDAO(_contexts.inMemDao.getConnection(), pid);
            } else {
                return _db.exec(new BpelDatabase.Callable<Boolean>() {
                    public Boolean run(BpelDAOConnection conn) throws Exception {
                        return deleteProcessDAO(conn, pid);
                    }
                });
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private boolean deleteProcessDAO(BpelDAOConnection conn, QName pid) {
        final ProcessDAO proc = conn.getProcess(pid);
        if (proc != null) {
            // delete routes
            if(__log.isDebugEnabled()) __log.debug("Deleting only the process " + pid + "...");
            proc.deleteProcessAndRoutes();
            if(__log.isInfoEnabled()) __log.info("Deleted only the process " + pid + ".");
             // we do deferred instance cleanup only for hibernate, for now
            if( proc instanceof DeferredProcessInstanceCleanable &&
                !DEFERRED_PROCESS_INSTANCE_CLEANUP_DISABLED ) {
                // schedule deletion of process runtime data
                _engine._contexts.scheduler.scheduleMapSerializableRunnable(
                    new ProcessCleanUpRunnable(((DeferredProcessInstanceCleanable)proc).getId()), new Date());
            } else if( proc instanceof DeferredProcessInstanceCleanable ) {
                ((DeferredProcessInstanceCleanable)proc).deleteInstances(Integer.MAX_VALUE);
            }
            return true;
        }
        return false;
        
    }

    public void onScheduledJob(JobInfo jobInfo) throws JobProcessorException {
        getEngine().onScheduledJob(jobInfo);
    }
    
    private class ProcessDefReaper implements Runnable {
        public void run() {
            __log.debug("Starting process definition reaper thread.");
            long pollingTime = 10000;
            try {
                while (true) {
                    Thread.sleep(pollingTime);
                    if (!_mngmtLock.writeLock().tryLock(100L, TimeUnit.MILLISECONDS)) continue;
                    try { 
                        __log.debug("Kicking reaper, OProcess instances: " + OProcess.instanceCount);
                        // Copying the runnning process list to avoid synchronization
                        // problems and a potential mess if a policy modifies the list
                        List<BpelProcess> candidates = new ArrayList<BpelProcess>(_registeredProcesses);
                        CollectionsX.remove_if(candidates, new MemberOfFunction<BpelProcess>() {
                            public boolean isMember(BpelProcess o) {
                                return !o.hintIsHydrated();
                            }
                            
                        });

                        // And the happy winners are...
                        List<BpelProcess> ripped = _dehydrationPolicy.markForDehydration(candidates);
                        // Bye bye
                        for (BpelProcess process : ripped) {
                            __log.debug("Dehydrating process " + process.getPID());
                            process.dehydrate();
                        }
                    } finally {
                        _mngmtLock.writeLock().unlock();
                    }
                }
            } catch (InterruptedException e) {
                __log.info(e);
            }
        }
    }

    public void setDehydrationPolicy(DehydrationPolicy dehydrationPolicy) {
        _dehydrationPolicy = dehydrationPolicy;
    }

    public void setConfigProperties(Properties configProperties) {
        _configProperties = configProperties;
    }
    
    public void setMessageExchangeContext(MessageExchangeContext mexContext) throws BpelEngineException {
        _contexts.mexContext = mexContext;
    }

    public void setScheduler(Scheduler scheduler) throws BpelEngineException {
        _contexts.scheduler = scheduler;
    }

    public void setCronScheduler(CronScheduler cronScheduler) throws BpelEngineException {
        _contexts.cronScheduler = cronScheduler;
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

    public DebuggerContext getDebugger(QName pid) throws BpelEngineException {
        return _engine._activeProcesses.get(pid)._debugger;
    }

    public boolean hasActiveInstances(final QName pid) {
        try {
            return _db.exec(new BpelDatabase.Callable<Boolean>() {
                public Boolean run(BpelDAOConnection conn) throws Exception {
                    return conn.getNumInstances(pid) > 0;
                }
            });
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setHydrationLazy(boolean hydrationLazy) {
        this._hydrationLazy = hydrationLazy;
    }

    public void setProcessThrottledMaximumSize(
            long hydrationThrottledMaximumSize) {
        _engine.setProcessThrottledMaximumSize(hydrationThrottledMaximumSize);
    }
    
    public void setProcessThrottledMaximumCount(
            int hydrationThrottledMaximumCount) {
        _engine.setProcessThrottledMaximumCount(hydrationThrottledMaximumCount);
    }

    public void setHydrationLazyMinimumSize(int hydrationLazyMinimumSize) {
        this._hydrationLazyMinimumSize = hydrationLazyMinimumSize;
    }

    public void setInstanceThrottledMaximumCount(
            int instanceThrottledMaximumCount) {
        _engine.setInstanceThrottledMaximumCount(instanceThrottledMaximumCount);
    }

    /**
     * A polled runnable instance that implements this interface will be set 
     * with the contexts before the run() method is called.
     * 
     * @author sean
     *
     */
    public interface ContextsAware {
        void setContexts(Contexts contexts);
    }

    /**
     * This wraps up the executor service for polled runnables.
     * 
     * @author sean
     *
     */
    public static class PolledRunnableProcessor implements Scheduler.JobProcessor {
        private ExecutorService _polledRunnableExec;
        private Contexts _contexts;
        
        // this map contains all polled runnable results that are not completed.
        // keep an eye on this one, since if we re-use this polled runnable and
        // generate too many entries in this map, this becomes a memory leak(
        // long-running memory occupation)
        private final Map<String, PolledRunnableResults> resultsByJobId = new HashMap<String, PolledRunnableResults>();

        public void setContexts(Contexts contexts) {
            _contexts = contexts;
        }
        
        public void setPolledRunnableExecutorService(ExecutorService polledRunnableExecutorService) {
            _polledRunnableExec = polledRunnableExecutorService;
        }

        public void onScheduledJob(final Scheduler.JobInfo jobInfo) throws Scheduler.JobProcessorException {
            JOB_STATUS statusOfPriorTry = JOB_STATUS.PENDING;
            Exception exceptionThrownOnPriorTry = null;
            boolean toRetry = false;
            
            synchronized( resultsByJobId ) {
                PolledRunnableResults results = resultsByJobId.get(jobInfo.jobName);        
                if( results != null ) {
                    statusOfPriorTry = results._status;
                    exceptionThrownOnPriorTry = results._exception;
                }
                if( statusOfPriorTry == JOB_STATUS.COMPLETED ) {
                    resultsByJobId.remove(jobInfo.jobName);
                    jobInfo.jobDetail.put("runnable_status", JOB_STATUS.COMPLETED);
                    return;
                }
                if( statusOfPriorTry == JOB_STATUS.PENDING || statusOfPriorTry == JOB_STATUS.FAILED ) {
                    resultsByJobId.put(jobInfo.jobName, new PolledRunnableResults(JOB_STATUS.IN_PROGRESS, null));
                    toRetry = true;
                }
            }
            
            if( toRetry ) {
                // re-try
                _polledRunnableExec.submit(new Runnable() {
                    public void run() {
                        try {
                            MapSerializableRunnable runnable = (MapSerializableRunnable)jobInfo.jobDetail.get("runnable");
                            runnable.restoreFromDetailsMap(jobInfo.jobDetail);
                            if( runnable instanceof ContextsAware ) {
                                ((ContextsAware)runnable).setContexts(_contexts);
                            }
                            runnable.run();
                            synchronized( resultsByJobId ) {
                                resultsByJobId.put(jobInfo.jobName, new PolledRunnableResults(JOB_STATUS.COMPLETED, null));
                            }
                        } catch( Exception e) {
                            __log.error("", e);
                            synchronized( resultsByJobId ) {
                                resultsByJobId.put(jobInfo.jobName, new PolledRunnableResults(JOB_STATUS.FAILED, e));
                            }
                        } finally {
                        }
                    }
                });
            }
            
            jobInfo.jobDetail.put("runnable_status", JOB_STATUS.IN_PROGRESS);
            if( exceptionThrownOnPriorTry != null ) {
                throw new Scheduler.JobProcessorException(exceptionThrownOnPriorTry, true);
            }
        }
        
        private static enum JOB_STATUS {
            PENDING, IN_PROGRESS, FAILED, COMPLETED
        }
        
        private class PolledRunnableResults {
            private JOB_STATUS _status = JOB_STATUS.PENDING;
            private Exception _exception;
            
            public PolledRunnableResults(JOB_STATUS status, Exception exception) {
                _status = status;
                _exception = exception;
            }
        }
    }

    public void cleanupProcess(ProcessConf pconf) throws BpelEngineException {
        if (pconf != null) {
            deleteProcessDAO(pconf.getProcessId(), pconf.isTransient());
        }
    }
}
