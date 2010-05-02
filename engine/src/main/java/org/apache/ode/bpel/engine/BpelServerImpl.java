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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.context.ContextInterceptor;
import org.apache.ode.dao.bpel.*;
import org.apache.ode.bpel.engine.cron.CronScheduler;
import org.apache.ode.bpel.evar.ExternalVariableModule;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.extension.ExtensionBundleRuntime;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.bpel.iapi.Scheduler.JobInfo;
import org.apache.ode.bpel.iapi.Scheduler.JobProcessorException;
import org.apache.ode.bpel.iapi.Scheduler.MapSerializableRunnable;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.rapi.ProcessModel;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.utils.GUID;
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

    /** Maximum age of a process before it is quiesced */
    private static Long __processMaxAge;

    public final static String DEFERRED_PROCESS_INSTANCE_CLEANUP_DISABLED_NAME =
        "org.apache.ode.disable.deferredProcessInstanceCleanup";
    
    private static boolean DEFERRED_PROCESS_INSTANCE_CLEANUP_DISABLED = 
        Boolean.getBoolean(DEFERRED_PROCESS_INSTANCE_CLEANUP_DISABLED_NAME);

    /** RNG, for delays */
    private Random _random = new Random(System.currentTimeMillis());

    private static double _delayMean = 0;

    /**
     * Set of processes that are registered with the server. Includes hydrated and dehydrated processes. Guarded by
     * _mngmtLock.writeLock().
     */
    private final HashMap<QName, ODEProcess> _registeredProcesses = new HashMap<QName, ODEProcess>();

    /** Mapping from myrole service name to active process. */
    private final HashMap<QName, List<ODEProcess>> _wsServiceMap = new HashMap<QName, List<ODEProcess>>();

    private final HashMap<String, ODERESTProcess> _restServiceMap = new HashMap<String, ODERESTProcess>();

    /** Weak-reference cache of all the my-role message exchange objects. */
    private final IncomingMessageExchangeCache _incomingMexCache = new IncomingMessageExchangeCache();

    private State _state = State.SHUTDOWN;

    Contexts _contexts = new Contexts();

    private DehydrationPolicy _dehydrationPolicy;

    private OdeConfigProperties _properties;

    private ExecutorService _exec;

    BpelDatabase _db;

    private boolean _shutdownExecutor = false;

    /**
     * Management lock for synchronizing management operations and preventing processing (transactions) from occuring while
     * management operations are in progress.
     */
    private ReadWriteLock _mngmtLock = new ReentrantReadWriteLock();

    /**
     * The last time we started a {@link ServerCallable}. Useful for keeping track of idleness.
     */
    private final AtomicLong _lastTimeOfServerCallable = new AtomicLong(System.currentTimeMillis());
    
    /** Mapping from a potentially shared endpoint to its EPR */ 
    private SharedEndpoints _sharedEps;

    static {
        // TODO Clean this up and factorize engine configuration
        try {
            String processMaxAge = System.getProperty("ode.process.maxage");
            if (processMaxAge != null && processMaxAge.length() > 0) {
                __processMaxAge = Long.valueOf(processMaxAge);
                __log.debug("Process definition max age adjusted. Max age = " + __processMaxAge + "ms.");
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
    
    protected void waitForQuiessence() {
        do{
        _mngmtLock.writeLock().lock();
        _mngmtLock.writeLock().unlock();
        long ltime = _lastTimeOfServerCallable.get();
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            ;
        }
        _mngmtLock.writeLock().lock();
        _mngmtLock.writeLock().unlock();
        try {
            Thread.sleep(150);
        } catch (InterruptedException ie) {
            ;
        }
        if (_lastTimeOfServerCallable.get() == ltime)
            return;
        } while (true);
        
            
    }
    
    public void start() {
        _mngmtLock.writeLock().lock();
        try {
            if (!checkState(State.INIT, State.RUNNING)) {
                __log.debug("start() ignored -- already started");
                return;
            }

            __log.debug("BPEL SERVER starting.");

            if (_exec == null) {
                ThreadFactory threadFactory = new ThreadFactory() {
                    int threadNumber = 0;
                    public Thread newThread(Runnable r) {
                        threadNumber += 1;
                        Thread t = new Thread(r, "ODEServerImpl-"+threadNumber);
                        t.setDaemon(true);
                        return t;
                    }
                };
                _exec = Executors.newCachedThreadPool(threadFactory);
                _shutdownExecutor = true;
            }
            
            if (_contexts.txManager == null) {
                String errmsg = "Transaction manager not specified; call setTransactionManager(...)!";
                __log.fatal(errmsg);
                throw new IllegalStateException(errmsg);
            }
            
            if (_contexts.scheduler == null) { 
                String errmsg = "Scheduler not specified; call setScheduler(...)!";
                __log.fatal(errmsg);
                throw new IllegalStateException(errmsg);
            }
            
            _contexts.scheduler.start();
            _state = State.RUNNING;
            __log.debug(__msgs.msgServerStarted());
            if (_dehydrationPolicy != null)
                new Thread(new ProcessDefReaper()).start();
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }
    
    
    public void registerExternalVariableEngine(ExternalVariableModule eve) {
        _contexts.externalVariableEngines.put(eve.getName(), eve);
    }

    /**
     * Register a global listener to receive {@link BpelEvent}s froom all processes.
     * 
     * @param listener
     */
    public void registerBpelEventListener(BpelEventListener listener) {
        listener.startup(_properties.getProperties());

        // Do not synchronize, eventListeners is copy-on-write array.
        _contexts.eventListeners.add(listener);
    }

    /**
     * Unregister a global listener from receive {@link BpelEvent}s from all processes.
     * 
     * @param listener
     */
    public void unregisterBpelEventListener(BpelEventListener listener) {
        // Do not synchronize, eventListeners is copy-on-write array.
        if (_contexts.eventListeners.remove(listener)) {
            try {
                listener.shutdown();
            } catch (Exception e) {
                __log.warn("Stopping BPEL event listener " + listener.getClass().getName()
                        + " failed, nevertheless it has been unregistered.", e);
            }
        }
    }

    private void unregisterBpelEventListeners() {
        for (BpelEventListener l : _contexts.eventListeners) {
            unregisterBpelEventListener(l);
        }
    }

    public void registerExtensionBundle(ExtensionBundleRuntime bundle) {
        _contexts.extensionRegistry.put(bundle.getNamespaceURI(), bundle);
        bundle.registerExtensionActivities();
    }

    public void unregisterExtensionBundle(String nsURI) {
        _contexts.extensionRegistry.remove(nsURI);
    }

    public void registerContextInterceptor(ContextInterceptor interceptor) {
    	_contexts.contextInterceptorRegistry.add(interceptor);
    }

    public void unregisterContextInterceptor(ContextInterceptor interceptor) {
        _contexts.contextInterceptorRegistry.remove(interceptor);
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
            if (_shutdownExecutor){
                _exec.shutdownNow();
                _exec = null;
                _shutdownExecutor = false;
            }
            _state = State.INIT;
            __log.debug(__msgs.msgServerStopped());
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
            _sharedEps = new SharedEndpoints();
            _sharedEps.init();

        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    public void shutdown() throws BpelEngineException {
        _mngmtLock.writeLock().lock();
        try {
            stop();
            unregisterBpelEventListeners();

            _sharedEps = null;
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

        // Ok, IO out of the way, we will mod the server state, so need to get a lock.
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

            ODEProcess process;
            if (conf.isRestful()) {
                ODERESTProcess restProcess = new ODERESTProcess(this, conf, null, _incomingMexCache);
                for (String resUrl : restProcess.initResources()) {
                    _restServiceMap.put(resUrl, restProcess);
                }
                process = restProcess;
            } else {
                ODEWSProcess wsProcess = new ODEWSProcess(this, conf, null, _incomingMexCache);
                for (Endpoint e : wsProcess.getServiceNames()) {
                    __log.debug("Register process: serviceId=" + e + ", process=" + wsProcess);
                    // Get the list of processes associated with the given service
                    List<ODEProcess> processes = _wsServiceMap.get(e.serviceName);
                    // Create an empty list, if no processes were associated
                    if (processes == null)
                        _wsServiceMap.put(e.serviceName, processes = new ArrayList<ODEProcess>());

                    // Remove any older version of the process from the list
                    for (int i = 0; i < processes.size(); i++) {
                        ODEProcess cachedVersion = processes.get(i);
                        __log.debug("cached version " + cachedVersion.getPID() + " vs registering version " + wsProcess.getPID());
                        if (cachedVersion.getProcessType().equals(wsProcess.getProcessType()))
                            processes.remove(cachedVersion);
                    }
                    // Add the given process to the list associated with the given service
                    processes.add(wsProcess);
                }
                process = wsProcess;
            }

            process.activate(_contexts);
            _registeredProcesses.put(process.getPID(), process);
            if (_dehydrationPolicy == null) process.hydrate();

            __log.debug(__msgs.msgProcessRegistered(conf.getProcessId()));
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
            ODEProcess p = _registeredProcesses.remove(pid);
            if (p == null)
                return;

            // TODO Looks like there are some possible bugs here, if a new version of a process gets
            // deployed, the service will be removed.
            p.deactivate();
            
            // Remove the process from any services that might reference it.
            // However, don't remove the service itself from the map.
            for (List<ODEProcess> processes : _wsServiceMap.values()) {
                __log.debug("removing process " + pid + "; handle " + p + "; exists " + processes.contains(p));
                processes.remove(p);
            }

            __log.debug(__msgs.msgProcessUnregistered(pid));

        } catch (Exception ex) {
            __log.error(__msgs.msgProcessUnregisterFailed(pid), ex);
            throw new BpelEngineException(ex);
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    public void cleanupProcess(QName pid) throws BpelEngineException {
        deleteProcessDAO(pid);
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
     * @param interceptor message-exchange interceptor
     */
    public void unregisterMessageExchangeInterceptor(MessageExchangeInterceptor interceptor) {
        // NOTE: do not synchronize, globalInterceptors is copy-on-write.
        _contexts.globalIntereceptors.remove(interceptor);
    }

    /**
     * Route to a process using the service id. Note, that we do not need the endpoint name here, we are assuming that two processes
     * would not be registered under the same service qname but different endpoint.
     * 
     * @param service target service id
     * @param request request message
     * @return process corresponding to the targetted service, or <code>null</code> if service identifier is not recognized.
     */
    List<ODEProcess> route(QName service, Message request) {
        // TODO: use the message to route to the correct service if more than
        // one service is listening on the same endpoint.
        _mngmtLock.readLock().lock();
        try {
            return _wsServiceMap.get(service);
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
        return false;
    }

    protected boolean deleteProcessDAO(final QName pid) {
        try {
            return _db.exec(new BpelDatabase.Callable<Boolean>() {
                public Boolean run(BpelDAOConnection conn) throws Exception {
                    return deleteProcessDAO(conn, pid);
                }
            });
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
                _contexts.scheduler.scheduleMapSerializableRunnable(
                    new ProcessCleanUpRunnable(((DeferredProcessInstanceCleanable)proc).getId()), new Date());
            } else if( proc instanceof DeferredProcessInstanceCleanable ) {
                ((DeferredProcessInstanceCleanable)proc).deleteInstances(Integer.MAX_VALUE);
            }
            return true;
        }
        return false;
        
    }

    public void onScheduledJob(final JobInfo jobInfo) throws JobProcessorException {
        _mngmtLock.readLock().lock();
        try {
            final JobDetails j = jobInfo.jobDetail;
            ODEProcess process = _registeredProcesses.get(j.getProcessId());
            if (process == null) {
                // If the process is not active, it means that we should not be
                // doing any work on its behalf, therefore we will reschedule the
                // events for some time in the future (1 minute).
                _contexts.execTransaction(new Callable<Void>() {
                    public Void call() throws Exception {
                        _contexts.scheduler.jobCompleted(jobInfo.jobName);
		                Date future = new Date(System.currentTimeMillis() + (60 * 1000));
		                __log.debug(__msgs.msgReschedulingJobForInactiveProcess(j.getProcessId(), jobInfo.jobName, future));
                        _contexts.scheduler.schedulePersistedJob(j, future);            
						return null;
                    }
                    
                });
                return;
            }
            
            if (j.getType().equals(Scheduler.JobType.INVOKE_CHECK)) {
                if (__log.isDebugEnabled()) __log.debug("handleWorkEvent: InvokeCheck event for mexid " + j.getMexId());

                PartnerRoleMessageExchange mex = (PartnerRoleMessageExchange) getMessageExchange(j.getMexId());
                if (mex.getStatus() == MessageExchange.Status.ASYNC || mex.getStatus() == MessageExchange.Status.ACK) {
                    String msg = "No response received for invoke (mexId=" + j.getMexId() + "), forcing it into a failed state.";
                    if (__log.isDebugEnabled()) __log.debug(msg);
                    mex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, msg, null);
                }
                return;
            }

            process.handleWorkEvent(jobInfo);
        } catch (Exception ex) {
            throw new JobProcessorException(ex, jobInfo.jobDetail.getInMem() == false);
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

    public void setConfigProperties(OdeConfigProperties properties) {
        _properties = properties;
    }
    
    public OdeConfigProperties getConfigProperties() {
        return _properties;
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
    
    public SharedEndpoints getSharedEndpoints() {
        return _sharedEps;
    }

    public void setExecutor(ExecutorService exec) {
      _exec = exec;
    }

    public MyRoleMessageExchange createMessageExchange(final InvocationStyle istyle, final QName targetService,
            final String operation, final String clientKey) throws BpelEngineException {

        _mngmtLock.readLock().lock();
        try {
            // Obtain the list of processes that this service is potentially targeted at
            final List<ODEProcess> targets = route(targetService, null);

            if (targets == null || targets.size() == 0)
                throw new BpelEngineException("NoSuchService: " + targetService);
            
            if (targets.size() == 1) {
                // If the number of targets is one, create and return a simple MEX
                ODEProcess target = targets.get(0);
                return createNewMyRoleMex(target, istyle, targetService, operation, clientKey);
            } else {
                // If the number of targets is greater than one, create and return
                // a brokered MEX that embeds the simple MEXs for each of the targets
                ArrayList<MyRoleMessageExchange> meps = new ArrayList<MyRoleMessageExchange>();
                for (ODEProcess target : targets) {
                    meps.add(createNewMyRoleMex(target, istyle, targetService, operation, clientKey));
                }
                return createNewMyRoleMex(targets.get(0), meps, istyle);    
            }
        } finally {
            _mngmtLock.readLock().unlock();
        }
    }

    public RESTInMessageExchange createMessageExchange(final Resource resource, String foreignKey) throws BpelEngineException {
        _mngmtLock.readLock().lock();
        try {
            ODERESTProcess target = _restServiceMap.get(resource.getUrl());
            if (target == null) {
                try {
                    QName processId = _contexts.execTransaction(new Callable<QName>() {
                        public QName call() {
                            ResourceRouteDAO rr = _contexts.dao.getConnection()
                                    .getResourceRoute(resource.getUrl(), resource.getMethod());
                            if (rr == null) return null;
                        ProcessDAO processDao = rr.getInstance().getProcess();
                            return processDao.getProcessId();
                    }
                    });
                    for (ODEProcess odeRestProcess : _registeredProcesses.values()) {
                        if (odeRestProcess._pid.equals(processId)) target = (ODERESTProcess)odeRestProcess;
                    }
                } catch (Exception e) {
                    throw new BpelEngineException(e);
                }
            }

            if (target == null) throw new BpelEngineException("No such resource: " + resource.getUrl());
            assertNoTransaction();
            return target.createRESTMessageExchange(resource, foreignKey);
        } finally {
            _mngmtLock.readLock().unlock();
        }
    }

    /**
     * Return a simple type of MEX for a given process target
     * @param process
     * @param istyle
     * @param targetService
     * @param operation
     * @param clientKey
     * @return
     */
    private MyRoleMessageExchange createNewMyRoleMex(ODEProcess process, final InvocationStyle istyle, final QName targetService,
            final String operation, final String clientKey) {
        if (istyle == InvocationStyle.RELIABLE || istyle == InvocationStyle.TRANSACTED)
            assertTransaction();
        else
            assertNoTransaction();
    
        return ((ODEWSProcess)process).createNewMyRoleMex(istyle, targetService, operation);
    }
    
    /**
     * Return a brokered MEX that delegates invocations to each of the embedded
     * MEXs contained in the <code>meps</code> list, using the appropriate style.
     * 
     * @param target
     * @param meps
     * @param istyle
     * @return
     * @throws BpelEngineException
     */
    private MyRoleMessageExchange createNewMyRoleMex(ODEProcess target, List<MyRoleMessageExchange> meps, InvocationStyle istyle)
            throws BpelEngineException {
        String mexId = new GUID().toString();
        MyRoleMessageExchange template = meps.get(0);
        switch (istyle) {
        case RELIABLE:
            return new BrokeredReliableMyRoleMessageExchangeImpl(target, meps, mexId, template);
        case TRANSACTED:
            return new BrokeredTransactedMyRoleMessageExchangeImpl(target, meps, mexId, template);
        case UNRELIABLE:
            return new BrokeredUnreliableMyRoleMessageExchangeImpl(target, meps, mexId, template);
        case P2P:
        default:
            throw new BpelEngineException("Unsupported Invocation Style: " + istyle);
        }
    }
    
    public MessageExchange getMessageExchange(final String mexId) throws BpelEngineException {

        _mngmtLock.readLock().lock();
        try {
            final MessageExchangeDAO inmemdao = getInMemMexDAO(mexId);

            Callable<MessageExchange> loadMex = new Callable<MessageExchange>() {

                public MessageExchange call() {
                    MessageExchangeDAO mexdao = (inmemdao == null) ?
                            mexdao = _contexts.dao.getConnection().getMessageExchange(mexId) : inmemdao;
                    if (mexdao == null) return null;

                    ProcessDAO pdao = mexdao.getProcess();
                    ODEProcess process = pdao == null ? null : _registeredProcesses.get(pdao.getProcessId());

                    if (process == null) {
                        String errmsg = __msgs.msgProcessNotActive(pdao.getProcessId());
                        __log.error(errmsg);
                        // TODO: Perhaps we should define a checked exception for this
                        // condition.
                        throw new BpelEngineException(errmsg);
                    }

                    InvocationStyle istyle = mexdao.getInvocationStyle();
                    if (istyle == InvocationStyle.RELIABLE || istyle == InvocationStyle.TRANSACTED)
                        assertTransaction();

                    switch (mexdao.getDirection()) {
                    case MessageExchangeDAO.DIR_BPEL_INVOKES_PARTNERROLE:
                        return ((ODEWSProcess)process).createPartnerRoleMex(mexdao);
                    case MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE:
                        return ((ODEWSProcess)process).lookupMyRoleMex(mexdao);
                    default:
                        String errmsg = "BpelEngineImpl: internal error, invalid MexDAO direction: " + mexId;
                        __log.fatal(errmsg);
                        throw new BpelEngineException(errmsg);
                    }
                }
            };

            try {
                if (inmemdao != null || _contexts.isTransacted()) 
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

    /*
     * Calculate the invocation style of the target(s) associated with the given service
     * If more than one target exists, then take the intersection of their individual styles.
     *   
     * (non-Javadoc)
     * @see org.apache.ode.bpel.iapi.BpelServer#getSupportedInvocationStyle(javax.xml.namespace.QName)
     */
    public Set<InvocationStyle> getSupportedInvocationStyle(QName serviceId) {

        _mngmtLock.readLock().lock();
        try {
            List<ODEProcess> processes = route(serviceId, null);
            if (processes == null || processes.size() == 0)
                throw new BpelEngineException("No such service: " + serviceId);

            // Compute the intersection of the styles of all providing processes 
            Set<InvocationStyle> istyles = new HashSet<InvocationStyle>();
            for (ODEProcess process : processes) {
                Set<InvocationStyle> pistyles = process.getSupportedInvocationStyle(serviceId);
                if (istyles.isEmpty()) {
                    istyles.addAll(pistyles);
                } else {
                    for (InvocationStyle istyle : istyles) {
                        if (!pistyles.contains(istyle)) {
                            istyles.remove(istyle);
                        }
                    }
                }
            }
            return istyles;
        } finally {
            _mngmtLock.readLock().unlock();
        }
    }

    MessageExchangeDAO getInMemMexDAO(String mexId) {
        _mngmtLock.readLock().lock();
        try {
          for (ODEProcess p : _registeredProcesses.values()) {
              MessageExchangeDAO mexDao = p.getInMemMexDAO(mexId);
              if (mexDao != null)
                  return mexDao;
          }
        } finally {
            _mngmtLock.readLock().unlock();
        }
        
        return null;
    }
    
    ProcessModel getProcessModel(QName processId) {
        _mngmtLock.readLock().lock();
        try {
            ODEProcess process = _registeredProcesses.get(processId);

            if (process == null) return null;

            return process.getProcessModel();

        } finally {
            _mngmtLock.readLock().unlock();
        }
    }


    <T> Future<T> enqueueTransaction(final Callable<T> transaction) throws ContextException {
        return _exec.submit(new ServerCallable<T>(new TransactedCallable<T>(transaction)));
    }

    void enqueueRunnable(final Runnable runnable) {
        _exec.submit(new ServerRunnable(runnable));
    }
    
    /**
     * Schedule a {@link Runnable} object for execution after the completion of the current transaction. 
     * @param runnable
     */
    void scheduleRunnable(final Runnable runnable) {
        assertTransaction();
        _contexts.registerCommitSynchronizer(new Runnable() {
            public void run() {
                _exec.submit(new ServerRunnable(runnable));
            }
            
        });
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
        return (long) (-Math.log(u) * mean);
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
                        // Copying the runnning process list to avoid synchronizatMessageExchangeInterion
                        // problems and a potential mess if a policy modifies the list
                        List<ODEProcess> candidates = new ArrayList<ODEProcess>(_registeredProcesses.values());
                        CollectionsX.remove_if(candidates, new MemberOfFunction<ODEProcess>() {
                            public boolean isMember(ODEProcess o) {
                                return !o.hintIsHydrated();
                            }
                        });

                        // And the happy winners are...
                        List<ODEProcess> ripped = _dehydrationPolicy.markForDehydration(candidates);
                        // Bye bye
                        for (ODEProcess process : ripped) {
                            __log.debug("Dehydrating process " + process.getPID());
                            process.dehydrate();
                        }
                    } finally {
                        _mngmtLock.writeLock().unlock();
                    }
                }
            } catch (InterruptedException e) {
                __log.debug(e);
            }
        }
    }

    public ODEProcess getBpelProcess(QName processId) {
        _mngmtLock.readLock().lock();
        try {
            return _registeredProcesses.get(processId);
        } finally {
            _mngmtLock.readLock().unlock();
        }
    }

    private void ticktock() {
        _lastTimeOfServerCallable.set(System.currentTimeMillis());
        
    }
    
    class ServerRunnable implements Runnable {
        final Runnable _work;
        ServerRunnable(Runnable work) {
            _work = work;
        }
        
        public void run() {
            ticktock();
            _mngmtLock.readLock().lock();
            try {
                ticktock();
                _work.run();
                ticktock();
            } catch (Throwable ex) {
                ticktock();
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
            ticktock();
            _mngmtLock.readLock().lock();
            try {
                ticktock();
                return _work.call();
            } catch (Exception ex) {
                ticktock();
                __log.fatal("Internal Error", ex);
                throw ex;
            } finally {
                _mngmtLock.readLock().unlock();
                ticktock();
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
    
    public void setTransacted(boolean atomicScope) {
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
                    jobInfo.jobDetail.getDetailsExt().put("runnable_status", JOB_STATUS.COMPLETED);
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
                            MapSerializableRunnable runnable = (MapSerializableRunnable)jobInfo.jobDetail.getDetailsExt().get("runnable");
                            runnable.restoreFromDetails(jobInfo.jobDetail);
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
            
            jobInfo.jobDetail.getDetailsExt().put("runnable_status", JOB_STATUS.IN_PROGRESS);
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
            deleteProcessDAO(pconf.getProcessId());
        }
    }
}
