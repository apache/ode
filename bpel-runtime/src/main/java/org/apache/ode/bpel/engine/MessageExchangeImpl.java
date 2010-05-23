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
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.util.Set;

abstract class MessageExchangeImpl implements MessageExchange {

    private static final Log __log = LogFactory.getLog(MessageExchangeImpl.class);
    protected static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    /** Process-Instance identifier.*/
    protected Long _iid;

    protected PortType _portType;
    protected Operation _operation;

    protected final BpelEngineImpl _engine;

    protected EndpointReference _epr;

    protected MessageExchangeDAO _dao;
    
	/**
     * Constructor: requires the minimal information for a message exchange.
     */
    MessageExchangeImpl(BpelEngineImpl engine,
                        MessageExchangeDAO dao,
                        MessageExchangePattern pattern,
                        String opname,
                        EndpointReference epr) {
        _engine = engine;
        _dao = dao;
        _epr = epr;

        getDAO().setPattern(pattern.toString());
        getDAO().setOperation(opname);
        if (epr != null)
            getDAO().setEPR(epr.toXML().getDocumentElement());
    }

    public MessageExchangeImpl(BpelEngineImpl engine,
                               MessageExchangeDAO dao) {
        _engine = engine;
        _dao = dao;
    }


    public String getMessageExchangeId() throws BpelEngineException {
        return getDAO().getMessageExchangeId();
    }

    public String getOperationName() throws BpelEngineException {
        return getDAO().getOperation();
    }

    public MessageExchangePattern getMessageExchangePattern() {
        return MessageExchangePattern.valueOf(getDAO().getPattern());
    }

    public boolean isTransactionPropagated() throws BpelEngineException {
        return getDAO().getPropagateTransactionFlag();
    }

    public Message getResponse() {
        if (getDAO().getResponse() != null)
            return new MessageImpl(getDAO().getResponse());
        else
            return null;
    }

    public QName getFault() {
        return getDAO().getFault();
    }

    public Message getFaultResponse() {
        return getResponse();
    }

    public String getFaultExplanation() {
        return getDAO().getFaultExplanation();
    }

    public MessageExchangePattern getPattern() {
        return MessageExchangePattern.valueOf(getDAO().getPattern());
    }

    public Status getStatus() {
        return Status.valueOf(getDAO().getStatus());
    }

    public Message getRequest() {
        return new MessageImpl(getDAO().getRequest());
    }

    public Operation getOperation() {
        return _operation;
    }

    public PortType getPortType() {
        return _portType;
    }

    /**
     * Update the pattern of this message exchange.
     * @param pattern
     */
    void setPattern(MessageExchangePattern pattern) {
        if (__log.isTraceEnabled())
            __log.trace("Mex[" + getMessageExchangeId() + "].setPattern("+pattern+")");
        getDAO().setPattern(pattern.toString());
    }


    void setPortOp(PortType portType, Operation operation) {
        if (__log.isTraceEnabled())
            __log.trace("Mex[" + getMessageExchangeId()  + "].setPortOp(...)");
        _portType = portType;
        _operation = operation;
    }

    public MessageExchangeDAO getDAO() {
        return _dao;
    }

    void setFault(QName faultType, Message outputFaultMessage) throws BpelEngineException {
        setStatus(Status.FAULT);
        getDAO().setFault(faultType);
        getDAO().setResponse(((MessageImpl)outputFaultMessage)._dao);
        
        responseReceived();
    }

    void setFaultExplanation(String explanation) {
        getDAO().setFaultExplanation(explanation);
    }

    void setResponse(Message outputMessage) throws BpelEngineException {
        if (getStatus() != Status.REQUEST && getStatus()!=Status.ASYNC)
            throw new IllegalStateException("Not in REQUEST state!");

        setStatus(Status.RESPONSE);
        getDAO().setFault(null);
        getDAO().setResponse(((MessageImpl)outputMessage)._dao);

        // Meant to be overriden by subclasses when needed
        responseReceived();
    }

    void setFailure(FailureType type, String reason, Element details) throws BpelEngineException {
        // TODO not using FailureType, nor details
        setStatus(Status.FAILURE);
        getDAO().setFaultExplanation(reason);
    }

    void setStatus(Status status) {
        getDAO().setStatus(status.toString());
    }

    public Message createMessage(javax.xml.namespace.QName msgType) {
        MessageDAO mdao = getDAO().createMessage(msgType);
        return new MessageImpl(mdao);
    }

    public void setEndpointReference(EndpointReference ref) {
        _epr = ref;
        if (ref != null)
            getDAO().setEPR(ref.toXML().getDocumentElement());
    }

    public EndpointReference getEndpointReference() throws BpelEngineException {
        if (_epr != null) return _epr;
        if (getDAO().getEPR() == null)
            return null;

        return _epr = _engine._contexts.eprContext.resolveEndpointReference(getDAO().getEPR());
    }


    QName getServiceName() {
        return getDAO().getCallee();
    }

    public String getProperty(String key) {
        String val = getDAO().getProperty(key);
        if (__log.isDebugEnabled())
            __log.debug("GET MEX property " + key + " = " + val);
        return val;
    }

    public void setProperty(String key, String value) {
        getDAO().setProperty(key,value);
        if (__log.isDebugEnabled())
            __log.debug("SET MEX property " + key + " = " + value);
    }

    public Set<String> getPropertyNames() {
        return getDAO().getPropertyNames();
    }

    public int getSubscriberCount() {
    	return getDAO().getSubscriberCount();    	
    }
    
    public void setSubscriberCount(int subscriberCount) {
    	getDAO().setSubscriberCount(subscriberCount);
    }
    
    public void release() {
        __log.debug("Releasing mex " + getMessageExchangeId());
        // for a one-way, message exchanges are always deleted
        _dao.release(true);
        _dao = null;
    }

    public String toString() {
        return "MEX["+getDAO().getMessageExchangeId() +"]";
    }

    protected void responseReceived() {
        // Nothing to do here, just opening the possibility of overriding
    }
}
