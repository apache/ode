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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.iapi.BpelEngine;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.utils.msg.MessageBundle;

/**
 * Implementation of the {@link BpelEngine} interface: provides the server
 * methods that should be invoked in the context of a transaction.
 * 
 * @author mszefler
 * 
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

    /** Active processes, keyed by process id. */
    final HashMap<QName, BpelProcess> _activeProcesses = new HashMap<QName, BpelProcess>();

    /** Mapping from myrole endpoint name to active process. */
    private final HashMap<Endpoint, BpelProcess> _serviceMap = new HashMap<Endpoint, BpelProcess>();

    final Contexts _contexts;

    public BpelEngineImpl(Contexts contexts) {
        _contexts = contexts;
    }

    public MyRoleMessageExchange createMessageExchange(String clientKey, QName targetService, String operation)
            throws BpelEngineException {

        MessageExchangeDAO dao = _contexts.dao.getConnection().createMessageExchange(
                MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE);
        dao.setCorrelationId(clientKey);
        dao.setCorrelationStatus(CorrelationStatus.UKNOWN_ENDPOINT.toString());
        dao.setPattern(MessageExchangePattern.UNKNOWN.toString());
        dao.setCallee(targetService);
        dao.setStatus(Status.NEW.toString());
        dao.setOperation(operation);
        MyRoleMessageExchangeImpl mex = new MyRoleMessageExchangeImpl(this, dao);

        BpelProcess target = route(targetService, null);
        if (target != null)
            target.initMyRoleMex(mex);

        return mex;
    }

    public MessageExchange getMessageExchange(String mexId) throws BpelEngineException {
        MessageExchangeDAO mexdao = _contexts.dao.getConnection().getMessageExchange(mexId);
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
            } else {
                OPartnerLink plink = (OPartnerLink) process._oprocess.getChild(mexdao.getPartnerLinkModelId());
                PortType ptype = plink.partnerRolePortType;
                Operation op = plink.getPartnerRoleOperation(mexdao.getOperation());
                // TODO: recover Partner's EPR
                mex = new PartnerRoleMessageExchangeImpl(this, mexdao, ptype, op, null,
                        plink.hasMyRole() ? process.getInitialMyRoleEPR(plink) : null,
                        process.getPartnerRoleChannel(plink));
            }
            break;
        case MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE:
            mex = new MyRoleMessageExchangeImpl(this, mexdao);
            if (process != null) {
                OPartnerLink plink = (OPartnerLink) process._oprocess.getChild(mexdao.getPartnerLinkModelId());
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

        if (process != null)
            mex.setProcess(process._oprocess);

        return mex;
    }

    boolean unregisterProcess(QName process) {
        BpelProcess p = _activeProcesses.remove(process);
        if (p != null) {
            if (__log.isDebugEnabled())
                __log.debug("Deactivating process " + p.getPID());

            p.deactivate();
            while (_serviceMap.values().remove(p))
                ;
        }
        return p != null;
    }

    boolean isProcessRegistered(QName pid) {
        return _activeProcesses.containsKey(pid);
    }

    /**
     * Register a process with the engine.
     * 
     * @param process
     *            the process to register
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
     * Route to a process using the service id. Note, that we do not need the
     * endpoint name here, we are assuming that two processes would not be
     * registered under the same service qname but different endpoint.
     * 
     * @param service
     *            target service id
     * @param request
     *            request message
     * @return process corresponding to the targetted service, or
     *         <code>null</code> if service identifier is not recognized.
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
        if (process == null) return null;
        else return process._oprocess;
    }

    public void onScheduledJob(String jobId, Map<String, Object> jobDetail) {
        WorkEvent we = new WorkEvent(jobDetail);

        ProcessInstanceDAO instance = _contexts.dao.getConnection().getInstance(we.getIID());
        if (instance == null) {
            __log.error(__msgs.msgScheduledJobReferencesUnknownInstance(we.getIID()));
            // nothing we can do, this instance is not in the database, it will
            // always
            // fail.
            return;
        }

        ProcessDAO processDao = instance.getProcess();
        BpelProcess process = _activeProcesses.get(processDao.getProcessId());
        if (process == null) {
            // If the process is not active, it means that we should not be
            // doing
            // any work on its behalf, therefore we will reschedule the events
            // for some time in the future (1 minute).
            Date future = new Date(System.currentTimeMillis() + (60 * 1000));
            __log.info(__msgs.msgReschedulingJobForInactiveProcess(processDao.getProcessId(), jobId, future));
            _contexts.scheduler.schedulePersistedJob(jobDetail, future);
        }

        assert process != null;
        process.handleWorkEvent(jobDetail);
        
        // Do a delay for debugging purposes.
        if (_delayMean != 0 )
        try {
            double u = _random.nextDouble();  // Uniform
            long delay  = (long)(-Math.log(u)*_delayMean); // Exponential distribution with mean _delayMean
            __log.warn("Debugging delay has been activated; delaying transaction for " + delay + "ms." );
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            ; // ignore
        } 

    }

    public MessageExchange getMessageExchangeByClientKey(String clientKey) {
        // TODO: implement me.
        throw new UnsupportedOperationException("Todo: implementme");
    }

    /**
     * Get the list of globally-registered message-exchange interceptors.
     * 
     * @return
     */
    List<MessageExchangeInterceptor> getGlobalInterceptors() {
        return _contexts.globalIntereceptors;
    }

}
