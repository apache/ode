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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.BpelEngine;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.Scheduler.JobInfo;
import org.apache.ode.bpel.iapi.Scheduler.JobProcessorException;
import org.apache.ode.bpel.iapi.Scheduler.Synchronizer;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

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

    /** 
     * Set of processes that are registered with the server. Includes hydrated and dehydrated processes.
     * Guarded by _mngmtLock.writeLock(). 
     */
    private final Set<BpelProcess> _registeredProcesses = new HashSet<BpelProcess>();

    private State _state = State.SHUTDOWN;
    private Contexts _contexts = new Contexts();
    private DehydrationPolicy _dehydrationPolicy;
    private Properties _configProperties;
    
    BpelEngineImpl _engine;
    BpelDatabase _db;

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
    
    public void start() {
        _mngmtLock.writeLock().lock();
        try {
            if (!checkState(State.INIT, State.RUNNING)) {
                __log.debug("start() ignored -- already started");
                return;
            }

            __log.debug("BPEL SERVER starting.");

            _contexts.scheduler.start();
            _state = State.RUNNING;
            __log.info(__msgs.msgServerStarted());
            if (_dehydrationPolicy != null) new Thread(new ProcessDefReaper()).start();
        } finally {
            _mngmtLock.writeLock().unlock();
        }
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
            
            _engine = new BpelEngineImpl(_contexts);

        } finally {
            _mngmtLock.writeLock().unlock();
        }
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

            BpelProcess process = new BpelProcess(conf, null);

            _engine.registerProcess(process);
            _registeredProcesses.add(process);
            process.hydrate();

            __log.info(__msgs.msgProcessRegistered(conf.getProcessId()));
        } finally {
            _mngmtLock.writeLock().unlock();
        }
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
                _engine.unregisterProcess(pid);
                _registeredProcesses.remove(p);
            }

            __log.info(__msgs.msgProcessUnregistered(pid));

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
        _contexts.globalIntereceptors.add(interceptor);
    }

    /**
     * Unregister a global message exchange interceptor.
     * @param interceptor message-exchange interceptor
     */
    public void unregisterMessageExchangeInterceptor(MessageExchangeInterceptor interceptor) {
        // NOTE: do not synchronize, globalInterceptors is copy-on-write.
        _contexts.globalIntereceptors.remove(interceptor);
    }

    /**
     * Check a state transition from state "i" to state "j".
     */
    private final boolean checkState(State i, State j) {
        if (_state == i)
            return true;
        if (_state == j)
            return false;
        throw new IllegalStateException("Unexpected state: " + i);
    }

    /* TODO: We need to have a method of cleaning up old deployment data. */
    private boolean deleteProcessDAO(final QName pid) {
        try {
            // Delete it from the database.
            return _db.exec(new BpelDatabase.Callable<Boolean>() {
                public Boolean run(BpelDAOConnection conn) throws Exception {
                    ProcessDAO proc = conn.getProcess(pid);
                    if (proc != null) {
                        proc.delete();
                        return true;
                    }
                    return false;
                }
            });
        } catch (Exception ex) {
            String errmsg = "DbError";
            __log.error(errmsg, ex);
            throw new BpelEngineException(errmsg, ex);
        }
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
                    _mngmtLock.writeLock().lockInterruptibly();
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

}