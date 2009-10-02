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
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import net.sf.saxon.xqj.SaxonXQConnection;
import net.sf.saxon.xqj.SaxonXQDataSource;

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
import org.apache.ode.bpel.pmapi.MockQueryRequestDocument;
import org.apache.ode.bpel.pmapi.MockQueryResponseDocument;
import org.apache.ode.bpel.pmapi.ResponseType;
import org.apache.ode.bpel.pmapi.CommunicationType.Exchange;
import org.apache.ode.bpel.pmapi.CommunicationType.ServiceConfig;
import org.apache.ode.bpel.runtime.PROCESS;
import org.apache.ode.utils.DOMUtils;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
    
    public Map<QName, ServiceConfig> servicesConfig = new HashMap<QName, ServiceConfig>();

    public final Date replayStartDate;

    public Answers answers = new Answers();

    public class Answers {
        public Map<String, AnswersForKey> answersMap = new HashMap<String, AnswersForKey>();

        public String getAnswersKey(QName service, String operation) {
            return service.toString() + ";" + operation;
        }

        public void add(Exchange e) {
            ServiceConfig cfg = getServiceConfig(e.getService());
            if (cfg.getReplayType().isSetMock()) {
                String key = getAnswersKey(e.getService(), e.getOperation());
                AnswersForKey v = answersMap.get(key);
                if (v == null) {
                    v = new AnswersForKey();
                    answersMap.put(key, v);
                }
                v.answers.add(e);
            }
        }

        public Exchange fetchAnswer(QName service, String operation, Element outgoingMessage, Date currentEventDateTime) {
            __log.debug("fetching answer for " + service + " " + operation);
            
            ServiceConfig cfg = getServiceConfig(service);
            
            if (cfg.getReplayType().isSetMock()) {
                String key = getAnswersKey(service, operation);
                AnswersForKey v = answersMap.get(key);
                Exchange e = v == null ? null : v.answerPos < v.answers.size() ? v.answers.get(v.answerPos) : null;
                if (e == null) {
                    throw new IllegalStateException("answer for " + service + " " + operation + " at time " + currentEventDateTime + " not found, outgoing message was " + DOMUtils.domToString(outgoingMessage));
                }
                v.answerPos++;
                __log.debug("fetched " + e);
                return e;
            } else if (cfg.getReplayType().isSetMockQuery()) {
                return fetchMockQuery(service, operation, outgoingMessage, cfg);
            } else assert(false);
            return null;
        }

        public void remainingExchanges(List<Exchange> e) {
            for (AnswersForKey v : answersMap.values()) {
                v.remainingExchanges(e);
            }
        }
        
        private Exchange fetchMockQuery(QName service, String operation, Element outgoingMessage, org.apache.ode.bpel.pmapi.CommunicationType.ServiceConfig serviceConfig) {
            try {
                MockQueryRequestDocument request = MockQueryRequestDocument.Factory.newInstance();
                request.addNewMockQueryRequest().addNewIn().set(XmlObject.Factory.parse(outgoingMessage));
                String xquery = serviceConfig.getReplayType().getMockQuery();
                
                XQDataSource xqds = new SaxonXQDataSource();
                XQConnection xqconn = xqds.getConnection();
    
                net.sf.saxon.Configuration configuration = ((SaxonXQConnection) xqconn).getConfiguration();
                configuration.setHostLanguage(net.sf.saxon.Configuration.XQUERY);
//                XQStaticContext staticEnv = xqconn.getStaticContext();
                XQPreparedExpression exp = xqconn.prepareExpression(xquery);
                Node requestNode = DOMUtils.parse(request.newXMLStreamReader());
                if (__log.isDebugEnabled()) {
                    __log.debug("request " + request.toString());
                }
                exp.bindItem(XQConstants.CONTEXT_ITEM, xqconn.createItemFromNode(requestNode, xqconn.createNodeType()));
                XQResultSequence result = exp.executeQuery();
                MockQueryResponseDocument response = MockQueryResponseDocument.Factory.parse(result.getSequenceAsStream());
                {
                    XmlOptions opts = new XmlOptions();
                    List<Object> errors = new ArrayList<Object>();
                    opts.setErrorListener(errors);
                    if (!response.validate(opts)) {
                        __log.error("MockQuery response doesn't validate. Errors: " + errors + " Request: " + request.toString() + " Response: " + response.toString(), new Exception());
                        throw new IllegalStateException("MockQuery response doesn't validate.");
                    }
                }
                ResponseType response2 = response.getMockQueryResponse();
                
                if (__log.isDebugEnabled()) {
                    __log.debug("mockQuery result " + response);
                }
                
                
                Exchange answer = Exchange.Factory.newInstance();
                {
                    if (response2.isSetOut()) {
                        answer.setOut(response2.getOut());
                    }
                    if (response2.isSetFault()) {
                        answer.setFault(response2.getFault());
                    }
                    if (response2.isSetFailure()) {
                        answer.setFailure(response2.getFailure());
                    }
                }
                
                return answer;
            } catch (Exception e) {
                __log.error("", e);
                __log.error(e.getCause());
                throw new IllegalStateException(e.getMessage());
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

    public void init(final CommunicationType r, ReplayerScheduler scheduler) throws Exception {
        this.scheduler = scheduler;
        
        for (ServiceConfig s : r.getServiceConfigList()) {
            servicesConfig.put(s.getService(), s);
        }
        
        final List<Exchange> exchangeList = r.getExchangeList();

        for (int i = 1; i < exchangeList.size(); i++) {
            Exchange e = exchangeList.get(i);
            // We skip failures, because INVOKE_CHECK job is not handled by
            // replayer
            if (e.getType() == ExchangeType.P && !e.isSetFailure()) {
                answers.add(e);
            }
        }

        {
            final Exchange e = exchangeList.get(0);

            final Date time = e.getCreateTime().getTime();
            scheduler.scheduleReplayerJob(new Callable<Void>() {
                public Void call() throws Exception {
                    __log.debug("initial call " + e);

                    final BpelProcess p = bpelEngine.getNewestProcessByType(r.getProcessType());
                    final ProcessDAO processDAO = p.getProcessDAO();
                    final MyRoleMessageExchangeImpl mex = ReplayerBpelRuntimeContextImpl.createMyRoleMex(e, bpelEngine);

                    p.invokeProcess(mex,
                    // time,
                            new BpelProcess.InvokeHandler() {
                                public boolean invoke(PartnerLinkMyRoleImpl target, RoutingInfo routing, boolean createInstance) {
                                    if (routing.messageRoute == null && createInstance) {
                                        ProcessInstanceDAO newInstance = processDAO.createInstance(routing.correlator);

                                        runtimeContext = new ReplayerBpelRuntimeContextImpl(p, newInstance, new PROCESS(p.getOProcess()), mex,
                                        // time,
                                                ReplayerContext.this);
                                        runtimeContext.setCurrentEventDateTime(time);
                                        runtimeContext.updateMyRoleMex(mex);
                                        // first receive is matched to provided
                                        // mex
                                        runtimeContext.execute();
                                        return true;
                                    } else if (routing.messageRoute != null) {
                                        throw new IllegalStateException("Instantiating mex causes invocation of existing instance " + mex);
                                    }
                                    return false;
                                }
                            });

                    for (int i = 1; i < exchangeList.size(); i++) {
                        Exchange e2 = exchangeList.get(i);
                        if (e2.getType() == ExchangeType.M) {
                            MyRoleMessageExchangeImpl mex2 = ReplayerBpelRuntimeContextImpl.createMyRoleMex(e2, bpelEngine);
                            runtimeContext.updateMyRoleMex(mex2);
                            scheduleInvoke(e2, mex2);
                        }
                    }
                    return null;
                }
            }, time, null);
        }

    }

    public void run() throws Exception {
        scheduler.startReplaying();
    }

    public ReplayerContext(Date replayStartDate) {
        super();
        this.replayStartDate = replayStartDate;
    }
    
    public ServiceConfig getServiceConfig(QName service) {
        ServiceConfig c = servicesConfig.get(service);
        if (c == null) {
            c = ServiceConfig.Factory.newInstance();
            c.setService(service);
            c.addNewReplayType().setMock(XmlAnySimpleType.Factory.newInstance());
            return c;
        } else return c;
    }
}
