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
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.engine.BpelEngineImpl;
import org.apache.ode.bpel.iapi.BpelEngine;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.bpel.pmapi.CommunicationType;
import org.apache.ode.bpel.pmapi.ExchangeType;
import org.apache.ode.bpel.pmapi.FaultType;
import org.apache.ode.bpel.pmapi.GetCommunication;
import org.apache.ode.bpel.pmapi.GetCommunicationResponse;
import org.apache.ode.bpel.pmapi.Replay;
import org.apache.ode.bpel.pmapi.CommunicationType.Exchange;
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
    public ReplayerScheduler scheduler = new ReplayerScheduler();

    public List<Long> replayInstances(Replay request, BpelEngine engine, BpelDAOConnection conn) throws Exception {
        Date startDate = Calendar.getInstance().getTime();
        List<ReplayerContext> contexts = new ArrayList<ReplayerContext>();
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

        scheduler.startReplaying();
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

            __log.debug("---");
            __log.debug("" + mexDao.getCallee());
            __log.debug("" + mexDao.getChannel());
            __log.debug("" + mexDao.getCreateTime());
            __log.debug("" + mexDao.getEPR());
            __log.debug("" + mexDao.getPortType());

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
}
