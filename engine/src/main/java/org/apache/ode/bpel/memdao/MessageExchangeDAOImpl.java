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

import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.MessageExchange.AckType;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.w3c.dom.Element;

public class MessageExchangeDAOImpl extends DaoBaseImpl implements MessageExchangeDAO {

    String messageExchangeId;
	MessageDAO response;
	Date createTime;
	MessageDAO request;
	String operation;
	QName portType;
	Status status;
	int partnerLinkModelId;
	String correlationId;
	MessageExchangePattern pattern;
	Element ePR;
	String channel;
	String resource;
	QName fault;
    String faultExplanation;
    String correlationStatus;
	ProcessDAO process;
	ProcessInstanceDAO instance;
	char direction;
	QName callee;
	Properties properties = new Properties();
    PartnerLinkDAOImpl _plink;
    InvocationStyle _istyle;
    String _pipedExchange;
    FailureType _failureType;
    long _timeout;
    AckType _ackType;
    QName _pipedPID;
    boolean _instantiatingResource;

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

	public void setStatus(Status status) {
		this.status = status;

	}

	public Status getStatus() {
		return status;
	}

	public MessageDAO createMessage(QName type) {
		MessageDAO messageDAO = new MessageDAOImpl();
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

	public String getPartnersKey() {
		return correlationId;
	}

	public void setPartnersKey(String correlationId) {
		this.correlationId = correlationId;

	}

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setPattern(MessageExchangePattern pattern) {
		this.pattern = pattern;

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

	
	public MessageExchangePattern getPattern() {
		return pattern;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String string) {
		this.channel = string;
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


    public void release() {
        instance = null;
        process = null;
        _plink = null;
        request = null;
        response = null;
    }

    public String toString() {
        return "mem.mex(direction=" + direction + " id=" + messageExchangeId + ")";
    }

    public InvocationStyle getInvocationStyle() {
        return _istyle;
    }

    public String getPipedMessageExchangeId() {
        return _pipedExchange;
    }

    public void setFailureType(FailureType failureType) {
        _failureType = failureType;
    }

    public FailureType getFailureType() {
        return _failureType;
    }
    
    public void setInvocationStyle(InvocationStyle invocationStyle) {
        _istyle = invocationStyle;
        
    }

    public void setPipedMessageExchangeId(String pipedMexId) {
        _pipedExchange = pipedMexId;
        
    }

    public long getTimeout() {
        return _timeout;
    }

    public void setTimeout(long timeout) {
        _timeout = timeout;
    }

    public AckType getAckType() {
        return _ackType;
    }

    public void setAckType(AckType ackType) {
        _ackType = ackType;
    }

    public QName getPipedPID() {
        return _pipedPID;
    }

    public void setPipedPID(QName pipedPid) {
        _pipedPID = pipedPid;
        
    }

    public boolean isInstantiatingResource() {
        return _instantiatingResource;
    }

    public void setInstantiatingResource(boolean inst) {
        _instantiatingResource = inst;
    }
}
