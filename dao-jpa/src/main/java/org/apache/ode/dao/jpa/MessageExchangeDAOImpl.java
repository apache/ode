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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.*;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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

@Entity
@Table(name="ODE_MESSAGE_EXCHANGE")
@NamedQueries({
    @NamedQuery(name=MessageExchangeDAOImpl.DELETE_MEXS_BY_PROCESS, query="delete from MessageExchangeDAOImpl as m where m._process = :process"),
    @NamedQuery(name=MessageExchangeDAOImpl.SELECT_MEX_IDS_BY_PROCESS, query="select m._id from MessageExchangeDAOImpl as m where m._process = :process")
})
public class MessageExchangeDAOImpl extends OpenJPADAO implements MessageExchangeDAO, CorrelatorMessageDAO {
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
    @Basic @Column(name="SUBSCRIBER_COUNT")
    private int _subscriberCount;

    @OneToMany(targetEntity=MexProperty.class,mappedBy="_mex",fetch=FetchType.EAGER,cascade={CascadeType.ALL})
    private Collection<MexProperty> _props = new ArrayList<MexProperty>();
    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PROCESS_INSTANCE_ID")
    private ProcessInstanceDAOImpl _processInst;
    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PARTNER_LINK_ID")
    private PartnerLinkDAOImpl _partnerLink;
    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PROCESS_ID")
    private ProcessDAOImpl _process;
    @OneToOne(fetch=FetchType.LAZY,cascade={CascadeType.ALL}) @Column(name="REQUEST_MESSAGE_ID")
    private MessageDAOImpl _request;
    @OneToOne(fetch=FetchType.LAZY,cascade={CascadeType.ALL}) @Column(name="RESPONSE_MESSAGE_ID")
    private MessageDAOImpl _response;

    @ManyToOne(fetch= FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="CORR_ID")
    private CorrelatorDAOImpl _correlator;

    public MessageExchangeDAOImpl() {
    }
    
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

    void setCorrelationKeySet(CorrelationKeySet correlationKeySet) {
        _correlationKeys = correlationKeySet.toCanonicalString();
    }

    CorrelationKeySet getCorrelationKeySet() {
        return new CorrelationKeySet(_correlationKeys);
    }

    public CorrelationKey getCorrelationKey() {
        if (_correlationKeys == null) return null;
        return getCorrelationKeySet().iterator().next();
    }

    public void setCorrelationKey(CorrelationKey ckey) {
        _correlationKeys = ckey.toCanonicalString();
    }


    public void release(boolean doClean) {
        if( doClean ) {
            deleteMessages();
        }
    }

    public void releasePremieMessages() {
        // do nothing; early messages are deleted during CorrelatorDaoImpl().dequeueMessage()
    }

    public void deleteMessages() {
        if( __log.isDebugEnabled() ) __log.debug("Deleting message on MEX release.");
        
        getEM().remove(this); // This deletes MexProperty, REQUEST MessageDAO, RESPONSE MessageDAO
    }

    public CorrelatorDAOImpl getCorrelator() {
        return _correlator;
    }

    public void setCorrelator(CorrelatorDAOImpl correlator) {
        _correlator = correlator;
    }

    public int getSubscriberCount() {
        return _subscriberCount;
    }
    
    public void setSubscriberCount(int subscriberCount) {
        this._subscriberCount = subscriberCount;
    }

    public void incrementSubscriberCount() {
        ++_subscriberCount;
    }
    
    public void release() {
        // no-op for now, could be used to do some cleanup
    }

    public void setCreateTime(Date createTime) {
        _createTime = createTime;
    }
}
