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

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.o.OPartnerLink;
import org.w3c.dom.Element;

/**
 * Base-class implementation of the interface used to expose a partner invocation to the integration layer.
 * 
 * @author Maciej Szefler
 */
abstract class PartnerRoleMessageExchangeImpl extends MessageExchangeImpl implements PartnerRoleMessageExchange {
    private static final Log __log = LogFactory.getLog(PartnerRoleMessageExchangeImpl.class);

    protected final PartnerRoleChannel _partnerRoleChannel;

    protected EndpointReference _myRoleEPR;

    protected String _responseChannel;

    protected volatile String _foreignKey;

    private QName _caller;

    /** thread-local indicator telling us if a given thread is the thread that "owns" the object. */
    final ThreadLocal<Boolean> _ownerThread = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }

    };

    volatile boolean _blocked = false;

    PartnerRoleMessageExchangeImpl(BpelProcess process, String mexId, OPartnerLink oplink, Operation operation,
            EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel channel) {
        super(process, mexId, oplink, oplink.partnerRolePortType, operation);
        _myRoleEPR = myRoleEPR;
        _partnerRoleChannel = channel;
    }

    @Override
    void load(MessageExchangeDAO dao) {
        super.load(dao);
        _caller = dao.getProcess().getProcessId();
    }

    @Override
    public void save(MessageExchangeDAO dao) {
        super.save(dao);
    }

    public void replyAsync(String foreignKey) {
        throw new BpelEngineException("replyAsync() is not supported for invocation style " + getInvocationStyle());
    }

    public void replyOneWayOk() {
        if (__log.isDebugEnabled()) {
            __log.debug("replyOneWayOk mex=" + getMessageExchangeId());
        }
        sync();
        checkReplyContextOk();
        setStatus(Status.ASYNC);
        sync();
    }

    public void replyWithFault(QName faultType, Message outputFaultMessage) throws BpelEngineException {
        if (__log.isDebugEnabled()) {
            __log.debug("replyWithFault mex=" + getMessageExchangeId());
        }
        sync();
        checkReplyContextOk();
        setFault(faultType, outputFaultMessage);
        sync();
        if (!_blocked)
            resumeInstance();
    }

    public void reply(Message response) throws BpelEngineException {
        if (__log.isDebugEnabled()) {
            __log.debug("reply mex=" + getMessageExchangeId());
        }
        sync();
        checkReplyContextOk();
        setResponse(response);
        sync();
        if (!_blocked)
            resumeInstance();

    }

    public void replyWithFailure(FailureType type, String description, Element details) throws BpelEngineException {
        if (__log.isDebugEnabled()) {
            __log.debug("replyWithFailure mex=" + getMessageExchangeId());
        }
        sync();
        checkReplyContextOk();
        setFailure(type, description, details);
        sync();
        if (!_blocked)
            resumeInstance();
    }

    public QName getCaller() {
        return _caller;
    }

    public PartnerRoleChannel getPartnerRoleChannel() {
        return _partnerRoleChannel;
    }

    public EndpointReference getMyRoleEndpointReference() {
        return _myRoleEPR;
    }

    public String toString() {
        try {
            return "{PartnerRoleMex#" + _mexId + " [PID " + getCaller() + "] calling " + _epr + "." + getOperationName() + "(...)}";

        } catch (Throwable t) {
            return "{PartnerRoleMex#????}";
        }

    }

    /**
     * Resume an instance. This happens if the response for the partner invocation were not "immediately" available, that is if the
     * IL was not able to supply a response in the scope of the
     * {@link MessageExchangeContext#invokePartnerReliable(PartnerRoleMessageExchange)} or
     * {@link MessageExchangeContext#invokePartnerAsynch(PartnerRoleMessageExchange)}. Note that this is actually the common case
     * for ASYNC and RELIABLE invocations.
     * 
     */
    protected void resumeInstance() {
        assert false : "should not get resumeInstance() call";
        throw new IllegalStateException("InternalError: unexpected state");
    }

    protected WorkEvent generateInvokeResponseWorkEvent() {
        WorkEvent we = new WorkEvent();
        we.setIID(_iid);
        we.setType(WorkEvent.Type.PARTNER_RESPONSE);
        we.setChannel(_responseChannel);
        we.setMexId(_mexId);

        return we;

    }

    protected void checkReplyContextOk() {
        // Prevent duplicate replies.
        if (getStatus() != MessageExchange.Status.REQUEST && getStatus() != MessageExchange.Status.ASYNC)
            throw new BpelEngineException("Invalid message exchange state, expect REQUEST or ASYNC, but got " + getStatus());

        // In-memory processe are special, they don't allow scheduling so any replies must be delivered immediately.
        if (!_blocked && _process.isInMemory())
            throw new BpelEngineException("Cannot reply to in-memory process outside of BLOCKING call");
    }

}
