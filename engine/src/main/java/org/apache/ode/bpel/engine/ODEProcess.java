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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.*;
import org.apache.ode.bpel.engine.extvar.ExternalVariableConf;
import org.apache.ode.bpel.engine.extvar.ExternalVariableManager;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.intercept.FailMessageExchangeException;
import org.apache.ode.bpel.intercept.FaultMessageExchangeException;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.bpel.memdao.ProcessInstanceDaoImpl;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.bpel.rapi.*;
import org.apache.ode.jacob.soup.ReplacementMap;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Entry point into the runtime of a BPEL process.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 * @author Matthieu Riou <mriou at apache dot org>
 */
public abstract class ODEProcess {
    static final Log __log = LogFactory.getLog(ODEProcess.class);

    protected static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    protected final QName _pid;
    protected volatile ProcessModel _processModel;
    protected volatile OdeRuntime _runtime;
    protected final ProcessConf _pconf;
    protected Contexts _contexts;
    protected final Set<InvocationStyle> _invocationStyles;
    protected final BpelDAOConnectionFactoryImpl _inMemDao;
    protected final BpelServerImpl _server;
    protected IncomingMessageExchangeCache _incomingMexCache;

    /** Last time the process was used. */
    protected volatile long _lastUsed;

    protected DebuggerSupport _debugger;

    /** {@link MessageExchangeInterceptor}s registered for this process. */
    protected final List<MessageExchangeInterceptor> _mexInterceptors = new ArrayList<MessageExchangeInterceptor>();
    protected final BpelInstanceWorkerCache _instanceWorkerCache = new BpelInstanceWorkerCache(this);

    /** Deploy-time configuraton for external variables. */
    protected ExternalVariableConf _extVarConf;
        protected ExternalVariableManager _evm;
    
    ODEProcess(BpelServerImpl server, ProcessConf conf, BpelEventListener debugger, IncomingMessageExchangeCache mexCache) {
        _server = server;
        _pid = conf.getProcessId();
        _pconf = conf;
        _contexts = server._contexts;
        if (_contexts.dao instanceof BpelDAOConnectionFactoryImpl)
            _inMemDao = (BpelDAOConnectionFactoryImpl) _contexts.dao;
        else
            _inMemDao = new BpelDAOConnectionFactoryImpl(_contexts.txManager);
        _incomingMexCache = mexCache;

        // TODO : do this on a per-partnerlink basis, support transacted styles.
        HashSet<InvocationStyle> istyles = new HashSet<InvocationStyle>();
        istyles.add(InvocationStyle.UNRELIABLE);

        if (!conf.isTransient()) istyles.add(InvocationStyle.RELIABLE);
        else istyles.add(InvocationStyle.TRANSACTED);
        _invocationStyles = Collections.unmodifiableSet(istyles);
    }

    abstract void activate(Contexts contexts);
    abstract void deactivate();
    abstract void hydrate();
    abstract void dehydrate();

    abstract void invokeProcess(final MessageExchangeDAO mexdao);
    abstract void invokePartner(final MessageExchangeDAO mexdao);

    abstract MessageExchangeImpl recreateIncomingMex(MessageExchangeDAO mexdao);

    protected abstract void latch(int s);
    protected abstract void releaseLatch(int s);
    protected abstract boolean isLatched(int s);

    /**
     * Retrives the base URI to use for local resource resolution.
     * 
     * @return URI - instance representing the absolute file path to the physical location of the process definition folder.
     */
    public URI getBaseResourceURI() {
        return this._pconf.getBaseURI();
    }
    
    /**
     * Intiialize the external variable configuration/engine manager. This is called from hydration logic, so it 
     * is possible to change the external variable configuration at runtime.
     * 
     */
    void initExternalVariables() {
        List<Element> conf = _pconf.getExtensionElement(ExternalVariableConf.EXTVARCONF_ELEMENT);
        _extVarConf = new ExternalVariableConf(conf);
        _evm = new ExternalVariableManager(_pid, _extVarConf, _contexts.externalVariableEngines);
    }
    
    public OdeConfigProperties getProperties() {
        return _server.getConfigProperties();
    }
    
    public ExternalVariableManager getEVM() {
        return _evm;
    }

    public String toString() {
        return "ODEProcess[" + _pid + "]";
    }

