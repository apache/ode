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
package org.apache.ode.bpel.iapi;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.util.Set;

/**
 * A representation of a communication (message-exchange) between the BPEL 
 * BPEL engine and an  external "partner".
 * 
 * @author mszefler
 */
public interface MessageExchange {

    /**
     * Enumeration of message exchange patterns.
     */
    public enum MessageExchangePattern {
        REQUEST_ONLY,
        REQUEST_RESPONSE,
        UNKNOWN
    }

    /**
     * Enumeration of the possible states for the message exchange.
     */
    public enum Status {
        /** New message exchange, has not been "invoked" */
        NEW,

        /** The request was sent, blocking while waiting for the service to respond. */
        REQ,

        /** The request was sent, no longer blocking. */
        ASYNC,
        
        /** The acknowledgement (either response/fault/failure) was sent. */
        ACK,

        /** The acknowledgement was processed. */
        COMPLETED

    }

    public enum AckType {
        RESPONSE,
        ONEWAY,
        FAULT,
        FAILURE
    }
    
    /**
     * Enumeration of the types of failures.
     */
    public enum FailureType {
        /** Requested endpoint is invalid. */
        INVALID_ENDPOINT,

        /** Requested endpoint is unknown/unavailable. */
        UNKNOWN_ENDPOINT,

        /** Requested operation is unknown/unimplemented. */
        UNKNOWN_OPERATION,

        /** Network / IPC errror. */
        COMMUNICATION_ERROR,

        /** Request message was of an invalid/unrecognized format. */
        FORMAT_ERROR,

        /** An internal failure: no response was provided. */
        NO_RESPONSE,

        /** Message exchange processing was aborted. */
        ABORTED,

        /** Other failure. */
        OTHER, NOMATCH
    }

    /**
     * Get the message exchange identifier. This identifier should be globally
     * unique as the BPEL engine may keep identifiers for extended periods of
     * time.
     * @return unique message exchange identifier
     */
    String getMessageExchangeId()
            throws BpelEngineException;


    /**
     * Get the invocation style for this message exchange. 
     * @return
     */
    InvocationStyle getInvocationStyle();
    
    /**
     * Get the time-out in ms. 
     * @return
     */
    long getTimeout();

    /**
     * Set the time-out in ms
     * @param timeout
     */
    void setTimeout(long timeout);
    
    /**
     * Get the name of the operation (WSDL 1.1) / message exchange (WSDL 1.2?).
     *
     * @return name of the operation (WSDL 1.1) /message exchange (WSDL 1.2?).
     */
    String getOperationName()
            throws BpelEngineException;


    /**
     * Get a reference to the end-point targeted by this message exchange.
     * @return end-point reference for this message exchange
     */
    EndpointReference getEndpointReference()
            throws BpelEngineException;


    AckType getAckType();

    /**
     * Return the type of message-exchange that resulted form this invocation
     * (request only/request-respone). If a
     * {@link MessageExchangePattern#REQUEST_RESPONSE} message-exchange was
     * created, then the caller should expect a response in the future.
     * @return type of message exchange created by the invocation
     */
    MessageExchangePattern getMessageExchangePattern();

    /**
     * Create a message associated with this exchange.
     * @param msgType message type
     * @return a new {@link Message}
     */
    Message createMessage(QName msgType);

    /**
     * Indicates whether a transactions in associated with the message exchange. If this is the case, then the object must be used
     * from a context (i.e. thread) that is associated with the same transaction. The TRANSACTED and RELIABLE invocation styles will
     * have this flag set to <code>true</code>. ASYNC and BLOCKING styles will always have this set to <code>false</code>.
     * @return <code>true<code> if there is a transaction associated with the object, <code>false</code> otherwise.
     */
    boolean isTransactional();

    /**
     * Get the message exchange status.
     * @return
     */
    Status getStatus();

    /**
     * Get the request message.
     * @return request message
     */
    Message getRequest();

    /**
     * Get the response message.
     * @return response message (or null if not avaiable)
     */
    Message getResponse();

    /**
     * Get the fault type.
     * @return fault type, or <code>null</code> if not available/applicable.
     */
    QName getFault();

    String getFaultExplanation();

    /**
     * Get the fault resposne message.
     * @return fault response, or <code>null</code> if not available/applicable.
     */
    Message getFaultResponse();

    /**
     * Get the operation description for this message exchange.
     * It is possible that the description cannot be resolved, for example if
     * the EPR is unknown or if the operation does not exist.
     * @return WSDL operation description or <code>null</code> if not availble
     */
    Operation getOperation();

    /**
     * Get the port type description for this message exchange.
     * It is possible that the description cannot be resolved, for example if
     * the EPR is unknown or if the operation does not exist.
     * @return WSDL port type description or <code>null</code> if not available.
     */
    PortType getPortType();

    /**
     * Set a message exchange property. Message exchange properties are not
     * interpreted by the engine--they exist to enable the integration layer
     * to persist information about the exchange.
     * @param key property key
     * @param value property value
     */
    void setProperty(String key, String value);

    /**
     * Get a message exchange property.
     * @param key property key
     * @return property value
     */
    String getProperty(String key);


    /**
     * Get a set containing the names of the defined properties.
     * @return set of property names.
     */
    public Set<String> getPropertyNames();

    /**
     * Report whether the operation is "safe" in the sense of the WSDL1.2 meaning of the term. That is,
     * is the operation side-effect free?
     * @return <code>true</code> if the operation is safe, <code>false</code> otherwise. 
     */
    public boolean isSafe();
    
    /**
     * Should be called by the external partner when it's done with the
     * message exchange. Ncessary for a better resource management and
     * proper mex cleanup.
     */
    public void release();

    public static final String PROPERTY_SEP_MYROLE_SESSIONID = "org.apache.ode.bpel.myRoleSessionId";
    public static final String PROPERTY_SEP_PARTNERROLE_SESSIONID = "org.apache.ode.bpel.partnerRoleSessionId";
    public static final String PROPERTY_SEP_PARTNERROLE_EPR = "org.apache.ode.bpel.partnerRoleEPR";
    public static final String PROPERTY_SEP_MYROLE_TRANSACTED = "org.apache.ode.bpel.myRoleTransacted";
}
