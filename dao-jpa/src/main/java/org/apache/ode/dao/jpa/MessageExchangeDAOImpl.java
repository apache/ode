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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;


@Entity
@Table(name="ODE_MESSAGE_EXCHANGE")
public class MessageExchangeDAOImpl implements MessageExchangeDAO {

	@Id @Column(name="MESSAGE_EXCHANGE_ID") 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long _id;
	@Basic @Column(name="CALLEE") private QName _callee;
	@Basic @Column(name="CHANNEL") private String _channel;
	@Basic @Column(name="CORRELATION_ID") private String _correlationId;
	@Basic @Column(name="CORRELATION_STATUS") private String _correlationStatus;
	@Basic @Column(name="CREATE_TIME") private Date _createTime;
	@Basic @Column(name="DIRECTION") private char _direction;
	@Lob   @Column(name="EPR") private String _epr;
	@Transient private Element _eprElement;
	@Basic @Column(name="FAULT") private String _fault;
	@Basic @Column(name="FAULT_EXPLANATION") private String _faultExplanation;
	@Basic @Column(name="MESSAGE_EXCHANGE_KEY") private String _messageExchangeId;
	@Basic @Column(name="OPERATION") private String _operation;
	@Basic @Column(name="PARTNER_LINK_MODEL_ID") private int _partnerLinkModelId;
	@Basic @Column(name="PATTERN") private String _pattern;
	@Basic @Column(name="PORT_TYPE") private QName _portType;
	@Basic @Column(name="PROPAGATE_TRANS") private boolean _propagateTransactionFlag;
	@Basic @Column(name="STATUS") private String _status;
	@Basic @Column(name="PROPERTIES") private HashMap<String,String> _props = new HashMap<String,String>();
	@Basic @Column(name="CORRELATION_KEYS") 
	private ArrayList<CorrelationKey> _correlationKeys = new ArrayList<CorrelationKey>();
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST})
	@Column(name="PROCESS_INSTANCE_ID")
	private ProcessInstanceDAOImpl _processInst;
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST})
	@Column(name="PARTNER_LINK_ID")
	private PartnerLinkDAOImpl _partnerLink;
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST})
	@Column(name="PROCESS_ID")
	private ProcessDAOImpl _process;
	@OneToOne(fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	@Column(name="REQUEST_MESSAGE_ID")
	private MessageDAOImpl _request;
	@OneToOne(fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	@Column(name="RESPONSE_MESSAGE_ID")
	private MessageDAOImpl _response;
	@Version @Column(name="VERSION") private long _version;
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST})
	@Column(name="CONNECTION_ID")
	private BPELDAOConnectionImpl _connection;
	
	public MessageExchangeDAOImpl() {}
	public MessageExchangeDAOImpl(char direction, BPELDAOConnectionImpl connection){
		_direction = direction;
		_connection = connection;
	}
	
	public MessageDAO createMessage(QName type) {
		return new MessageDAOImpl(type,this);
	}

	public QName getCallee() {
		return _callee;
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

	public String getFault() {
		return _fault;
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
		return _portType;
	}

	public ProcessDAO getProcess() {
		return _process;
	}

	public boolean getPropagateTransactionFlag() {
		return _propagateTransactionFlag;
	}

	public String getProperty(String key) {
		return _props.get(key);
	}

	public Set<String> getPropertyNames() {
		return _props.keySet();
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
		_callee = callee;
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

	public void setFault(String faultType) {
		_fault = faultType;
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
		_portType = porttype;
	}

	public void setProcess(ProcessDAO process) {
		_process = (ProcessDAOImpl)process;

	}

	public void setProperty(String key, String value) {
		_props.put(key, value);

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
	
	public void addCorrelationKey(CorrelationKey correlationKey) {
		_correlationKeys.add(correlationKey);
	}
	
	public Collection<CorrelationKey> getCorrelationKeys() {
		return _correlationKeys;
	}
}
