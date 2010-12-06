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
package org.apache.ode.bpel.engine.replayer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.engine.BpelEngineImpl;
import org.apache.ode.bpel.engine.BpelProcess;
import org.apache.ode.bpel.engine.MyRoleMessageExchangeImpl;
import org.apache.ode.bpel.engine.PartnerLinkMyRoleImpl;
import org.apache.ode.bpel.engine.PartnerLinkMyRoleImpl.RoutingInfo;
import org.apache.ode.bpel.evt.CorrelationMatchEvent;
import org.apache.ode.bpel.iapi.BpelEngine;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.bpel.iapi.Scheduler.JobType;
import org.apache.ode.bpel.pmapi.CommunicationType;
import org.apache.ode.bpel.pmapi.ExchangeType;
import org.apache.ode.bpel.pmapi.FaultType;
import org.apache.ode.bpel.pmapi.GetCommunication;
import org.apache.ode.bpel.pmapi.GetCommunicationResponse;
import org.apache.ode.bpel.pmapi.Replay;
import org.apache.ode.bpel.pmapi.CommunicationType.Exchange;
import org.apache.ode.bpel.runtime.PROCESS;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * Main class used for replaying. It's invoked from InstanceManagement API.
 * Receives request and sets up replaying contexts for each instance to replay.
 *
 * @author Rafal Rusin
 *
 */
public class Replayer {
    private static final Log __log = LogFactory.getLog(Replayer.class);
    public static ThreadLocal<Replayer> replayer = new ThreadLocal<Replayer>();
    public ReplayerScheduler scheduler = new ReplayerScheduler();
    public BpelEngineImpl engine = null;
    public List<ReplayerContext> contexts = null;
    public BpelDAOConnection conn = null;

    public List<Long> replayInstances(Replay request, BpelEngine engine, BpelDAOConnection conn) throws Exception {
        try {
            replayer.set(this);
            this.engine = (BpelEngineImpl) engine;
            this.conn = conn;

            Date startDate = Calendar.getInstance().getTime();
            contexts = new ArrayList<ReplayerContext>();
            {
                List<Long> toDelete = new ArrayList<Long>();
                List<CommunicationType> toRestore = new ArrayList<CommunicationType>();

                toDelete.addAll(request.getReplaceInstanceList());

                for (Long iid : request.getUpgradeInstanceList()) {
                    toDelete.add(iid);
                    toRestore.add(CommunicationType.Factory.parse(getCommunication(iid, conn).toString()));
                }
                toRestore.addAll(request.getRestoreInstanceList());

                {
                    Set<CLEANUP_CATEGORY> cleanupCategory = new HashSet<CLEANUP_CATEGORY>();
                    cleanupCategory.add(CLEANUP_CATEGORY.INSTANCE);
                    cleanupCategory.add(CLEANUP_CATEGORY.MESSAGES);
                    cleanupCategory.add(CLEANUP_CATEGORY.VARIABLES);
                    cleanupCategory.add(CLEANUP_CATEGORY.CORRELATIONS);
                    cleanupCategory.add(CLEANUP_CATEGORY.EVENTS);

                    for (Long l : toDelete) {
                        conn.getInstance(l).delete(cleanupCategory);
                    }
                }

                for (CommunicationType r : toRestore) {
                    ReplayerContext context = new ReplayerContext(startDate);
                    context.bpelEngine = (BpelEngineImpl) engine;
                    context.init(r, scheduler);
                    contexts.add(context);
                }
            }

            scheduler.startReplaying(this);
            {
                List<Exchange> remainingExchanges = new ArrayList<Exchange>();

                for (ReplayerContext c : contexts) {
                    c.answers.remainingExchanges(remainingExchanges);
                }
                if (remainingExchanges.size() > 0) {
                    throw new RemainingExchangesException(remainingExchanges);
                }
            }

            List<Long> r = new ArrayList<Long>();
            for (ReplayerContext c : contexts) {
                r.add(c.runtimeContext.getPid());
            }

            return r;
        } finally {
            replayer.set(null);
        }
    }

    public GetCommunicationResponse getCommunication(GetCommunication request, BpelDAOConnection conn) throws Exception {
        GetCommunicationResponse response = GetCommunicationResponse.Factory.newInstance();
        for (Long iid : request.getIidList()) {
            response.addNewRestoreInstance().set(getCommunication(iid, conn));
        }
        return response;
    }

