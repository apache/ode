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

import java.util.*;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
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
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

@Entity
@Table(name="ODE_MESSAGE_EXCHANGE")
@NamedQueries({
    @NamedQuery(name=MessageExchangeDAOImpl.DELETE_MEXS_BY_PROCESS, query="delete from MessageExchangeDAOImpl as m where m._process = :process"),
    @NamedQuery(name=MessageExchangeDAOImpl.SELECT_MEX_IDS_BY_PROCESS, query="select m._id from MessageExchangeDAOImpl as m where m._process = :process")
})
public class MessageExchangeDAOImpl extends OpenJPADAO implements MessageExchangeDAO {
    private static final Log __log = LogFactory.getLog(MessageExchangeDAOImpl.class);
    
    public final static String DELETE_MEXS_BY_PROCESS = "DELETE_MEXS_BY_PROCESS";
    public final static String SELECT_MEX_IDS_BY_PROCESS = "SELECT_MEX_IDS_BY_PROCESS";

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
	@Basic @Column(name="CREATE_TIME" , nullable = false)
    private Date _createTime;
	@Basic @Column(name="DIRECTION" ,nullable=false)
    private char _direction;
	@Lob   @Column(name="EPR")
    private String _epr;
	@Basic   @Column(name="RESOURCE", length=255)
    private String _resource;
	@Transient private
    Element _eprElement;
	@Basic @Column(name="FAULT")
    private String _fault;
	@Basic @Column(name="FAULT_EXPLANATION")
    private String _faultExplanation;
	@Basic @Column(name="OPERATION", nullable=false)
    private String _operation;
	@Basic @Column(name="PARTNER_LINK_MODEL_ID", nullable=false)
    private int _partnerLinkModelId;
	@Basic @Column(name="PATTERN", nullable=false)
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
    
    @Basic @Column(name="ACK_TYPE")
    private String _ackType;

    @OneToMany(targetEntity=MexProperty.class,mappedBy="_mex",fetch=FetchType.EAGER,cascade={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Collection<MexProperty> _props = new ArrayList<MexProperty>();
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="INSTANCE")
	private ProcessInstanceDAOImpl _processInst;
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PLINK")
	private PartnerLinkDAOImpl _partnerLink;
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PROCESS")
	private ProcessDAOImpl _process;
	@OneToOne(fetch=FetchType.LAZY,cascade={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}) @Column(name="REQUEST")
	private MessageDAOImpl _request;
    @OneToOne(fetch=FetchType.LAZY,cascade={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}) @Column(name="RESPONSE")
	private MessageDAOImpl _response;

    @ManyToOne(fetch= FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="CORRELATOR")
    private CorrelatorDAOImpl _correlator;
    
    @Basic @Column(name="ISTYLE")
    private String _istyle;

    @Basic @Column(name="TIMEOUT")
    private long _timeout;
    
    @Basic @Column(name="FAILURE_TYPE")
    private String _failureType;
    
    @Basic @Column(name="PIPED_PID")
    private String _pipedPid;

    @Basic @Column(name="INST_RES")
    private boolean _instantiatingResource;

    public MessageExchangeDAOImpl() {}
    
	public MessageExchangeDAOImpl(String mexId, char direction){
		_direction = direction;
		_id = mexId;
        _createTime = new Date();
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

	public String getPartnersKey() {
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
		if ( _eprElement == null && _epr != null && !"".equals(_epr)) {
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

	public MessageExchangePattern getPattern() {
		return _pattern == null ? null : MessageExchangePattern.valueOf(_pattern);
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

    public Map<String,String> getProperties() {
        HashMap<String,String> res = new  HashMap<String, String>();
        for (MexProperty prop : _props) {
            res.put(prop.getPropertyKey(), prop.getPropertyValue());
        }
        return res;
    }

	public MessageDAO getRequest() {
		return _request;
	}

	public MessageDAO getResponse() {
		return _response;
	}

	public Status getStatus() {
		return _status == null ? null : Status.valueOf(_status);
	}

	public void setCallee(QName callee) {
		_callee = callee.toString();
	}

	public void setChannel(String channel) {
		_channel = channel;
	}

	public void setPartnersKey(String correlationId) {
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

	public void setPattern(MessageExchangePattern pattern) {
		_pattern = pattern == null ? null : pattern.toString();
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

	public void setStatus(Status status) {
		_status = status == null ?  null : status.toString();
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
        if (_correlationKeys != null) {
        if (_correlationKeys.indexOf("^") > 0) {
            for (StringTokenizer tokenizer = new StringTokenizer(_correlationKeys, "^"); tokenizer.hasMoreTokens();) {
                String corrStr = tokenizer.nextToken();
                correlationKeys.add(new CorrelationKey(corrStr));
            }
            return correlationKeys;
        } else correlationKeys.add(new CorrelationKey(_correlationKeys));
        }
        return correlationKeys;
    }


    public void release(boolean doClean) {
        if( __log.isDebugEnabled() ) __log.debug("INSTANCE CLEANUP(MEX, doClean=" + doClean + ")");
        if( doClean ) {
            deleteMessages();
        }
    }

    public void deleteMessages() {
        if( __log.isDebugEnabled() ) __log.debug("INSTANCE CLEANUP(MEX:" + _id + "(request:" + _request + ", response:" + _response);
        
        getEM().remove(_request);
        getEM().remove(_response);
        getEM().createNamedQuery(MexProperty.DELETE_MEX_PROPERTIES_BY_MEX_ID).setParameter("mexId", _id).executeUpdate();
        getEM().remove(this); // This deletes MexProperty, REQUEST MessageDAO, RESPONSE MessageDAO
    }

    public CorrelatorDAOImpl getCorrelator() {
        return _correlator;
    }

    public void setCorrelator(CorrelatorDAOImpl correlator) {
        _correlator = correlator;
    }

    public InvocationStyle getInvocationStyle() {
        return _istyle == null ? null : InvocationStyle.valueOf(_istyle);
    }


    public long getTimeout() {
        return _timeout;
    }

    public void setFailureType(FailureType failureType) {
        _failureType = failureType == null ? null :failureType.toString();
    }
    
    public FailureType getFailureType() {
        return _failureType == null ? null : FailureType.valueOf(_failureType);
    }

    public void setInvocationStyle(InvocationStyle invocationStyle) {
        _istyle = invocationStyle == null ? null : invocationStyle.toString();
    }

    public void setTimeout(long timeout) {
        _timeout = timeout;
    }

    public AckType getAckType() {
        return _ackType == null ? null : AckType.valueOf(_ackType);
    }

    public void setAckType(AckType ackType) {
        _ackType = ackType == null ? null :ackType.toString();
    }

    public QName getPipedPID() {
        return _pipedPid == null ? null : QName.valueOf(_pipedPid);
    }

    public void setPipedPID(QName pipedPid) {
        _pipedPid = pipedPid == null ? null : pipedPid.toString();
    }

    public String getResource() {
        return _resource;
    }

    public void setResource(String resourceStr) {
        _resource = resourceStr;
    }

    public boolean isInstantiatingResource() {
        return _instantiatingResource;
    }

    public void setInstantiatingResource(boolean inst) {
        _instantiatingResource = inst;
    }
}
