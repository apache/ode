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

package org.apache.ode.dao.jpa;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.uuid.UUID;
import org.w3c.dom.Element;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

@Entity
@Table(name="ODE_MESSAGE_EXCHANGE")
public class MessageExchangeDAOImpl implements MessageExchangeDAO {

	@Id @Column(name="MESSAGE_EXCHANGE_ID") 
	private String _id;
	@Basic @Column(name="CALLEE")
    private String _callee;
	@Basic @Column(name="CHANNEL")
    private String _channel;
	@Basic @Column(name="CORRELATION_ID")
    private String _correlationId;
	@Basic @Column(name="CORRELATION_STATUS")
    private String _correlationStatus;
	@Basic @Column(name="CREATE_TIME")
    private Date _createTime;
	@Basic @Column(name="DIRECTION")
    private char _direction;
	@Lob   @Column(name="EPR")
    private String _epr;
	@Transient private
    Element _eprElement;
	@Basic @Column(name="FAULT")
    private String _fault;
	@Basic @Column(name="FAULT_EXPLANATION")
    private String _faultExplanation;
	@Basic @Column(name="OPERATION")
    private String _operation;
	@Basic @Column(name="PARTNER_LINK_MODEL_ID")
    private int _partnerLinkModelId;
	@Basic @Column(name="PATTERN")
    private String _pattern;
	@Basic @Column(name="PORT_TYPE")
    private String _portType;
	@Basic @Column(name="PROPAGATE_TRANS")
    private boolean _propagateTransactionFlag;
	@Basic @Column(name="STATUS")
    private String _status;
    @Basic @Column(name="CORRELATION_KEYS")
    private String _correlationKeys;
    @Basic @Column(name="PIPED_ID")
    private String _pipedMessageExchangeId;