    private CommunicationType getCommunication(Long iid, BpelDAOConnection conn) {
        CommunicationType result = CommunicationType.Factory.newInstance();
        List<Exchange> list = new ArrayList<Exchange>();
        ProcessInstanceDAO instance = conn.getInstance(iid);
        if (instance == null)
            return result;
        result.setProcessType(instance.getProcess().getType());

        for (String mexId : instance.getMessageExchangeIds()) {
            MessageExchangeDAO mexDao = conn.getMessageExchange(mexId);

            Exchange e = Exchange.Factory.newInstance();
            list.add(e);
            e.setCreateTime(new XmlCalendar(mexDao.getCreateTime()));
            e.setOperation(mexDao.getOperation());
            try {
                e.setIn(XmlObject.Factory.parse(mexDao.getRequest().getData()));
            } catch (XmlException e1) {
                __log.error("", e1);
            }
            try {
                Status status = Status.valueOf(mexDao.getStatus());
                if (status == Status.FAULT) {
                    FaultType f = e.addNewFault();
                    f.setType(mexDao.getFault());
                    f.setExplanation(mexDao.getFaultExplanation());
                    if (mexDao.getResponse() != null) {
                        f.setMessage(XmlObject.Factory.parse(mexDao.getResponse().getData()));
                    }
                } else if (status == Status.FAILURE) {
                    e.addNewFailure().setExplanation(mexDao.getFaultExplanation());
                } else {
                    if (mexDao.getResponse() != null) {
                        e.setOut(XmlObject.Factory.parse(mexDao.getResponse().getData()));
                    }
                }
            } catch (XmlException e1) {
                __log.error("", e1);
            }
            e.setType(ExchangeType.Enum.forString("" + mexDao.getDirection()));

            if (__log.isDebugEnabled()) {
                __log.debug("---");
                __log.debug("" + mexDao.getCallee());
                __log.debug("" + mexDao.getChannel());
                __log.debug("" + mexDao.getCreateTime());
                __log.debug("" + mexDao.getEPR());
                __log.debug("" + mexDao.getPortType());
            }
            
            if (e.getType() == ExchangeType.P) {
                e.setService(mexDao.getPortType());
            } else {
                e.setService(mexDao.getCallee());
            }
        }

        Collections.sort(list, new Comparator<Exchange>() {
            public int compare(Exchange arg0, Exchange arg1) {
                return arg0.getCreateTime().compareTo(arg1.getCreateTime());
            }
        });

        for (Exchange e : list) {
            result.addNewExchange().set(e);
        }
        return result;
    }

    public ReplayerContext findReplayedInstance(long iid) {
        for (ReplayerContext r : contexts) {
            if (r.runtimeContext.getPid() == iid) {
                return r;
            }
        }
        return null;
    }

    public void handleJobDetails(JobDetails jobDetail, final Date when) {
        JobDetails we = jobDetail;
        __log.debug("handleJobDetails " + jobDetail + " " + when);
        if (we.getType() == JobType.INVOKE_INTERNAL) {
            final BpelProcess p = engine._activeProcesses.get(we.getProcessId());
            final ProcessDAO processDAO = p.getProcessDAO();
            final MyRoleMessageExchangeImpl mex = (MyRoleMessageExchangeImpl) engine.getMessageExchange(we.getMexId());

            p.invokeProcess(mex,
            // time,
                    new BpelProcess.InvokeHandler() {
                        public boolean invoke(PartnerLinkMyRoleImpl target, RoutingInfo routing, boolean createInstance) {
                            if (routing.messageRoute == null && createInstance) {
                                __log.debug("creating new instance via live communication mex:" + mex);
                                ProcessInstanceDAO newInstance = processDAO.createInstance(routing.correlator);

                                ReplayerContext context = new ReplayerContext(null);
                                context.bpelEngine = (BpelEngineImpl) engine;
                                contexts.add(context);

                                ReplayerBpelRuntimeContextImpl runtimeContext = new ReplayerBpelRuntimeContextImpl(p, newInstance, new PROCESS(p.getOProcess()), mex,
                                // time,
                                        context);
                                context.runtimeContext = runtimeContext;
                                runtimeContext.setCurrentEventDateTime(when);
                                runtimeContext.updateMyRoleMex(mex);
                                // first receive is matched to provided
                                // mex
                                runtimeContext.execute();
                                return true;
                            } else if (routing.messageRoute != null) {
                                long iid = routing.messageRoute.getTargetInstance().getInstanceId();
                                ReplayerContext ctx = findReplayedInstance(iid);
                                if (ctx == null) {
                                    throw new IllegalStateException("Trying to hit existing instance via live communication, but there's no such instance mex:" + mex + " iid:" + iid);
                                }
                                __log.debug("hitting existing instance via live communication mex:" + mex + " iid:" + iid);

                                ctx.runtimeContext.inputMsgMatch(routing.messageRoute.getGroupId(), routing.messageRoute.getIndex(), mex);
                                routing.correlator.removeRoutes(routing.messageRoute.getGroupId(), ctx.runtimeContext.getDAO());

                                mex.setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.MATCHED);
                                mex.getDAO().setInstance(routing.messageRoute.getTargetInstance());
                                ctx.runtimeContext.execute();
                            }
                            return false;
                        }
                    }, true);
        } else if (we.getType() == JobType.INVOKE_RESPONSE) {
            __log.debug("reply for live communication");
            ReplayerContext ctx = findReplayedInstance(we.getInstanceId());
            assert ctx != null;
            ctx.runtimeContext.invocationResponse(we.getMexId(), we.getChannel());
            ctx.runtimeContext.execute();
        }
    }
}
