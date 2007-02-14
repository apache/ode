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

import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.engine.WorkEvent.Type;
import org.apache.ode.bpel.iapi.*;
import org.w3c.dom.Element;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

class PartnerRoleMessageExchangeImpl extends MessageExchangeImpl implements PartnerRoleMessageExchange {

    private PartnerRoleChannel _channel;
    private EndpointReference _myRoleEPR;
    
    PartnerRoleMessageExchangeImpl(BpelEngineImpl engine, MessageExchangeDAO dao, PortType portType,
            Operation operation, 
            EndpointReference epr,
            EndpointReference myRoleEPR,
            PartnerRoleChannel channel) {
        super(engine, dao);
        _myRoleEPR = myRoleEPR;
        setPortOp(portType, operation);
        _channel = channel;
    }

    public void replyOneWayOk() {
        setStatus(Status.ASYNC);
    }

    public void replyAsync() {
        setStatus(Status.ASYNC);
    }

    public void replyWithFault(QName faultType, Message outputFaultMessage) throws BpelEngineException {
        boolean isAsync = isAsync();
        setFault(faultType, outputFaultMessage);
        if (isAsync)
            continueAsync();
    }

    public void reply(Message response) throws BpelEngineException {
        boolean isAsync = isAsync();
        setResponse(response);
        System.out.println("#### REPLY ASYNC " + isAsync);
        if (isAsync)
            continueAsync();

    }

    public void replyWithFailure(FailureType type, String description, Element details) throws BpelEngineException {
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
        if (getDAO().getChannel() == null)
            return;
        
        WorkEvent we = new WorkEvent();
        we.setIID(getDAO().getInstance().getInstanceId());
        we.setType(Type.INVOKE_RESPONSE);
        if (_engine._activeProcesses.get(getDAO().getProcess().getProcessId()).isInMemory())
            we.setInMem(true);
        we.setChannel(getDAO().getChannel());
        we.setMexId(getDAO().getMessageExchangeId());
        _engine._contexts.scheduler.schedulePersistedJob(we.getDetail(), null);
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
        return _dao.getProcess().getProcessId();
    }

    public String toString() {
        try {
            return "{PartnerRoleMex#" + getMessageExchangeId() + " [PID " + getCaller() + "] calling " + _epr + "."
                    + getOperationName() + "(...)}";

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