    void recoverActivity(ProcessInstanceDAO instanceDAO, final String channel, final long activityId, final String action,
            final FaultInfo fault) {
        if (__log.isDebugEnabled())
            __log.debug("Recovering activity in process " + instanceDAO.getInstanceId() + " with action " + action);

        latch(1);
        try {
            markused();
            BpelInstanceWorker iworker = _instanceWorkerCache.get(instanceDAO.getInstanceId());
            final OdeRTInstance rti = _runtime.newInstance(getState(iworker, instanceDAO));
            final BpelRuntimeContextImpl processInstance = new BpelRuntimeContextImpl(iworker, instanceDAO, rti);
            try {
                iworker.execInCurrentThread(new Callable<Void>() {
                    public Void call() throws Exception {
                        processInstance.recoverActivity(channel, activityId, action, fault);
                        return null;
                    }
                });
            } catch (Exception e) {
                throw new BpelEngineException(e);
            }
        } finally {
            releaseLatch(1);
        }
    }

    void executeCreateInstance(MessageExchangeDAO mexdao) {
        assert isLatched(1);

        BpelInstanceWorker worker = _instanceWorkerCache.get(mexdao.getInstance().getInstanceId());
        assert worker.isWorkerThread();
        BpelRuntimeContextImpl rtictx = new BpelRuntimeContextImpl(
                worker, mexdao.getInstance(), _runtime.newInstance(getState(worker, mexdao.getInstance())));
        rtictx.executeCreateInstance(mexdao);
    }

    void executeContinueInstanceMyRoleRequestReceived(MessageExchangeDAO mexdao) {
        assert isLatched(1);

        assert mexdao != null;
        assert mexdao.getInstance() != null;
        
        BpelInstanceWorker worker = _instanceWorkerCache.get(mexdao.getInstance().getInstanceId());
        assert worker.isWorkerThread();

        OdeRTInstance rti = _runtime.newInstance(getState(worker, mexdao.getInstance()));
        BpelRuntimeContextImpl instance = new BpelRuntimeContextImpl(worker, mexdao.getInstance(), rti);
        int amp = mexdao.getChannel().indexOf('&');
        String groupId = mexdao.getChannel().substring(0, amp);
        int idx = Integer.valueOf(mexdao.getChannel().substring(amp + 1));
        instance.injectMyRoleMessageExchange(groupId, idx, mexdao);
        instance.execute();
    }

    void executeContinueInstanceResume(ProcessInstanceDAO instanceDao, int retryCount) {
        BpelInstanceWorker worker = _instanceWorkerCache.get(instanceDao.getInstanceId());
        assert worker.isWorkerThread();

        OdeRTInstance rti = _runtime.newInstance(getState(worker, instanceDao));
        BpelRuntimeContextImpl brc = new BpelRuntimeContextImpl(worker, instanceDao, rti);
        brc.setRetryCount(retryCount);
        brc.execute();

    }

    void executeContinueInstanceTimerReceived(ProcessInstanceDAO instanceDao, String timerChannel) {
        BpelInstanceWorker worker = _instanceWorkerCache.get(instanceDao.getInstanceId());
        assert worker.isWorkerThread();

        OdeRTInstance rti = _runtime.newInstance(getState(worker, instanceDao));
        BpelRuntimeContextImpl brc = new BpelRuntimeContextImpl(worker, instanceDao, rti);
        if (brc.injectTimerEvent(timerChannel)) brc.execute();

    }

