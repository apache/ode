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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.engine.BpelEngineImpl;
import org.apache.ode.bpel.engine.BpelProcess;
import org.apache.ode.bpel.engine.MyRoleMessageExchangeImpl;
import org.apache.ode.bpel.engine.PartnerLinkMyRoleImpl;
import org.apache.ode.bpel.engine.PartnerLinkMyRoleImpl.RoutingInfo;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.pmapi.CommunicationType;
import org.apache.ode.bpel.pmapi.ExchangeType;
import org.apache.ode.bpel.pmapi.CommunicationType.Exchange;
import org.apache.ode.bpel.runtime.PROCESS;

/**
 * Context holding replayer state (eg. invoke answers) for single instance during replaying.
 * 
 * @author Rafal Rusin
 *
 */
public class ReplayerContext {
    private static final Log __log = LogFactory.getLog(ReplayerContext.class);

    public ReplayerScheduler scheduler;

    public BpelEngineImpl bpelEngine;
    public ReplayerBpelRuntimeContextImpl runtimeContext;

    public final Date replayStartDate;

    public Answers answers = new Answers();

    public static class Answers {
        public Map<String, AnswersForKey> answersMap = new HashMap<String, AnswersForKey>();

        public static String getAnswersKey(QName service, String operation) {
            return service.toString() + ";" + operation;
        }

        public void add(Exchange e) {
            String key = getAnswersKey(e.getService(), e.getOperation());
            AnswersForKey v = answersMap.get(key);
            if (v == null) {
                v = new AnswersForKey();
                answersMap.put(key, v);
            }
            v.answers.add(e);
        }

        public Exchange fetchAnswer(QName service, String operation) {
            __log.debug("fetching answer for " + service + " " + operation);
            String key = getAnswersKey(service, operation);
            AnswersForKey v = answersMap.get(key);
            if (v == null) {
                throw new IllegalStateException("answer for " + service + " " + operation + " not found");
            }
            Exchange e = v.answers.get(v.answerPos);
            v.answerPos++;
            __log.debug("fetched " + e);
            return e;
        }

        public void remainingExchanges(List<Exchange> e) {
            for (AnswersForKey v : answersMap.values()) {
                v.remainingExchanges(e);
            }
        }
    }

    public static class AnswersForKey {
        List<Exchange> answers = new ArrayList<Exchange>();
        int answerPos = 0;

        public boolean isCompleted() {
            return !(answerPos < answers.size());
        }

        public void remainingExchanges(List<Exchange> e) {
            for (int i = answerPos; i < answers.size(); i++) {
                e.add(answers.get(i));
            }
        }

        @Override
        public String toString() {
            return new Integer(answerPos).toString() + " / " + answers.size();
        }
    }

    private void scheduleInvoke(final Exchange e, final MyRoleMessageExchangeImpl mex) {
        final Date time = e.getCreateTime().getTime();
        scheduler.scheduleReplayerJob(new Callable<Void>() {
            public Void call() throws Exception {
                __log.debug("call " + e);
                mex.getDAO().setStatus(Status.ASYNC.toString());
                runtimeContext.handleIncomingRequest(mex, time);
                return null;
            }
        }, time, runtimeContext);
    }

    public void init(CommunicationType r, ReplayerScheduler scheduler) throws Exception {
        this.scheduler = scheduler;
        List<Exchange> exchangeList = r.getExchangeList();

        for (int i = 1; i < exchangeList.size(); i++) {
            Exchange e = exchangeList.get(i);
            if (e.getType() == ExchangeType.P) {
                answers.add(e);
            }
        }

        {
            final Exchange e = exchangeList.get(0);
            final BpelProcess p = bpelEngine.getNewestProcessByType(r.getProcessType());
            final ProcessDAO processDAO = p.getProcessDAO();
            final MyRoleMessageExchangeImpl mex = ReplayerBpelRuntimeContextImpl.createMyRoleMex(e, bpelEngine);

            p.invokeProcess(mex, new BpelProcess.InvokeHandler() {
                public boolean invoke(PartnerLinkMyRoleImpl target, RoutingInfo routing, boolean createInstance) {
                    if (routing.messageRoute == null && createInstance) {
                        ProcessInstanceDAO newInstance = processDAO.createInstance(routing.correlator);

                        runtimeContext = new ReplayerBpelRuntimeContextImpl(p, newInstance, new PROCESS(p.getOProcess()), mex, ReplayerContext.this);
                        runtimeContext.setCurrentEventDateTime(e.getCreateTime().getTime());
                        runtimeContext.updateMyRoleMex(mex);
                        // first receive is matched to provided mex
                        runtimeContext.execute();
                        return true;
                    } else if (routing.messageRoute != null) {
                        throw new IllegalStateException("Instantiating mex causes invocation of existing instance " + mex);
                    }
                    return false;
                }
            });
        }

        for (int i = 1; i < exchangeList.size(); i++) {
            Exchange e = exchangeList.get(i);
            if (e.getType() == ExchangeType.M) {
                MyRoleMessageExchangeImpl mex = ReplayerBpelRuntimeContextImpl.createMyRoleMex(e, bpelEngine);
                runtimeContext.updateMyRoleMex(mex);
                scheduleInvoke(e, mex);
            }
        }
    }

    public void run() throws Exception {
        scheduler.startReplaying();
    }

    public ReplayerContext(Date replayStartDate) {
        super();
        this.replayStartDate = replayStartDate;
    }
}
