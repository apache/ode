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
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.BpelEngine;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.Scheduler.JobInfo;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.utils.msg.MessageBundle;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the {@link BpelEngine} interface: provides the server methods that should be invoked in the context of a
 * transaction.
 *
 * @author mszefler
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class BpelEngineImpl implements BpelEngine {
    private static final Log __log = LogFactory.getLog(BpelEngineImpl.class);

    /** RNG, for delays */
    private Random _random = new Random(System.currentTimeMillis());

    private static double _delayMean = 0;

    static {
        try {
            String delay = System.getenv("ODE_DEBUG_TX_DELAY");
            if (delay != null && delay.length() > 0) {
                _delayMean = Double.valueOf(delay);
                __log.info("Stochastic debugging delay activated. Delay (Mean)=" + _delayMean + "ms.");
            }
        } catch (Throwable t) {
            if (__log.isDebugEnabled()) {
                __log.debug("Could not read ODE_DEBUG_TX_DELAY environment variable; assuming 0 (mean) delay", t);
            } else {
                __log.info("Could not read ODE_DEBUG_TX_DELAY environment variable; assuming 0 (mean) delay");
            }
        }
    }

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    /** Maximum number of tries for async jobs. */
    private static final int MAX_RETRIES = 3;

    /** Active processes, keyed by process id. */
    final HashMap<QName, BpelProcess> _activeProcesses = new HashMap<QName, BpelProcess>();

    /** Mapping from myrole endpoint name to active process. */
    private final HashMap<Endpoint, BpelProcess> _serviceMap = new HashMap<Endpoint, BpelProcess>();

    /** Manage instance-level locks. */
    private final InstanceLockManager _instanceLockManager = new InstanceLockManager();

    final Contexts _contexts;

    public BpelEngineImpl(Contexts contexts) {
        _contexts = contexts;
    }

    MyRoleMessageExchange createMessageExchange(InvocationStyle istyle, QName targetService, String operation, String clientKey)
            throws BpelEngineException {

        // TODO: for now, invocation of the engine is only supported in RELIABLE mode.
        if (istyle != InvocationStyle.RELIABLE)
            throw new BpelEngineException("Unsupported InvocationStyle: " + istyle);
        
        BpelProcess target = route(targetService, null);

        MessageExchangeDAO dao;
        if (target == null || target.isInMemory()) {
            dao = _contexts.inMemDao.getConnection().createMessageExchange(MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE);
        } else {
            dao = _contexts.dao.getConnection().createMessageExchange(MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE);
        }
        dao.setCorrelationId(clientKey);
        dao.setCorrelationStatus(CorrelationStatus.UKNOWN_ENDPOINT.toString());
        dao.setPattern(MessageExchangePattern.UNKNOWN.toString());
        dao.setCallee(targetService);
        dao.setStatus(Status.NEW.toString());
        dao.setOperation(operation);
        dao.setPipedMessageExchangeId(pipedMexId);
        ReliableMyRoleMessageExchangeImpl mex = new ReliableMyRoleMessageExchangeImpl(this, dao);

        if (target != null) {
            target.initMyRoleMex(mex);
        }

        return mex;
    }

    MessageExchange getMessageExchange(String mexId) throws BpelEngineException {
        MessageExchangeDAO mexdao = _contexts.inMemDao.getConnection().getMessageExchange(mexId);
        if (mexdao == null) mexdao = _contexts.dao.getConnection().getMessageExchange(mexId);
        if (mexdao == null)
            return null;

        ProcessDAO pdao = mexdao.getProcess();
        BpelProcess process = pdao == null ? null : _activeProcesses.get(pdao.getProcessId());

        MessageExchangeImpl mex;
        switch (mexdao.getDirection()) {
        case MessageExchangeDAO.DIR_BPEL_INVOKES_PARTNERROLE:
            if (process == null) {
                String errmsg = __msgs.msgProcessNotActive(pdao.getProcessId());
                __log.error(errmsg);
                // TODO: Perhaps we should define a checked exception for this
                // condition.
                throw new BpelEngineException(errmsg);
            }
            {
                OPartnerLink plink = (OPartnerLink) process.getOProcess().getChild(mexdao.getPartnerLinkModelId());
                PortType ptype = plink.partnerRolePortType;
                Operation op = plink.getPartnerRoleOperation(mexdao.getOperation());
                // TODO: recover Partner's EPR
                mex = new PartnerRoleMessageExchangeImpl(this, mexdao, ptype, op, null, plink.hasMyRole() ? process
                        .getInitialMyRoleEPR(plink) : null, process.getPartnerRoleChannel(plink));
            }
            break;
        case MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE:
            mex = new ReliableMyRoleMessageExchangeImpl(this, mexdao);
            if (process != null) {
                OPartnerLink plink = (OPartnerLink) process.getOProcess().getChild(mexdao.getPartnerLinkModelId());
                PortType ptype = plink.myRolePortType;
                Operation op = plink.getMyRoleOperation(mexdao.getOperation());
                mex.setPortOp(ptype, op);
            }
            break;
        default:
            String errmsg = "BpelEngineImpl: internal error, invalid MexDAO direction: " + mexId;
            __log.fatal(errmsg);
            throw new BpelEngineException(errmsg);
        }

        return mex;
    }

    BpelProcess unregisterProcess(QName process) {
        BpelProcess p = _activeProcesses.remove(process);
        if (p != null) {
            if (__log.isDebugEnabled())
                __log.debug("Deactivating process " + p.getPID());

            p.deactivate();
            while (_serviceMap.values().remove(p))
                ;
        }
        return p;
    }

    boolean isProcessRegistered(QName pid) {
        return _activeProcesses.containsKey(pid);
    }

    /**
     * Register a process with the engine.
     * @param process the process to register
     */
    void registerProcess(BpelProcess process) {
        _activeProcesses.put(process.getPID(), process);
        for (Endpoint e : process.getServiceNames()) {
            __log.debug("Register process: serviceId=" + e + ", process=" + process);
            _serviceMap.put(e, process);
        }
        process.activate(this);
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

        BpelProcess routed = null;
        for (Endpoint endpoint : _serviceMap.keySet()) {
            if (endpoint.serviceName.equals(service))
                routed = _serviceMap.get(endpoint);
        }
        if (__log.isDebugEnabled())
            __log.debug("Routed: svcQname " + service + " --> " + routed);
        return routed;

    }

    OProcess getOProcess(QName processId) {
        BpelProcess process = _activeProcesses.get(processId);

        if (process == null)
            return null;

        return process.getOProcess();
    }

    public void onScheduledJob(Scheduler.JobInfo jobInfo) throws Scheduler.JobProcessorException {
        final WorkEvent we = new WorkEvent(jobInfo.jobDetail);

        // We lock the instance to prevent concurrent transactions and prevent unnecessary rollbacks,
        // Note that we don't want to wait too long here to get our lock, since we are likely holding
        // on to scheduler's locks of various sorts.
        try {
            _instanceLockManager.lock(we.getIID(), 1, TimeUnit.MICROSECONDS);
            _contexts.scheduler.registerSynchronizer(new Scheduler.Synchronizer() {
                public void afterCompletion(boolean success) {
                    _instanceLockManager.unlock(we.getIID());
                }
                public void beforeCompletion() { }
            });
        } catch (InterruptedException e) {
            // Retry later.
            __log.debug("Thread interrupted, job will be rescheduled: " + jobInfo);
            throw new Scheduler.JobProcessorException(true);
        } catch (org.apache.ode.bpel.engine.InstanceLockManager.TimeoutException e) {
            __log.debug("Instance " + we.getIID() + " is busy, rescheduling job.");
            // TODO: This should really be more of something like the exponential backoff algorithm in ethernet.
            _contexts.scheduler.schedulePersistedJob(jobInfo.jobDetail, new Date(System.currentTimeMillis()
                    + Math.min(randomExp(1000), 10000)));
            return;
        }
        // DONT PUT CODE HERE-need this method real tight in a try/catch block, we need to handle
        // all types of failure here, the scheduler is not going to know how to handle our errors,
        // ALSO we have to release the lock obtained above (IMPORTANT), lest the whole system come
        // to a grinding halt.
        try {

            BpelProcess process;
            if (we.getProcessId() != null) {
                process = _activeProcesses.get(we.getProcessId());
            } else {
                ProcessInstanceDAO instance;
                if (we.isInMem()) instance = _contexts.inMemDao.getConnection().getInstance(we.getIID());
                else instance = _contexts.dao.getConnection().getInstance(we.getIID());

                if (instance == null) {
                    __log.error(__msgs.msgScheduledJobReferencesUnknownInstance(we.getIID()));
                    // nothing we can do, this instance is not in the database, it will
                    // always
                    // fail.
                    return;
                }
                ProcessDAO processDao = instance.getProcess();
                process = _activeProcesses.get(processDao.getProcessId());
            }

            if (process == null) {
                // If the process is not active, it means that we should not be
                // doing any work on its behalf, therefore we will reschedule the
                // events for some time in the future (1 minute).
                Date future = new Date(System.currentTimeMillis() + (60 * 1000));
                __log.info(__msgs.msgReschedulingJobForInactiveProcess(we.getProcessId(), jobInfo.jobName, future));
                _contexts.scheduler.schedulePersistedJob(jobInfo.jobDetail, future);
                return;
            }

            process.handleWorkEvent(jobInfo.jobDetail);
            debuggingDelay();
        } catch (BpelEngineException bee) {
            __log.error(__msgs.msgScheduledJobFailed(we.getDetail()), bee);
            throw new Scheduler.JobProcessorException(bee, checkRetry(jobInfo, bee));
        } catch (ContextException ce) {
            __log.error(__msgs.msgScheduledJobFailed(we.getDetail()), ce);
            throw new Scheduler.JobProcessorException(ce, checkRetry(jobInfo, ce));
        } catch (RuntimeException rte) {
            __log.error(__msgs.msgScheduledJobFailed(we.getDetail()), rte);
            throw new Scheduler.JobProcessorException(rte, checkRetry(jobInfo, rte));
        } catch (Throwable t) {
            __log.error(__msgs.msgScheduledJobFailed(we.getDetail()), t);
            throw new Scheduler.JobProcessorException(false);

        }
    }

    private boolean checkRetry(final JobInfo jobInfo, Throwable t) {
        // TODO, better handling of failed jobs (put them in the DB perhaps?)
        if (jobInfo.retryCount < MAX_RETRIES)
            return true;

        __log.error("Job could not be completed after " + MAX_RETRIES + ": " + jobInfo, t);

        boolean saveToDisk = false;
        if (jobInfo.jobDetail.get("final") == null) {
            __log.error("Rescheduling problematic job for a bit later: " + jobInfo, t);

            try {
                if (jobInfo.jobDetail.get("inmem") != null)
                    _contexts.scheduler.scheduleVolatileJob(true, jobInfo.jobDetail);
                else
                    _contexts.scheduler.execIsolatedTransaction(new Callable<Void>() {
                        public Void call() throws Exception {
                            jobInfo.jobDetail.put("final", true);
                            _contexts.scheduler.schedulePersistedJob(jobInfo.jobDetail,
                                    new Date(System.currentTimeMillis() + 60 * 1000));
                            return null;
                        }
                    });
            } catch (Exception ex) {
                __log.error("Error rescheduling problematic job: " + jobInfo,ex);
                saveToDisk = true;
            }
        } else {
            saveToDisk = true;
        }

        if (saveToDisk)
            try {
                File f = File.createTempFile("ode-bad-job", ".ser", new File(""));
                ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream(f));
                fos.writeObject(jobInfo);
                fos.close();
                __log.error("Saved problematic job to disk (last resort): " + jobInfo +" in file " + f);
            } catch (Exception ex) {
                __log.error("Could not save bad job; it will be lost: " + jobInfo, ex);
            }


        // No more retries.
        return false;
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

    void fireEvent(BpelEvent event) {
        // Note that the eventListeners list is a copy-on-write array, so need
        // to mess with synchronization.
        for (org.apache.ode.bpel.iapi.BpelEventListener l : _contexts.eventListeners) {
            l.onEvent(event);
        }
    }

    /**
     * Get the list of globally-registered message-exchange interceptors.
     *
     * @return list
     */
    List<MessageExchangeInterceptor> getGlobalInterceptors() {
        return _contexts.globalIntereceptors;
    }

}