    private void executeContinueInstanceMatcherEvent(ProcessInstanceDAO instanceDao, String correlatorId,
            CorrelationKey correlationKey) {

        if (__log.isDebugEnabled()) {
            __log.debug("MatcherEvent handling: correlatorId=" + correlatorId + ", ckey=" + correlationKey);
        }

        CorrelatorDAO correlator = instanceDao.getProcess().getCorrelator(correlatorId);

        // Find the route first, this is a SELECT FOR UPDATE on the "selector" row,
        // So we want to acquire the lock before we do anthing else.
        MessageRouteDAO mroute = correlator.findRoute(correlationKey);
        if (mroute == null) {
            // Ok, this means that a message arrived before we did, so nothing to do.
            __log.debug("MatcherEvent handling: nothing to do, route no longer in DB");
            return;
        }

        // Now see if there is a message that matches this selector.
        MessageExchangeDAO mexdao = correlator.dequeueMessage(correlationKey);
        if (mexdao != null) {
            __log.debug("MatcherEvent handling: found matching message in DB (i.e. message arrived before <receive>)");

            // We have a match, so we can get rid of the routing entries.
            correlator.removeRoutes(mroute.getGroupId(), instanceDao);
            mexdao.setInstance(instanceDao);

            // Found message matching one of our selectors.
            if (__log.isDebugEnabled()) {
                __log.debug("SELECT: " + mroute.getGroupId() + ": matched to MESSAGE " + mexdao + " on CKEY " + correlationKey);
            }

            BpelInstanceWorker worker = _instanceWorkerCache.get(instanceDao.getInstanceId());
            assert worker.isWorkerThread();

            OdeRTInstance rti = _runtime.newInstance(getState(worker, mexdao.getInstance()));
            BpelRuntimeContextImpl brc = new BpelRuntimeContextImpl(worker, instanceDao, rti);
            brc.injectMyRoleMessageExchange(mroute.getGroupId(), mroute.getIndex(), mexdao);
            brc.execute();
            
            mexdao.release(true);
        } else {
            __log.debug("MatcherEvent handling: nothing to do, no matching message in DB");

        }
    }

    void executeContinueInstancePartnerRoleResponseReceived(MessageExchangeDAO mexdao) {
        assert isLatched(1);
        ProcessInstanceDAO instanceDao = mexdao.getInstance();
        if (instanceDao == null)
            throw new BpelEngineException("InternalError: No instance for partner mex " + mexdao);

        BpelInstanceWorker worker = _instanceWorkerCache.get(mexdao.getInstance().getInstanceId());
        assert worker.isWorkerThread();

        OdeRTInstance rti = _runtime.newInstance(getState(worker, mexdao.getInstance()));
        BpelRuntimeContextImpl brc = new BpelRuntimeContextImpl(worker, mexdao.getInstance(), rti);
        // Canceling invoke check
        String jobId = mexdao.getProperty("invokeCheckJobId");
        if (jobId != null) 
            _contexts.scheduler.cancelJob(jobId);        

        brc.injectPartnerResponse(mexdao.getMessageExchangeId(), mexdao.getChannel());
        brc.execute();
    }

    void enqueueInstanceTransaction(Long instanceId, final Runnable runnable) {
        if (instanceId == null)
            throw new NullPointerException("instanceId was null!");

        BpelInstanceWorker iworker = _instanceWorkerCache.get(instanceId);
        iworker.enqueue(_server.new TransactedRunnable(runnable));
    }

    protected <T> T doInstanceWork(Long instanceId, final Callable<T> callable) {
        try {
            BpelInstanceWorker iworker = _instanceWorkerCache.get(instanceId);
            return iworker.execInCurrentThread(new ProcessCallable<T>(callable));

        } catch (Exception ex) {
            throw new BpelEngineException(ex);
        }
    }

    /**
     * Process the message-exchange interceptors.
     * @return <code>true</code> if execution should continue, <code>false</code> otherwise
     */
    boolean processInterceptors(MessageExchangeDAO mexdao, InterceptorInvoker invoker) {
        InterceptorContextImpl ictx = new InterceptorContextImpl(_contexts.dao.getConnection(), mexdao, getProcessDAO(), _pconf);

        try {
            for (MessageExchangeInterceptor interceptor : _mexInterceptors)
                invoker.invoke(interceptor, ictx);

            for (MessageExchangeInterceptor interceptor : _server._contexts.globalIntereceptors)
                invoker.invoke(interceptor, ictx);
        } catch (FailMessageExchangeException e) {
            MexDaoUtil.setFailed(mexdao, MessageExchange.FailureType.ABORTED, e.getMessage());
            return false;
        } catch (FaultMessageExchangeException e) {
            MexDaoUtil.setFaulted(mexdao, e.getFaultName(), e.getFaultData());
            return false;
        }

        return true;
    }

    /**
     * Handle a work event; this method is called from the scheduler thread and should be very quick, i.e. any serious work needs to
     * be handed off to a separate thread.
     */
    void handleWorkEvent(final Scheduler.JobInfo jobInfo) throws Scheduler.JobProcessorException {
        assert !_contexts.isTransacted() : "work events must be received outside of a transaction";

        markused();

        final WorkEvent we = new WorkEvent(jobInfo.jobDetail);
        if (__log.isDebugEnabled()) {
            __log.debug(ObjectPrinter.stringifyMethodEnter("handleWorkEvent", new Object[] { "jobInfo", jobInfo }));
        }

        enqueueInstanceTransaction(we.getIID(), new Runnable() {
            public void run() {
                _contexts.scheduler.jobCompleted(jobInfo.jobName);
                execInstanceEvent(we);
            }
        });

    }

