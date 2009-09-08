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

import java.util.Date;
import java.util.concurrent.Callable;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.engine.BpelEngineImpl;
import org.apache.ode.bpel.engine.BpelProcess;
import org.apache.ode.bpel.engine.BpelRuntimeContextImpl;
import org.apache.ode.bpel.engine.MessageImpl;
import org.apache.ode.bpel.engine.MyRoleMessageExchangeImpl;
import org.apache.ode.bpel.engine.PartnerLinkMyRoleImpl;
import org.apache.ode.bpel.engine.PartnerLinkMyRoleImpl.RoutingInfo;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.pmapi.CommunicationType.Exchange;
import org.apache.ode.bpel.runtime.PROCESS;
import org.apache.ode.bpel.runtime.PartnerLinkInstance;
import org.apache.ode.bpel.runtime.Selector;
import org.apache.ode.bpel.runtime.channels.InvokeResponseChannel;
import org.apache.ode.bpel.runtime.channels.PickResponseChannel;
import org.apache.ode.bpel.runtime.channels.TimerResponseChannel;
import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class intercepts invocations on BpelRuntimeContextImpl and substitutes them as necessary during replaying. 
 * For exaple when INVOKE activity calls invoke on BpelRuntimeContextImpl then ReplayerBpelRuntimeContextImpl intercepts this call
 * and provides specific answer.
 * 
 * @author Rafal Rusin
 *
 */
public class ReplayerBpelRuntimeContextImpl extends BpelRuntimeContextImpl {
    private static final Log __log = LogFactory.getLog(ReplayerBpelRuntimeContextImpl.class);

    private ReplayerContext replayerContext;

    public ReplayerBpelRuntimeContextImpl(BpelProcess bpelProcess, ProcessInstanceDAO dao, PROCESS PROCESS, MyRoleMessageExchangeImpl instantiatingMessageExchange, ReplayerContext context) {
        super(bpelProcess, dao, PROCESS, instantiatingMessageExchange);
        this.replayerContext = context;
    }

    @Override
    public void cancel(TimerResponseChannel timerResponseChannel) {
        __log.debug("cancel " + timerResponseChannel.export());
        super.cancel(timerResponseChannel);
    }

    @Override
    public String invoke(int aid, PartnerLinkInstance partnerLink, Operation operation, Element outgoingMessage, InvokeResponseChannel channel) throws FaultException {
        __log.debug("invoke");

        Exchange answer = replayerContext.answers.fetchAnswer(partnerLink.partnerLink.partnerRolePortType.getQName(), operation.getName());

        PartnerLinkDAO plinkDAO = fetchPartnerLinkDAO(partnerLink);

        MessageExchangeDAO mexDao = _dao.getConnection().createMessageExchange(MessageExchangeDAO.DIR_BPEL_INVOKES_PARTNERROLE);

        mexDao.setCreateTime(new Date(getCurrentEventDateTime().getTime() + 1));
        mexDao.setOperation(operation.getName());
        mexDao.setPortType(partnerLink.partnerLink.partnerRolePortType.getQName());
        mexDao.setPartnerLinkModelId(partnerLink.partnerLink.getId());
        mexDao.setPartnerLink(plinkDAO);
        mexDao.setPattern((operation.getOutput() != null ? MessageExchangePattern.REQUEST_RESPONSE : MessageExchangePattern.REQUEST_ONLY).toString());
        mexDao.setProcess(_dao.getProcess());
        mexDao.setInstance(_dao);
        {
            MessageDAO request = mexDao.createMessage(new QName("replayer", "replayer"));
            request.setData(outgoingMessage);
            mexDao.setRequest(request);
        }

        if (mexDao.getPattern().equals(MessageExchangePattern.REQUEST_RESPONSE.toString())) {
            if (answer.isSetFault()) {
                MessageDAO response = mexDao.createMessage(new QName("replayer", "replayer"));
                try {
                    assign(response, answer.getFault());
                } catch (Exception e) {
                    throw new FaultException(new QName("replayer", "replayer"), e);
                }
                mexDao.setResponse(response);
                mexDao.setFault(answer.getFault().getType());
                mexDao.setFaultExplanation(answer.getFault().getExplanation());
                mexDao.setStatus(Status.FAULT.toString());

            } else if (answer.isSetOut()) {
                MessageDAO response = mexDao.createMessage(new QName("replayer", "replayer"));
                try {
                    assign(response, answer.getOut());
                } catch (Exception e) {
                    throw new FaultException(new QName("replayer", "replayer"), e);
                }
                mexDao.setResponse(response);
                mexDao.setStatus(Status.RESPONSE.toString());
            } else {
                // We don't have output for in-out operation - resulting with
                // replayer error to the top
                throw new IllegalStateException("I don't have response for invoke " + answer);
            }
            invocationResponse(mexDao.getMessageExchangeId(), channel.export());
        } else {
            // in only - continuing
            mexDao.setStatus(Status.COMPLETED_OK.toString());
        }

        return mexDao.getMessageExchangeId();
    }

