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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import org.apache.ode.bpel.iapi.Scheduler.JobInfo;
import org.apache.ode.bpel.iapi.Scheduler.JobProcessorException;
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
 * This implementation is intended to be thread safe. The key concurrency mechanism is a "management" read/write lock that
 * synchronizes all management operations (they require "write" access) and prevents concurrent management operations and processing
 * (processing requires "read" access). Write access to the lock is scoped to the method, while read access is scoped to a
 * transaction.
 * </p>
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class BpelServerImpl implements BpelServer, Scheduler.JobProcessor {

    private static final Log __log = LogFactory.getLog(BpelServerImpl.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private final List<WeakReference<MessageExchangeStateListener>> _mexStateListeners = new ArrayList<WeakReference<MessageExchangeStateListener>>();

    /** Maximum age of a process before it is quiesced */
    private static Long __processMaxAge;

    /** RNG, for delays */
    private Random _random = new Random(System.currentTimeMillis());

    private static double _delayMean = 0;

    /**
     * Set of processes that are registered with the server. Includes hydrated and dehydrated processes. Guarded by
     * _mngmtLock.writeLock().
     */
    private final HashMap<QName, BpelProcess> _registeredProcesses = new HashMap<QName, BpelProcess>();

    /** Mapping from myrole service name to active process. */
    private final HashMap<QName, BpelProcess> _serviceMap = new HashMap<QName, BpelProcess>();

    private State _state = State.SHUTDOWN;

    Contexts _contexts = new Contexts();

    private DehydrationPolicy _dehydrationPolicy;

    private Properties _configProperties;

    private ExecutorService _exec;

    BpelDatabase _db;

    /**
     * Management lock for synchronizing management operations and preventing processing (transactions) from occuring while
     * management operations are in progress.
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


            if (_exec == null)
                _exec = Executors.newCachedThreadPool();
            
            _contexts.scheduler.start();
            _state = State.RUNNING;
            __log.info(__msgs.msgServerStarted());
            if (_dehydrationPolicy != null)
                new Thread(new ProcessDefReaper()).start();
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    /**
     * Register a global listener to receive {@link BpelEvent}s froom all processes.
     * 
     * @param listener
     */
    public void registerBpelEventListener(BpelEventListener listener) {
        // Do not synchronize, eventListeners is copy-on-write array.
        listener.startup(_configProperties);
        _contexts.eventListeners.add(listener);
    }

    /**
     * Unregister a global listener from receive {@link BpelEvent}s from all processes.
     * 
     * @param listener
     */
    public void unregisterBpelEventListener(BpelEventListener listener) {
        // Do not synchronize, eventListeners is copy-on-write array.
        try {
            listener.shutdown();
        } catch (Exception e) {
            __log.warn("Stopping BPEL event listener " + listener.getClass().getName()
                    + " failed, nevertheless it has been unregistered.", e);
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

            _db = new BpelDatabase(_contexts);
            _state = State.INIT;

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
            _state = State.SHUTDOWN;
        } finally {
            _mngmtLock.writeLock().unlock();
        }

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
            if (_registeredProcesses.containsKey(conf.getProcessId())) {
                __log.debug("skipping doRegister" + conf.getProcessId() + ") -- process is already registered");
                return;
            }

            __log.debug("Registering process " + conf.getProcessId() + " with server.");

            BpelProcess process = new BpelProcess(this, conf, null);

            for (Endpoint e : process.getServiceNames()) {
                __log.debug("Register process: serviceId=" + e + ", process=" + process);
                _serviceMap.put(e.serviceName, process);
            }

            process.activate(_contexts);

            _registeredProcesses.put(process.getPID(), process);
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
            BpelProcess p = _registeredProcesses.remove(pid);
            p.deactivate();
            while (_serviceMap.values().remove(p))
                ;

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
     * Route to a process using the service id. Note, that we do not need the endpoint name here, we are assuming that two processes
     * would not be registered under the same service qname but different endpoint.
     * 
     * @param service
     *            target service id
     * @param request
     *            request message
     * @return process corresponding to the targetted service, or <code>null</code> if service identifier is not recognized.
     */
    BpelProcess route(QName service, Message request) {
        // TODO: use the message to route to the correct service if more than
        // one service is listening on the same endpoint.

        _mngmtLock.readLock().lock();
        try {
            return _serviceMap.get(service);
        } finally {
            _mngmtLock.readLock().unlock();
        }
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

    public void onScheduledJob(final JobInfo jobInfo) throws JobProcessorException {
        _mngmtLock.readLock().lock();
        try {
            final WorkEvent we = new WorkEvent(jobInfo.jobDetail);
            BpelProcess process = _registeredProcesses.get(we.getProcessId());
            if (process == null) {
                // If the process is not active, it means that we should not be
                // doing any work on its behalf, therefore we will reschedule the
                // events for some time in the future (1 minute).
                _contexts.execTransaction(new Callable<Void>() {

                    public Void call() throws Exception {
                        _contexts.scheduler.jobCompleted(jobInfo.jobName);
                        Date future = new Date(System.currentTimeMillis() + (60 * 1000));
                        __log.info(__msgs.msgReschedulingJobForInactiveProcess(we.getProcessId(), jobInfo.jobName, future));
                        _contexts.scheduler.schedulePersistedJob(we.getDetail(), future);            
                        return null;
                    }
                    
                });
                return;
            }

            process.handleWorkEvent(jobInfo);
        } catch (Exception ex) {
            throw new JobProcessorException(ex, true);
        } finally {
            _mngmtLock.readLock().unlock();
        }
    }

    public void setTransactionManager(TransactionManager txm) {
        _contexts.txManager = txm;
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
     * Set the DAO connection factory. The DAO is used by the BPEL engine to persist information about active processes.
     * 
     * @param daoCF
     *            {@link BpelDAOConnectionFactory} implementation.
     */
    public void setDaoConnectionFactory(BpelDAOConnectionFactory daoCF) throws BpelEngineException {
        _contexts.dao = daoCF;
    }

    public void setBindingContext(BindingContext bc) {
        _contexts.bindingContext = bc;
    }

    public MyRoleMessageExchange createMessageExchange(final InvocationStyle istyle, final QName targetService,
            final String operation, final String clientKey) throws BpelEngineException {

        _mngmtLock.readLock().lock();
        try {
            final BpelProcess target = route(targetService, null);

            if (target == null)
                throw new BpelEngineException("NoSuchService: " + targetService);

            if (istyle == InvocationStyle.RELIABLE || istyle == InvocationStyle.TRANSACTED)
                assertTransaction();
            else
                assertNoTransaction();
            
            
            return target.createNewMyRoleMex(istyle, targetService, operation, clientKey);
        } finally {
            _mngmtLock.readLock().unlock();
        }
    }
    
    public MessageExchange getMessageExchange(final String mexId) throws BpelEngineException {

        _mngmtLock.readLock().lock();
        try {
            final MessageExchangeDAO inmemdao = getInMemMexDAO(mexId);

            Callable<MessageExchange> loadMex = new Callable<MessageExchange>() {

                public MessageExchange call() {
                    MessageExchangeDAO mexdao = (inmemdao == null) ? mexdao = _contexts.dao.getConnection().getMessageExchange(
                            mexId) : inmemdao;
                    if (mexdao == null)
                        return null;

                    ProcessDAO pdao = mexdao.getProcess();
                    BpelProcess process = pdao == null ? null : _registeredProcesses.get(pdao.getProcessId());

                    if (process == null) {
                        String errmsg = __msgs.msgProcessNotActive(pdao.getProcessId());
                        __log.error(errmsg);
                        // TODO: Perhaps we should define a checked exception for this
                        // condition.
                        throw new BpelEngineException(errmsg);
                    }

                    InvocationStyle istyle = InvocationStyle.valueOf(mexdao.getInvocationStyle());
                    if (istyle == InvocationStyle.RELIABLE || istyle == InvocationStyle.TRANSACTED)
                        assertTransaction();

                    switch (mexdao.getDirection()) {
                    case MessageExchangeDAO.DIR_BPEL_INVOKES_PARTNERROLE:
                        return process.createPartnerRoleMex(mexdao);
                    case MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE:
                        return process.createMyRoleMex(mexdao);
                    default:
                        String errmsg = "BpelEngineImpl: internal error, invalid MexDAO direction: " + mexId;
                        __log.fatal(errmsg);
                        throw new BpelEngineException(errmsg);
                    }
                }
            };

            try {
                if (inmemdao != null || _contexts.isTransacted()) // TODO: hmmmmm, catch-22, need to be able to infer if TRANSACTED/RELIABLE just from mex id ? here || istyle == InvocationStyle.RELIABLE || istyle == InvocationStyle.TRANSACTED)
                    return loadMex.call();
                else 
                    return enqueueTransaction(loadMex).get();
            } catch (ContextException e) {
                throw new BpelEngineException(e);
            } catch (Exception e) {
                throw new BpelEngineException(e);
            }

        } finally {
            _mngmtLock.readLock().unlock();
        }

    }

    public MessageExchange getMessageExchangeByForeignKey(String foreignKey) throws BpelEngineException {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<InvocationStyle> getSupportedInvocationStyle(QName serviceId) {

        _mngmtLock.readLock().lock();
        try {
            BpelProcess process = _serviceMap.get(serviceId);
            if (process == null)
                throw new BpelEngineException("No such service: " + serviceId);
            return process.getSupportedInvocationStyle(serviceId);
        } finally {
            _mngmtLock.readLock().unlock();
        }
    }

    MessageExchangeDAO getInMemMexDAO(String mexId) {
        _mngmtLock.readLock().lock();
        try {
          for (BpelProcess p : _registeredProcesses.values()) {
              MessageExchangeDAO mexDao = p.getInMemMexDAO(mexId);
              if (mexDao != null)
                  return mexDao;
          }
        } finally {
            _mngmtLock.readLock().unlock();
        }
        
        return null;
    }
    
    void registerMessageExchangeStateListener(MessageExchangeStateListener mexStateListener) {
        WeakReference<MessageExchangeStateListener> ref = new WeakReference<MessageExchangeStateListener>(mexStateListener);

    }

    OProcess getOProcess(QName processId) {
        _mngmtLock.readLock().lock();
        try {
            BpelProcess process = _registeredProcesses.get(processId);

            if (process == null)
                return null;

            return process.getOProcess();

        } finally {
            _mngmtLock.readLock().unlock();
        }
    }


    <T> Future<T> enqueueTransaction(final Callable<T> transaction) throws ContextException {
        return _exec.submit(new ServerCallable<T>(new TransactedCallable<T>(transaction)));
    }

    /**
     * Schedule a {@link Runnable} object for execution after the completion of the current transaction. 
     * @param runnable
     */
    void scheduleRunnable(Runnable runnable) {
        assertTransaction();
        _contexts.registerCommitSynchronizer(new ServerRunnable(runnable));
    }
    
    protected void assertTransaction() {
        if (!_contexts.isTransacted())
            throw new BpelEngineException("Operation must be performed in a transaction!");
    }

    protected void assertNoTransaction() {
        if (_contexts.isTransacted())
            throw new BpelEngineException("Operation must be performed outside of a transaction!");
    }

    void fireEvent(BpelEvent event) {
        // Note that the eventListeners list is a copy-on-write array, so need
        // to mess with synchronization.
        for (org.apache.ode.bpel.iapi.BpelEventListener l : _contexts.eventListeners) {
            l.onEvent(event);
        }
    }

    /**
     * Block the thread for random amount of time. Used for testing for races and the like. The delay generated is exponentially
     * distributed with the mean obtained from the <code>ODE_DEBUG_TX_DELAY</code> environment variable.
     */
    private void debuggingDelay() {
        // Do a delay for debugging purposes.
        if (_delayMean != 0)
            try {
                long delay = randomExp(_delayMean);
                // distribution
                // with mean
                // _delayMean
                __log.warn("Debugging delay has been activated; delaying transaction for " + delay + "ms.");
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                ; // ignore
            }
    }

    private long randomExp(double mean) {
        double u = _random.nextDouble(); // Uniform
        long delay = (long) (-Math.log(u) * mean); // Exponential
        return delay;
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
                        // Copying the runnning process list to avoid synchronizatMessageExchangeInterion
                        // problems and a potential mess if a policy modifies the list
                        List<BpelProcess> candidates = new ArrayList<BpelProcess>(_registeredProcesses.values());
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

    public BpelProcess getBpelProcess(QName processId) {
        _mngmtLock.readLock().lock();
        try {
            return _registeredProcesses.get(processId);
        } finally {
            _mngmtLock.readLock().unlock();
        }
    }

    
   
    
    class ServerRunnable implements Runnable {
        final Runnable _work;
        ServerRunnable(Runnable work) {
            _work = work;
        }
        
        public void run() {
            _mngmtLock.readLock().lock();
            try {
                _work.run();
            } catch (Throwable ex) {
                __log.fatal("Internal Error", ex);
            } finally {
                _mngmtLock.readLock().unlock();
            }
        }
        
    }
    
   
    
    class ServerCallable<T> implements Callable<T>{
        final Callable<T> _work;
        ServerCallable(Callable<T> work) {
            _work = work;
        }
        
        public T call () throws Exception {
            _mngmtLock.readLock().lock();
            try {
                return _work.call();
            } finally {
                _mngmtLock.readLock().unlock();
            }
        }
        
    }

    class TransactedCallable<T> implements Callable<T> {
        Callable<T> _work;

        TransactedCallable(Callable<T> work) {
            _work = work;
        }

        public T call() throws Exception {
            return _contexts.execTransaction(_work);
        }
    }


    class TransactedRunnable implements Runnable {
        Runnable _work;

        TransactedRunnable(Runnable work) {
            _work = work;
        }

        public void run() {
            _contexts.execTransaction(_work);
        }
    }
}