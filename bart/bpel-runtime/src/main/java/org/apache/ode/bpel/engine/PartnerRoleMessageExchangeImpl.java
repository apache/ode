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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

import com.sun.corba.se.spi.activation._ActivatorImplBase;

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

    protected Lock _accessLock = new ReentrantLock();

    protected Condition _stateChanged = _accessLock.newCondition();
    protected Condition _acked = _accessLock.newCondition();

    private QName _caller;

    /** the states for a partner mex. */
    enum State {
        /** state when we're in one of the MexContext.invokeXXX methods. */
        INVOKE_XXX,

        /** hold all actions (blocks the IL) */
        HOLD,

        /** the MEX is ASYNC ("in the wild"), i.e. a response can come at any momemnt from any thread. */
        ASYNC,

        /** the MEX is dead, it should no longer be accessed by the IL */
        DEAD
    };

    protected State _state = State.INVOKE_XXX;

    PartnerRoleMessageExchangeImpl(BpelProcess process, Long iid, String mexId, OPartnerLink oplink, Operation operation,
            EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel channel) {
        super(process, iid, mexId, oplink, oplink.partnerRolePortType, operation);
        _myRoleEPR = myRoleEPR;
        _partnerRoleChannel = channel;
    }

    @Override
    void load(MessageExchangeDAO dao) {
        super.load(dao);
        _caller = dao.getProcess().getProcessId();
    }

    @Override
    void save(MessageExchangeDAO dao) {
        super.save(dao);
    }

    @Override
    void ack(AckType acktype) {
        _accessLock.lock();
        try {
            super.ack(acktype);
            _acked.signalAll();
        } finally {
            _accessLock.unlock();
        }
    }
    
    public void replyAsync(String foreignKey) {
        throw new IllegalStateException("replyAsync() is not supported for invocation style " + getInvocationStyle());
    }

    public void replyOneWayOk() {
        if (__log.isDebugEnabled()) {
            __log.debug("replyOneWayOk mex=" + getMessageExchangeId());
        }

        _accessLock.lock();
        try {
            checkReplyContextOk();
            ack(AckType.ONEWAY);
        } finally {
            _accessLock.unlock();
        }
    }

    public void replyWithFault(QName faultType, Message outputFaultMessage) throws BpelEngineException {
        if (__log.isDebugEnabled()) {
            __log.debug("replyWithFault mex=" + getMessageExchangeId());
        }

        _accessLock.lock();
        try {
            checkReplyContextOk();
            _fault = faultType;
            _failureType = null;
            _response = (MessageImpl) outputFaultMessage;
            ack(AckType.FAULT);            
            if (_state == State.ASYNC)
                asyncACK();
        } finally {
            _accessLock.unlock();
        }
    }

    public void reply(Message response) throws BpelEngineException {
        if (__log.isDebugEnabled()) {
            __log.debug("reply mex=" + getMessageExchangeId());
        }

        _accessLock.lock();
        try {
            checkReplyContextOk();
            _response = (MessageImpl) response;
            _fault = null;
            _failureType = null;
            ack(AckType.RESPONSE);
            if (_state == State.ASYNC)
                asyncACK();
        } finally {
            _accessLock.unlock();
        }

    }

    public void replyWithFailure(FailureType type, String description, Element details) throws BpelEngineException {
        if (__log.isDebugEnabled()) {
            __log.debug("replyWithFailure mex=" + getMessageExchangeId());
        }

        _accessLock.lock();
        try {
            checkReplyContextOk();
            _failureType = type;
            _explanation = description;
            _fault = null;
            _response = null;
            ack(AckType.FAILURE);
            if (_state == State.ASYNC)
                asyncACK();
        } finally {
            _accessLock.unlock();
        }
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
    protected abstract void asyncACK();
    
    
    protected void checkReplyContextOk() {
        // Prevent duplicate replies.
        while (_state == State.HOLD)
            try {
                _stateChanged.await();
            } catch (InterruptedException e) {
                throw new BpelEngineException("Thread Interrupted.", e);
            }

        if (_state == State.DEAD)
            throw new IllegalStateException("Object used in inappropriate context. ");

        if (getStatus() != MessageExchange.Status.REQ)
            throw new IllegalStateException("Invalid message exchange state, expect REQUEST or ASYNC, but got " + getStatus());

    }

    void setState(State newstate) {
        _accessLock.lock();
        try {
            _state = newstate;
            _stateChanged.signalAll();
        } finally {
            _accessLock.unlock();
        }
    }
    
    public boolean waitForAck(long timeout) throws InterruptedException  {
        _accessLock.lock();
        try {
            if (getStatus() != Status.ACK) 
                return _acked.await(timeout,TimeUnit.MILLISECONDS);
            else
                return true;
        } finally {
            _accessLock.unlock();
        }
    }

    
}