    @OneToMany(targetEntity=MexProperty.class,mappedBy="_mex",fetch=FetchType.EAGER,cascade={CascadeType.ALL})
    private Collection<MexProperty> _props = new ArrayList<MexProperty>();
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PROCESS_INSTANCE_ID")
	private ProcessInstanceDAOImpl _processInst;
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PARTNER_LINK_ID")
	private PartnerLinkDAOImpl _partnerLink;
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PROCESS_ID")
	private ProcessDAOImpl _process;
	@OneToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="REQUEST_MESSAGE_ID")
	private MessageDAOImpl _request;
    @OneToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="RESPONSE_MESSAGE_ID")
	private MessageDAOImpl _response;

    @ManyToOne(fetch= FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="CORR_ID")
    private CorrelatorDAOImpl _correlator;
    
    
    @Basic @Column(name="ISTYLE")
    private String _istyle;

    @OneToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="P2P_PIPE_PEER")
    private MessageExchangeDAO _pipedMex;
    
    @Basic @Column(name="TIMEOUT")
    private long _timeout;
    
    @Basic @Column(name="FAILURE_TYPE")
    private String _failureType;

    public MessageExchangeDAOImpl() {}
    
	public MessageExchangeDAOImpl(char direction){
		_direction = direction;
		_id = new UUID().toString();
	}
	
	public MessageDAO createMessage(QName type) {
		MessageDAOImpl ret = new MessageDAOImpl(type,this);
		return ret ;
	}

	public QName getCallee() {
		return _callee == null ? null : QName.valueOf(_callee);
	}

	public String getChannel() {
		return _channel;
	}

	public String getCorrelationId() {
		return _correlationId;
	}

	public String getCorrelationStatus() {
		return _correlationStatus;
	}

	public Date getCreateTime() {
		return _createTime;
	}

	public char getDirection() {
		return _direction;
	}

	public Element getEPR() {
		if ( _eprElement == null && _epr != null ) {
			try {
				_eprElement = DOMUtils.stringToDOM(_epr);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return _eprElement;
	}

	public QName getFault() {
		return _fault == null ? null : QName.valueOf(_fault);
	}

	public String getFaultExplanation() {
		return _faultExplanation;
	}

	public ProcessInstanceDAO getInstance() {
		return _processInst;
	}

	public String getMessageExchangeId() {
		//return _messageExchangeId;
		return _id.toString();
	}

	public String getOperation() {
		return _operation;
	}

	public PartnerLinkDAO getPartnerLink() {
		return _partnerLink;
	}

	public int getPartnerLinkModelId() {
		return _partnerLinkModelId;
	}

	public String getPattern() {
		return _pattern;
	}

	public QName getPortType() {
		return _portType == null ? null : QName.valueOf(_portType);
	}

	public ProcessDAO getProcess() {
		return _process;
	}

	public boolean getPropagateTransactionFlag() {
		return _propagateTransactionFlag;
	}

	public String getProperty(String key) {
        for (MexProperty prop : _props) {
            if (prop.getPropertyKey().equals(key)) return prop.getPropertyValue();
        }
        return null;
	}

	public Set<String> getPropertyNames() {
        HashSet<String> propNames = new HashSet<String>();
        for (MexProperty prop : _props) {
            propNames.add(prop.getPropertyKey());
        }
        return propNames;
	}

	public MessageDAO getRequest() {
		return _request;
	}

	public MessageDAO getResponse() {
		return _response;
	}

	public String getStatus() {
		return _status;
	}

	public void setCallee(QName callee) {
		_callee = callee.toString();
	}

	public void setChannel(String channel) {
		_channel = channel;
	}

	public void setCorrelationId(String correlationId) {
		_correlationId = correlationId;
	}

	public void setCorrelationStatus(String cstatus) {
		_correlationStatus = cstatus;
	}

	public void setEPR(Element epr) {
		_eprElement = epr;
		_epr = DOMUtils.domToString(epr);
	}

	public void setFault(QName faultType) {
		_fault = faultType == null ? null : faultType.toString();
	}

	public void setFaultExplanation(String explanation) {
		_faultExplanation = explanation;
	}

	public void setInstance(ProcessInstanceDAO dao) {
		_processInst = (ProcessInstanceDAOImpl)dao;
	}

	public void setOperation(String opname) {
		_operation = opname;
	}

	public void setPartnerLink(PartnerLinkDAO plinkDAO) {
		_partnerLink = (PartnerLinkDAOImpl)plinkDAO;
	}

	public void setPartnerLinkModelId(int modelId) {
		_partnerLinkModelId = modelId;
	}

	public void setPattern(String pattern) {
		_pattern = pattern;
	}

	public void setPortType(QName porttype) {
		_portType = porttype.toString();
	}

	public void setProcess(ProcessDAO process) {
		_process = (ProcessDAOImpl)process;
	}

	public void setProperty(String key, String value) {
        _props.add(new MexProperty(key, value, this));
	}

	public void setRequest(MessageDAO msg) {
		_request = (MessageDAOImpl)msg;
	}

	public void setResponse(MessageDAO msg) {
		_response = (MessageDAOImpl)msg;
	}

	public void setStatus(String status) {
		_status = status;
	}

    public String getPipedMessageExchangeId() {
        return _pipedMessageExchangeId;
    }

    public void setPipedMessageExchangeId(String pipedMessageExchangeId) {
        _pipedMessageExchangeId = pipedMessageExchangeId;
    }

    public void addCorrelationKey(CorrelationKey correlationKey) {
        if (_correlationKeys == null)
            _correlationKeys = correlationKey.toCanonicalString();
        else
            _correlationKeys = _correlationKeys + "^" + correlationKey.toCanonicalString();
	}

	public Collection<CorrelationKey> getCorrelationKeys() {
        ArrayList<CorrelationKey> correlationKeys = new ArrayList<CorrelationKey>();
        if (_correlationKeys.indexOf("^") > 0) {
            for (StringTokenizer tokenizer = new StringTokenizer(_correlationKeys, "^"); tokenizer.hasMoreTokens();) {
                String corrStr = tokenizer.nextToken();
                correlationKeys.add(new CorrelationKey(corrStr));
            }
            return correlationKeys;
        } else correlationKeys.add(new CorrelationKey(_correlationKeys));
        return correlationKeys;
    }


    public void release() {
        // no-op for now, could be used to do some cleanup
    }

    public CorrelatorDAOImpl getCorrelator() {
        return _correlator;
    }

    public void setCorrelator(CorrelatorDAOImpl correlator) {
        _correlator = correlator;
    }

    public String getInvocationStyle() {
        return _istyle;
    }

    public MessageExchangeDAO getPipedMessageExchange() {
        return _pipedMex;
    }

    public long getTimeout() {
        return _timeout;
    }

    public void setFailureType(String failureType) {
        _failureType = failureType;
    }

    public void setInvocationStyle(String invocationStyle) {
        _istyle = invocationStyle;
    }

    public void setPipedMessageExchange(MessageExchangeDAO mex) {
        _pipedMex = mex;
    }

    public void setTimeout(long timeout) {
        // TODO Auto-generated method stub
        
    }
}
