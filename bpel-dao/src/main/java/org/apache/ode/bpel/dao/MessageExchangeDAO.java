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
package org.apache.ode.bpel.dao;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Date;
import java.util.Set;

/**
 * Data access object for a message exchange.
 */
public interface MessageExchangeDAO {

    public static final char DIR_BPEL_INVOKES_PARTNERROLE = 'P';

    public static final char DIR_PARTNER_INVOKES_MYROLE = 'M';

    /**
     * Instance id of the message exchange.
     *
     * @return message exchange id.
     */
    String getMessageExchangeId();

    /**
     * Get output message (could be fault message)
     *
     * @return output message DAO
     */
    MessageDAO getResponse();

    /**
     * Creation time of the message exchange
     *
     * @return create time
     */
    Date getCreateTime();

    void setCreateTime(Date createTime);

    /**
     * Get the input message.
     *
     * @return input message DAO
     */
    MessageDAO getRequest();

    /**
     * Get the operation name of this message exchange.
     *
     * @return operation name.
     */
    String getOperation();

    /**
     * The qualified name of the WSDL port type.
     *
     * @return port type name
     */
    QName getPortType();

    /**
     * Set the port type.
     *
     * @param porttype
     *          port type
     */
    void setPortType(QName porttype);

    /**
     * Set state of last message sent/received.
     *
     * @param status state to be set
     */
    void setStatus(String status);

    /**
     * Get state of last message sent/received.
     *
     * @return the state
     */
    String getStatus();

    /**
     * Create a new message associated with this message-exchange
     *
     * @param type
     *          message type
     * @return new {@link MessageDAO}
     */
    MessageDAO createMessage(QName type);

    /**
     * Creates an input message DAO.
     */
    void setRequest(MessageDAO msg);

    /**
     * Creates an output message DAO.
     */
    void setResponse(MessageDAO msg);

    /**
     * Get the model id for the partner link to which this message exchange
     * relates.
     *
     * @return
     */
    int getPartnerLinkModelId();

    /**
     * Set the model id for the partner link to which this message exchange
     * relates
     *
     * @param modelId
     */
    void setPartnerLinkModelId(int modelId);

    /**
     * Get the correlation identifier/client id
     *
     * @return correlation identifier
     */
    String getCorrelationId();

    /**
     * Set the correlation identifier/client id
     *
     * @param correlationId
     *          identifier
     */
    void setCorrelationId(String correlationId);

    void setPattern(String string);

    void setOperation(String opname);

    void setEPR(Element epr);

    Element getEPR();

    String getPattern();

    /**
     * Get the response channel.
     *
     * @return response channel.
     */
    String getChannel();

    /**
     * Set the response channel.
     *
     * @param string
     *          response channel
     */
    void setChannel(String string);

    boolean getPropagateTransactionFlag();

    QName getFault();

    void setFault(QName faultType);

    public String getFaultExplanation();

    public void setFaultExplanation(String explanation);

    void setCorrelationStatus(String cstatus);

    String getCorrelationStatus();

    /**
     * Get the process associate with this message exchange. The process should
     * always be available for partnerRole message exchanges. However, for myRole
     * message exchanges, it is possible that no process is associated with the
     * message exchange (i.e. if the EPR routing fails).
     *
     * @return process associated with the message exchange
     */
    ProcessDAO getProcess();

    void setProcess(ProcessDAO process);

    void setInstance(ProcessInstanceDAO dao);

    ProcessInstanceDAO getInstance();

    /**
     * Get the direction of the message exchange.
     *
     * @return
     */
    char getDirection();

    /**
     * Get the "callee"--the id of the process being invoked in a myRole
     * exchange.
     * @return
     */
    QName getCallee();

    /**
     * Set the "callee"--the id of the process being invoked in a myRole
     * exchange.
     * @param callee
     */
    void setCallee(QName callee);

    String getProperty(String key);

    void setProperty(String key, String value);

    Set<String> getPropertyNames();

    void setPartnerLink(PartnerLinkDAO plinkDAO);

    PartnerLinkDAO getPartnerLink();

    /**
     * Gets the mex id for the message exchange that has been piped with
     * this one in a process to process interaction. 
     * @return
     */
    String getPipedMessageExchangeId();
    void setPipedMessageExchangeId(String mexId);
    
    int getSubscriberCount();
    void setSubscriberCount(int subscriberCount);

    void release(boolean doClean);

    /**
     * Deletes messages that arrived before the route is setup
     */
    void releasePremieMessages();
}
