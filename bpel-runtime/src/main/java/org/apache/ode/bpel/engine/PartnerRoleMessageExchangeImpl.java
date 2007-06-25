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
import org.apache.ode.bpel.engine.WorkEvent.Type;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.w3c.dom.Element;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

class PartnerRoleMessageExchangeImpl extends MessageExchangeImpl implements PartnerRoleMessageExchange {
    private static final Log LOG = LogFactory.getLog(PartnerRoleMessageExchangeImpl.class);

    private final PartnerRoleChannel _channel;
    private EndpointReference _myRoleEPR;
    private boolean _inMem;

    private QName _caller;
    
    PartnerRoleMessageExchangeImpl(
            BpelEngineImpl engine, 
            PortType portType,
            Operation operation, 
            boolean inMem,
            EndpointReference epr,
            EndpointReference myRoleEPR,
            PartnerRoleChannel channel) {
        super(engine);
        _myRoleEPR = myRoleEPR;
        _channel = channel;
        _inMem = inMem;
        setPortOp(portType, operation);    
    }

    @Override
    void load(MessageExchangeDAO dao) {
        super.load(dao);
    }

    @Override
    public void save(MessageExchangeDAO dao) {
        super.save(dao);
    }

    public void replyOneWayOk() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("replyOneWayOk mex=" + getMessageExchangeId());
        }
        setStatus(Status.ASYNC);
    }

    public void replyAsync() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("replyAsync mex=" + getMessageExchangeId());
        }
        setStatus(Status.ASYNC);
    }

    public void replyWithFault(QName faultType, Message outputFaultMessage) throws BpelEngineException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("replyWithFault mex=" + getMessageExchangeId());
        }
        boolean isAsync = isAsync();
        setFault(faultType, outputFaultMessage);
        if (isAsync)
            continueAsync();
    }

    public void reply(Message response) throws BpelEngineException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reply mex=" + getMessageExchangeId());
        }
        boolean isAsync = isAsync();
        setResponse(response);
        if (isAsync)
            continueAsync();

    }

    public void replyWithFailure(FailureType type, String description, Element details) throws BpelEngineException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("replyWithFailure mex=" + getMessageExchangeId());
        }
        boolean isAsync = isAsync();
        setFailure(type, description, details);
        if (isAsync)
            continueAsync();
    }

    /**
     * Continue from the ASYNC state.
     * 
     */
    private void continueAsync() {
        // If there is no channel waiting for us, there is nothing to do.
        if (getDAO().getChannel() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("no channel on mex=" + getMessageExchangeId());
            }
            return;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("create work event for mex=" + getMessageExchangeId());
        }
        WorkEvent we = new WorkEvent();
        we.setIID(_iid);
        we.setType(Type.INVOKE_RESPONSE);
        if (_inMem)
            we.setInMem(true);
        we.setChannel(getDAO().getChannel());
        we.setMexId(_mexId);
        if (_inMem)
            _contexts.scheduler.scheduleVolatileJob(true, we.getDetail());
        else
            _contexts.scheduler.schedulePersistedJob(we.getDetail(), null);
    }

    /**
     * Check if we are in the ASYNC state.
     * 
     * @return
     */
    private boolean isAsync() {
        return getStatus() == Status.ASYNC;
    }

    public QName getCaller() {
        return _caller;
    }

    public String toString() {
        try {
            return "{PartnerRoleMex#" + _mexId  + " [PID " + getCaller() + "] calling " + _epr + "."
                    + _opname + "(...)}";

        } catch (Throwable t) {
            return "{PartnerRoleMex#????}";
        }

    }

    public PartnerRoleChannel getChannel() {
        return _channel;
    }

    public EndpointReference getMyRoleEndpointReference() {
        return _myRoleEPR;
    }

}