    public static class TimerResume extends JacobRunnable {
        private static final long serialVersionUID = 198476512L;

        private final String channelId;

        public TimerResume(String channelId) {
            super();
            this.channelId = channelId;
        }

        @Override
        public void run() {
            importChannel(channelId, TimerResponseChannel.class).onTimeout();
        }
    }

    @Override
    public void registerTimer(final TimerResponseChannel timerChannel, final Date timeToFire) {
        __log.debug("register timer " + timerChannel + " " + timeToFire);
        final String channel = timerChannel.export();

        if (timeToFire.before(replayerContext.replayStartDate)) {
            replayerContext.scheduler.scheduleReplayerJob(new Callable() {
                public Object call() throws Exception {
                    __log.debug("executing timer resume " + timerChannel + " " + timeToFire);
                    timerEvent(channel);
                    return null;
                }
            }, timeToFire, this);
        } else {
            super.registerTimer(timerChannel, timeToFire);
        }
    }

    @Override
    public void reply(PartnerLinkInstance plinkInstnace, String opName, String mexId, Element msg, QName fault) throws FaultException {
        String mexRef = _outstandingRequests.release(plinkInstnace, opName, mexId);

        if (mexRef == null) {
            throw new FaultException(_bpelProcess.getOProcess().constants.qnMissingRequest);
        }

        MessageExchangeDAO mex = _dao.getConnection().getMessageExchange(mexRef);

        MessageDAO message = mex.createMessage(plinkInstnace.partnerLink.getMyRoleOperation(opName).getOutput().getMessage().getQName());
        buildOutgoingMessage(message, msg);

        __log.debug("reply mexRef:" + mexRef);
        mex.setResponse(message);
        mex.setStatus(Status.RESPONSE.toString());
    }

    @Override
    public void select(PickResponseChannel pickResponseChannel, Date timeout, boolean createInstance, Selector[] selectors) throws FaultException {
        super.select(pickResponseChannel, timeout, createInstance, selectors);
        __log.debug("select " + pickResponseChannel + " " + ObjectPrinter.toString(selectors, selectors));
    }

    public ProcessInstanceDAO getDAO() {
        return _dao;
    }

    public static MyRoleMessageExchangeImpl createMyRoleMex(Exchange e, BpelEngineImpl engine) throws Exception {
        MyRoleMessageExchangeImpl mex = (MyRoleMessageExchangeImpl) engine.createMessageExchange(new GUID().toString(), e.getService(), e.getOperation());
        mex.getDAO().setCreateTime(e.getCreateTime().getTime());

        MessageImpl m2 = (MessageImpl) mex.createMessage(new QName("replayer", "replayer"));
        assign(m2._dao, e.getIn());
        mex.getDAO().setRequest(m2._dao);
        mex.getDAO().setStatus(Status.REQUEST.toString());
        return mex;
    }

    public void updateMyRoleMex(MyRoleMessageExchangeImpl m) {
        m.getDAO().setProcess(_dao.getProcess());
        m.getDAO().setInstance(_dao);
    }

    public static void assign(MessageDAO m, XmlObject o) throws Exception {
        NodeList nodes = DOMUtils.parse(o.newInputStream(new XmlOptions().setSaveOuter())).getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n instanceof Element) {
                m.setData((Element) n);
            }
        }
    }

    public void handleIncomingRequest(final MyRoleMessageExchangeImpl mex, final Date currentEventDateTime) {
        __log.debug("handleIncomingRequest " + mex);

        setCurrentEventDateTime(currentEventDateTime);

        _bpelProcess.invokeProcess(mex, new BpelProcess.InvokeHandler() {
            public boolean invoke(PartnerLinkMyRoleImpl target, RoutingInfo routing, boolean createInstance) {
                if (routing.messageRoute == null && createInstance) {
                    // No route but we can create a new instance
                    throw new IllegalStateException("Mex caused creation of new instance " + mex);
                } else if (routing.messageRoute != null) {
                    if (!routing.messageRoute.getTargetInstance().getInstanceId().equals(_dao.getInstanceId())) {
                        throw new IllegalStateException("Routed target instance is not equal to replayed instance");
                    }
                    // Found a route, hitting it
                    inputMsgMatch(routing.messageRoute.getGroupId(), routing.messageRoute.getIndex(), mex);

                    // Kill the route so some new message does not get routed to
                    // same process instance.
                    routing.correlator.removeRoutes(routing.messageRoute.getGroupId(), _dao);
                    execute();
                    return true;
                }
                return false;
            }
        });

    }

}
