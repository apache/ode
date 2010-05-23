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

package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class MessageExchangeDAOImpl extends DaoBaseImpl implements MessageExchangeDAO {

    private String messageExchangeId;
    private MessageDAO response;
    private Date createTime;
    private MessageDAO request;
    private String operation;
    private QName portType;
    private String status;
    private int partnerLinkModelId;
    private String correlationId;
    private String pattern;
    private Element ePR;
    private Element callbackEPR;
    private String channel;
    private boolean propagateTransactionFlag;
    private QName fault;
    private String faultExplanation;
    private String correlationStatus;
    private ProcessDAO process;
    private ProcessInstanceDAO instance;
    private char direction;
    private QName callee;
    private Properties properties = new Properties();
    private PartnerLinkDAOImpl _plink;
    private String pipedMessageExchangeId;
    private int subscriberCount;

    public MessageExchangeDAOImpl(char direction, String messageEchangeId){
        this.direction = direction;
        this.messageExchangeId = messageEchangeId;
    }
    
    public String getMessageExchangeId() {
        return messageExchangeId;
    }

    public MessageDAO getResponse() {
        return response;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date d) {
        createTime = d;
    }

    public MessageDAO getRequest() {
        return request;
    }

    public String getOperation() {
        return operation;
    }

    public QName getPortType() {
        return portType;
    }

    public void setPortType(QName porttype) {
        this.portType = porttype;

    }

    public void setStatus(String status) {
        this.status = status;

    }

    public String getStatus() {
        return status;
    }

    public MessageDAO createMessage(QName type) {
        MessageDAO messageDAO = new MessageDAOImpl(this);
        messageDAO.setType(type);
        return messageDAO;
    }

    public void setRequest(MessageDAO msg) {
        this.request = msg;

    }

    public void setResponse(MessageDAO msg) {
        this.response = msg;

    }

    public int getPartnerLinkModelId() {
        return partnerLinkModelId;
    }

    public void setPartnerLinkModelId(int modelId) {
        this.partnerLinkModelId = modelId;

    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;

    }

    public void setPattern(String string) {
        this.pattern = string;

    }

    public void setOperation(String opname) {
        this.operation = opname;

    }

    public void setEPR(Element epr) {
        this.ePR = epr;

    }

    public Element getEPR() {
        return ePR;
    }

    public void setCallbackEPR(Element epr) {
        this.callbackEPR = epr;

    }

    public Element getCallbackEPR() {
        return callbackEPR;
    }

    public String getPattern() {
        return pattern;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String string) {
        this.channel = string;
    }

    public boolean getPropagateTransactionFlag() {
        return propagateTransactionFlag;
    }

    public QName getFault() {
        return fault;
    }

    public void setFault(QName faultType) {
        this.fault = faultType;
    }

    public String getFaultExplanation() {
        return faultExplanation;
    }

    public void setFaultExplanation(String explanation) {
        this.faultExplanation = explanation;
    }



    public void setCorrelationStatus(String cstatus) {
        this.correlationStatus = cstatus;
    }

    public String getCorrelationStatus() {
        return correlationStatus;
    }

    public ProcessDAO getProcess() {
        return process;
    }

    public void setProcess(ProcessDAO process) {
        this.process = process;

    }

    public void setInstance(ProcessInstanceDAO dao) {
        this.instance = dao;

    }

    public ProcessInstanceDAO getInstance() {
        return instance;
    }

    public char getDirection() {
        return direction;
    }

    public QName getCallee() {
        return callee;
    }

    public void setCallee(QName callee) {
        this.callee = callee;

    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key,value);
    }

    public void setPartnerLink(PartnerLinkDAO plinkDAO) {
        _plink = (PartnerLinkDAOImpl) plinkDAO;
        
    }

    public PartnerLinkDAO getPartnerLink() {
        return _plink;
    }

    public Set<String> getPropertyNames() {
        HashSet<String> retVal = new HashSet<String>();
        for (Entry<Object,Object> e : properties.entrySet()) {
            retVal.add((String)e.getKey());
        }
        return retVal;
    }

    public String getPipedMessageExchangeId() {
        return pipedMessageExchangeId;
    }

    public void setPipedMessageExchangeId(String pipedMessageExchangeId) {
        this.pipedMessageExchangeId = pipedMessageExchangeId;
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }
    
    public void setSubscriberCount(int subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public void incrementSubscriberCount() {
        ++subscriberCount;
    }
    
    public void release(boolean doClean) {
        instance = null;
        process = null;
        _plink = null;
        request = null;
        response = null;
        BpelDAOConnectionImpl.removeMessageExchange(getMessageExchangeId());
    }

    public void releasePremieMessages() {
        // do nothing; early messages are deleted during CorrelatorDaoImpl().dequeueMessage()
    }
    
    public String toString() {
        return "mem.mex(direction=" + direction + " id=" + messageExchangeId + ")";
    }

}