    /**
     * Enqueue a transaction for execution by the engine.
     * 
     * @param tx
     *            the transaction
     */
    <T> Future<T> enqueueTransaction(final Callable<T> tx) {
        // We have to wrap our transaction to make sure that we are hydrated when the transaction runs.
        return _server.enqueueTransaction(new ProcessCallable<T>(tx));
    }

    private Object getState(BpelInstanceWorker worker, ProcessInstanceDAO instanceDAO) {
        ExecutionQueueImpl state = (ExecutionQueueImpl) worker.getCachedState(instanceDAO.getExecutionStateCounter());
        if (state != null) return state;

        if (isInMemory()) {
            ProcessInstanceDaoImpl inmem = (ProcessInstanceDaoImpl) instanceDAO;
            if (inmem.getSoup() != null) {
                state = (ExecutionQueueImpl) inmem.getSoup();
            }
        } else {
            byte[] daoState = instanceDAO.getExecutionState();
            if (daoState != null) {
                state = new ExecutionQueueImpl(getClass().getClassLoader());
                state.setReplacementMap((ReplacementMap) _runtime.getReplacementMap(instanceDAO.getProcess().getProcessId()));

                ByteArrayInputStream iis = new ByteArrayInputStream(daoState);
                try {
                    state.read(iis);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return state;
    }

    private void execInstanceEvent(WorkEvent we) {
        BpelInstanceWorker worker = _instanceWorkerCache.get(we.getIID());
        assert worker.isWorkerThread();

        ProcessInstanceDAO instanceDAO = getProcessDAO().getInstance(we.getIID());
        MessageExchangeDAO mexDao = we.getMexId() == null ? null : loadMexDao(we.getMexId());

        if (instanceDAO == null) {
            if (__log.isDebugEnabled()) {
                __log.debug("handleWorkEvent: no ProcessInstance found with iid " + we.getIID() + "; ignoring.");
            }
            return;
        }

        if (__log.isDebugEnabled()) {
            __log.debug("handleWorkEvent: " + we.getType() + " event for process instance " + we.getIID());
        }

        switch (we.getType()) {
        case MYROLE_INVOKE:
            executeContinueInstanceMyRoleRequestReceived(mexDao);
            if(__log.isDebugEnabled()) __log.debug("handleWorkEvent: releasing myrole mex dao: " + mexDao);
            mexDao.release(true);
            break;
        case TIMER:
            executeContinueInstanceTimerReceived(instanceDAO, we.getChannel());
            break;
        case RESUME:
            executeContinueInstanceResume(instanceDAO, we.getRetryCount());
            break;
        case PARTNER_RESPONSE:
            executeContinueInstancePartnerRoleResponseReceived(mexDao);
            break;
        case MATCHER:
            executeContinueInstanceMatcherEvent(instanceDAO, we.getCorrelatorId(), we.getCorrelationKey());
            break;
        }
    }


    MessageExchangeDAO loadMexDao(String mexId) {
        return isInMemory() ? _inMemDao.getConnection().getMessageExchange(mexId) : _contexts.dao.getConnection()
                .getMessageExchange(mexId);
    }

    ProcessDAO getProcessDAO() {
        return isInMemory() ? _inMemDao.getConnection().getProcess(_pid) : _contexts.dao.getConnection().getProcess(_pid);
    }

    static String genCorrelatorId(PartnerLinkModel plink, String opName) {
        return plink.getId() + "." + opName;
    }

    /**
     * Get all the services that are implemented by this process.
     * 
     * @return list of qualified names corresponding to the myroles.
     */
    public Set<Endpoint> getServiceNames() {
        Set<Endpoint> endpoints = new HashSet<Endpoint>();
        for (Endpoint provide : _pconf.getProvideEndpoints().values()) {
            endpoints.add(provide);
        }
        return endpoints;
    }

    protected boolean isShareable(Endpoint endpoint) {
        if (!_pconf.isSharedService(endpoint.serviceName)) return false;

//    	PartnerLinkMyRoleImpl partnerLink = _endpointToMyRoleMap.get(endpoint);
//        return partnerLink != null && partnerLink.isOneWayOnly();
        return false;
    }

    QName getPID() {
        return _pid;
    }

    QName getProcessType() {
        return _pconf.getType();
    }

    public void saveEvent(ProcessInstanceEvent event, ProcessInstanceDAO instanceDao) {
        saveEvent(event, instanceDao, null);
    }
    public void saveEvent(ProcessInstanceEvent event, ProcessInstanceDAO instanceDao, List<String> scopeNames) {
        markused();
        if (_pconf.isEventEnabled(scopeNames, event.getType())) {
            // notify the listeners
            _server.fireEvent(event);
            if (instanceDao != null)
                instanceDao.insertBpelEvent(event);
            else
                __log.debug("Couldn't find instance to save event, no event generated!");
        }
    }


    ProcessModel getProcessModel() {
        latch(1);
        try {
            return _processModel;
        } finally {
            releaseLatch(1);
        }
    }

    Set<InvocationStyle> getSupportedInvocationStyle(QName serviceId) {
        return _invocationStyles;
    }

    public boolean isInMemory() {
        return _pconf.isTransient();
    }

    public long getLastUsed() {
        return _lastUsed;
    }

    /**
     * Get a hint as to whether this process is hydrated. Note this is only a hint, since things could change.
     */
    public boolean hintIsHydrated() {
        return _processModel != null;
    }

    /** Keep track of the time the process was last used. */
    protected final void markused() {
        _lastUsed = System.currentTimeMillis();
    }

    protected void bounceProcessDAO() {
        if (isInMemory()) {
            doBounce(_inMemDao.getConnection(), _pid, _pconf.getVersion(), _processModel);
        } else if (_contexts.isTransacted()) {
            // If we have a transaction, we do this in the current transaction.
            doBounce(_contexts.dao.getConnection(), _pid, _pconf.getVersion(), _processModel);
        } else {
            // If we do not have a transaction we need to create one.
            try {
                _contexts.execTransaction(new Callable<Object>() {
                    public Object call() throws Exception {
                        deleteProcessDAO(_contexts.dao.getConnection(), _pid, _pconf.getVersion(), _processModel);
                        return null;
                    }
                });
                _contexts.execTransaction(new Callable<Object>() {
                    public Object call() throws Exception {
                        createProcessDAO(_contexts.dao.getConnection(), _pid, _pconf.getVersion(), _processModel);
                        return null;
                    }
                });
            } catch (Exception ex) {
                String errmsg = "DbError";
                __log.error(errmsg, ex);
                throw new BpelEngineException(errmsg, ex);
            }
        }
    }

    /**
     * If necessary, create an object in the data store to represent the process. We'll re-use an existing object if it already
     * exists and matches the GUID.
     */
    private void doBounce(BpelDAOConnection conn, final QName pid, final long version, final ProcessModel mprocess) {
        // SEAN; I do not think we need this one anymore -- needs to confirm
        // deleteProcessDAO(conn, pid, version, mprocess);
        createProcessDAO(conn, pid, version, mprocess);
    }
    
    private void deleteProcessDAO(BpelDAOConnection conn, final QName pid, final long version, final ProcessModel mprocess) {
        __log.debug("Creating process DAO for " + pid + " (guid=" + mprocess.getGuid() + ")");
        try {
            ProcessDAO old = conn.getProcess(pid);
            if (old != null) {
                __log.debug("Found ProcessDAO for " + pid + " with GUID " + old.getGuid());
                if (mprocess.getGuid() == null) {
                    // No guid, old version assume its good
                } else {
                    if (old.getGuid().equals(mprocess.getGuid())) {
                        // Guids match, no need to create
                    } else {
                        // GUIDS dont match, delete and create new
                        String errmsg = "ProcessDAO GUID " + old.getGuid() + " does not match " + mprocess.getGuid() + "; replacing.";
                        __log.debug(errmsg);
                        old.deleteProcessAndRoutes();
                    }
                }
            }
        } catch (BpelEngineException ex) {
            throw ex;
        } catch (Exception dce) {
            __log.error("DbError", dce);
            throw new BpelEngineException("DbError", dce);
        }
    }

    private void createProcessDAO(BpelDAOConnection conn, final QName pid, final long version, final ProcessModel mprocess) {
        __log.debug("Creating process DAO for " + pid + " (guid=" + mprocess.getGuid() + ")");
        try {
            boolean create = true;
            ProcessDAO old = conn.getProcess(pid);
            if (old != null) {
                __log.debug("Found ProcessDAO for " + pid + " with GUID " + old.getGuid());
                if (mprocess.getGuid() == null) {
                    // No guid, old version assume its good
                    create = false;
                } else {
                    if (old.getGuid().equals(mprocess.getGuid())) {
                        // Guids match, no need to create
                        create = false;
                    } else {
                        // GUIDS dont match, delete and create new
                        String errmsg = "ProcessDAO GUID " + old.getGuid() + " does not match " + mprocess.getGuid() + "; replacing.";
                        __log.debug(errmsg);
                    }
                }
            }

            if (create) {
                ProcessDAO newDao = conn.createProcess(pid, mprocess.getQName(), mprocess.getGuid(), (int) version);
                for (String correlator : mprocess.getCorrelators()) {
                    newDao.addCorrelator(correlator);
                }
            }
        } catch (BpelEngineException ex) {
            throw ex;
        } catch (Exception dce) {
            __log.error("DbError", dce);
            throw new BpelEngineException("DbError", dce);
        }
    }
    
    MessageExchangeDAO createMessageExchange(String mexId, final char dir) {
        if (isInMemory()) {
            return _inMemDao.getConnection().createMessageExchange(mexId, dir);
        } else {
            return _contexts.dao.getConnection().createMessageExchange(mexId, dir);
        }
    }

    MessageExchangeDAO getInMemMexDAO(String mexId) {
        return _inMemDao.getConnection().getMessageExchange(mexId);
    }

    public void releaseMessageExchange(final String mexId) {
        if (isInMemory()) {
            _inMemDao.getConnection().releaseMessageExchange(mexId);
        } else {
            if( _contexts.isTransacted() ) {
                _contexts.dao.getConnection().releaseMessageExchange(mexId);
            } else {
                try {
                    _contexts.execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            _contexts.dao.getConnection().releaseMessageExchange(mexId);
                            return null;
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    /**
     * Schedule process-level work. This method defers to the server to do the scheduling and wraps the {@link Runnable} in a
     * try-finally block that ensures that the process is hydrated.
     * 
     * @param runnable
     */
    void scheduleRunnable(final Runnable runnable) {
        if (__log.isDebugEnabled()) __log.debug("schedulingRunnable for process " + _pid + ": " + runnable);

        _server.scheduleRunnable(new ProcessRunnable(runnable));
    }

    void enqueueRunnable(BpelInstanceWorker worker) {
        if (__log.isDebugEnabled()) __log.debug("enqueuRunnable for process " + _pid + ": " + worker);

        _server.enqueueRunnable(new ProcessRunnable(worker));
    }

    void p2pCall(MessageExchangeDAO mexdao, MessageExchange.Status old) {
            ODEProcess caller = _server.getBpelProcess(mexdao.getPipedPID());
            // process no longer deployed....
            if (caller == null) return;

            MessageExchangeDAO pmex = caller.loadMexDao(mexdao.getPipedMessageExchangeId());
            // Mex no longer there.... odd..
            if (pmex == null) return;

            // Need to copy the response and state from myrolemex --> partnerrolemex
            boolean compat = !(caller.isInMemory() ^ isInMemory());
            if (compat) {
                // both processes are in-mem or both are persisted, can share the message
                pmex.setResponse(mexdao.getResponse());
            } else /* one process in-mem, other persisted */{
                MessageDAO presponse = pmex.createMessage(mexdao.getResponse().getType());
                presponse.setData(mexdao.getResponse().getData());
                presponse.setHeader(mexdao.getResponse().getHeader());
                pmex.setResponse(presponse);
            }
            pmex.setFault(mexdao.getFault());
            pmex.setStatus(mexdao.getStatus());
            pmex.setAckType(mexdao.getAckType());
            pmex.setFailureType(mexdao.getFailureType());

            if (old == MessageExchange.Status.ASYNC) caller.p2pWakeup(pmex);
    }

    /**
     * Read an {@link org.apache.ode.bpel.rtrep.v2.OProcess} representation from a stream.
     *
     * @param is input stream
     * @return deserialized process representation
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    protected ProcessModel deserializeCompiledProcess(InputStream is) throws IOException, ClassNotFoundException {
        ProcessModel compiledProcess;
        Serializer ofh = new Serializer(is);
        compiledProcess = ofh.readPModel();
        return compiledProcess;
    }


    class ProcessRunnable implements Runnable {
        Runnable _work;

        ProcessRunnable(Runnable work) {
            _work = work;
        }
        public void run() {
            latch(1);
            try {
                _work.run();
            } finally {
                releaseLatch(1);
            }
        }
    }

    class ProcessCallable<T> implements Callable<T> {
        Callable<T> _work;

        ProcessCallable(Callable<T> work) {
            _work = work;
        }

        public T call() throws Exception {
            latch(1);
            try {
                return _work.call();
            } finally {
                releaseLatch(1);
            }
        }
    }

    public String scheduleWorkEvent(WorkEvent we, Date timeToFire) {
        // if (isInMemory())
        // throw new InvalidProcessException("In-mem process execution resulted in event scheduling.");

        return _contexts.scheduler.schedulePersistedJob(we.getDetail(), timeToFire);
    }

    protected OdeRuntime buildRuntime(int modelVersion) {
        // Relying on package naming conventions to find our runtime
        String qualifiedName = "org.apache.ode.bpel.rtrep.v" + modelVersion + ".RuntimeImpl";
        try {
            OdeRuntime runtime = (OdeRuntime) Class.forName(qualifiedName).newInstance();
            runtime.setExtensionRegistry(_contexts.extensionRegistry);
            return runtime;
        } catch (Exception e) {
            throw new RuntimeException("Couldn't instantiate ODE runtime version " + modelVersion +
                    ", either your process definition version is outdated or we have a bug.");
        }
    }

    void setStatefulEPRs(MessageExchangeDAO partnerRoleMex) {
        setStatefulEPRs(partnerRoleMex, null);
    }

    protected void setStatefulEPRs(MessageExchangeDAO partnerRoleMex, MessageExchangeDAO myRoleMex) {
        // Properties used by stateful-exchange protocol.
        String mySessionId = partnerRoleMex.getPartnerLink().getMySessionId();
        String partnerSessionId = partnerRoleMex.getPartnerLink().getPartnerSessionId();

        if (ODEProcess.__log.isDebugEnabled())
            __log.debug("Setting myRoleMex session ids for p2p interaction, mySession " + partnerSessionId
                    + " - partnerSess " + mySessionId);

        if (mySessionId != null) {
            partnerRoleMex.setProperty(WSMessageExchange.PROPERTY_SEP_MYROLE_SESSIONID, mySessionId);
            if (myRoleMex != null) myRoleMex.setProperty(WSMessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID, mySessionId);
        }
        if (partnerSessionId != null) {
            partnerRoleMex.setProperty(WSMessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID, partnerSessionId);
            if (myRoleMex != null) myRoleMex.setProperty(WSMessageExchange.PROPERTY_SEP_MYROLE_SESSIONID, partnerSessionId);
        }

        if (__log.isDebugEnabled())
            __log.debug("INVOKE PARTNER (SEP): sessionId=" + mySessionId + " partnerSessionId=" + partnerSessionId);
    }

    /**
     * Handle in-line P2P responses. Called from the child's transaction.
     * 
     * @param prolemex
     */
    protected void p2pWakeup(final MessageExchangeDAO prolemex) {
        try {
            doInstanceWork(prolemex.getInstance().getInstanceId(), new Callable<Void>() {
                public Void call() throws Exception {
                    executeContinueInstancePartnerRoleResponseReceived(prolemex);
                    return null;
                }
            });
        } catch (Exception ex) {
            throw new BpelEngineException(ex);
        }
    }

    public boolean isCleanupCategoryEnabled(boolean instanceSucceeded, ProcessConf.CLEANUP_CATEGORY category) {
        return _pconf.isCleanupCategoryEnabled(instanceSucceeded, category);
    }

    public Set<ProcessConf.CLEANUP_CATEGORY> getCleanupCategories(boolean instanceSucceeded) {
        return _pconf.getCleanupCategories(instanceSucceeded);
    }

    public Node getProcessProperty(QName propertyName) {
        Map<QName, Node> properties = _pconf.getProcessProperties();
        if (properties != null) {
            return properties.get(propertyName);
        }
        return null;
    }

}
