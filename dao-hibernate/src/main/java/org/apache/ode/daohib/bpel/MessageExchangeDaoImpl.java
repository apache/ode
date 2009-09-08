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

package org.apache.ode.daohib.bpel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorMessage;
import org.apache.ode.daohib.bpel.hobj.HLargeData;
import org.apache.ode.daohib.bpel.hobj.HMessage;
import org.apache.ode.daohib.bpel.hobj.HMessageExchange;
import org.apache.ode.daohib.bpel.hobj.HProcess;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

public class MessageExchangeDaoImpl extends HibernateDao implements
        MessageExchangeDAO {
    @SuppressWarnings("unused")
    private static final Log __log = LogFactory.getLog(MessageExchangeDaoImpl.class);
    
    private HMessageExchange _hself;

    // Used when provided process and instance aren't hibernate implementations. The relation
    // therefore can't be persisted. Used for in-mem DAOs so that doesn't matter much. 
    private ProcessDAO _externalProcess;
    private ProcessInstanceDAO _externalInstance;

    public MessageExchangeDaoImpl(SessionManager sm, HMessageExchange mex) {
        super(sm, mex);
        entering("MessageExchangeDaoImpl.MessageExchangeDaoImpl");
        _hself = mex;
    }

    public String getMessageExchangeId() {
        return _hself.getId().toString();
    }

    public MessageDAO getResponse() {
        entering("MessageExchangeDaoImpl.getResponse");
        return _hself.getResponse() == null ? null : new MessageDaoImpl(_sm, _hself.getResponse());
    }

    public Date getCreateTime() {
        return _hself.getInsertTime();
    }

    public void setCreateTime(Date createTime) {
        _hself.setInsertTime(createTime);
    }

    public MessageDAO getRequest() {
        entering("MessageExchangeDaoImpl.getRequest");
        return _hself.getRequest() == null ? null : new MessageDaoImpl(_sm, _hself.getRequest());
    }

    public String getOperation() {
        return _hself.getOperationName();
    }

    public QName getPortType() {
        return _hself.getPortType() == null ? null : QName.valueOf(_hself.getPortType());
    }

    public void setPortType(QName porttype) {
        entering("MessageExchangeDaoImpl.setPortType");
        _hself.setPortType(porttype == null ? null : porttype.toString());
        update();
    }

    public void setStatus(String status) {
        entering("MessageExchangeDaoImpl.setStatus");
        _hself.setState(status);
        update();
    }

    public String getStatus() {
        return _hself.getState();
    }

    public MessageDAO createMessage(QName type) {
        entering("MessageExchangeDaoImpl.createMessage");
        HMessage message = new HMessage();
        message.setType(type == null ? null : type.toString());
        message.setCreated(new Date());
        message.setMessageExchange(_hself);
        getSession().save(message);
        return new MessageDaoImpl(_sm, message);

    }

    public void setRequest(MessageDAO msg) {
        entering("MessageExchangeDaoImpl.setRequest");
        _hself.setRequest(msg == null ? null : (HMessage) ((MessageDaoImpl) msg).getHibernateObj());
        update();
    }

    public void setResponse(MessageDAO msg) {
        entering("MessageExchangeDaoImpl.setResponse");
        _hself.setResponse(msg == null ? null : (HMessage) ((MessageDaoImpl) msg).getHibernateObj());
        update();
    }

    public int getPartnerLinkModelId() {
        return _hself.getPartnerLinkModelId();
    }

    public void setPartnerLinkModelId(int modelId) {
        entering("MessageExchangeDaoImpl.setPartnerLinkModelId");
        _hself.setPartnerLinkModelId(modelId);
        update();
    }

    public String getCorrelationId() {
        return _hself.getClientKey();
    }

    public void setCorrelationId(String clientKey) {
        entering("MessageExchangeDaoImpl.setCorrelationId");
        _hself.setClientKey(clientKey);
        update();
    }

    public void setPattern(String pattern) {
        entering("MessageExchangeDaoImpl.setPattern");
        _hself.setPattern(pattern);
        update();

    }

    public void setOperation(String opname) {
        entering("MessageExchangeDaoImpl.setOperation");
        _hself.setOperationName(opname);
        update();
    }

    public void setEPR(Element source) {
        entering("MessageExchangeDaoImpl.setEPR");
        if (source == null)
            _hself.setEndpoint(null);
        else {
            HLargeData ld = new HLargeData(DOMUtils.domToString(source));
            getSession().save(ld);
            _hself.setEndpoint(ld);
        }

        getSession().saveOrUpdate(_hself);

    }

    public Element getEPR() {
        entering("MessageExchangeDaoImpl.getEPR");
        HLargeData ld = _hself.getEndpoint();
        if (ld == null)
            return null;
        try {
            return DOMUtils.stringToDOM(ld.getText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setCallbackEPR(Element source) {
        entering("MessageExchangeDaoImpl.setCallbackEPR");
        if (source == null)
            _hself.setCallbackEndpoint(null);
        else {
            HLargeData ld = new HLargeData(DOMUtils.domToString(source));
            getSession().save(ld);
            _hself.setCallbackEndpoint(ld);
        }

        getSession().saveOrUpdate(_hself);

    }

    public Element getCallbackEPR() {
        entering("MessageExchangeDaoImpl.getCallbackEPR");
        HLargeData ld = _hself.getCallbackEndpoint();
        if (ld == null)
            return null;
        try {
            return DOMUtils.stringToDOM(ld.getText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPattern() {
        return _hself.getPattern();
    }

    public String getChannel() {
        return _hself.getChannelName();
    }

    public void setChannel(String channel) {
        entering("MessageExchangeDaoImpl.setChannel");
        _hself.setChannelName(channel);
        update();
    }

    public boolean getPropagateTransactionFlag() {
        // TODO Auto-generated method stub
        return false;
    }

    public QName getFault() {
        return _hself.getFault() == null ? null : QName.valueOf(_hself.getFault());
    }

    public void setFault(QName faultType) {
        entering("MessageExchangeDaoImpl.setFault");
        _hself.setFault(faultType == null ? null : faultType.toString());
        update();
    }

    public String getFaultExplanation() {
        return _hself.getFaultExplanation();
    }

    public void setFaultExplanation(String explanation) {
        entering("MessageExchangeDaoImpl.setFaultExplanation");
        _hself.setFaultExplanation(explanation);
        update();
    }

    public void setCorrelationStatus(String cstatus) {
        entering("MessageExchangeDaoImpl.setCorrelationStatus");
        _hself.setCorrelationStatus(cstatus);
        update();
    }

    public String getCorrelationStatus() {
        return _hself.getCorrelationStatus();
    }

    public ProcessDAO getProcess() {
        entering("MessageExchangeDaoImpl.getProcess");
        if (_externalProcess != null) return _externalProcess;
        else return _hself.getProcess() == null ? null : new ProcessDaoImpl(_sm, _hself.getProcess());
    }

    public void setProcess(ProcessDAO process) {
        entering("MessageExchangeDaoImpl.setProcess");
        if (process == null || process instanceof ProcessDaoImpl) {
        _hself.setProcess(process == null ? null : (HProcess) ((ProcessDaoImpl) process).getHibernateObj());
        update();
        } else {
            _externalProcess = process;
    }
    }

    public void setInstance(ProcessInstanceDAO instance) {
        entering("MessageExchangeDaoImpl.setInstance");
        if (instance == null || instance instanceof ProcessInstanceDaoImpl) {
        _hself.setInstance(instance == null ? null : (HProcessInstance) ((ProcessInstanceDaoImpl) instance)
                .getHibernateObj());
        update();
        } else {
            _externalInstance = instance;
    }

    }

    public ProcessInstanceDAO getInstance() {
        entering("MessageExchangeDaoImpl.getInstance");
        if (_externalInstance != null) return _externalInstance;
        else return _hself.getInstance() == null ? null : new ProcessInstanceDaoImpl(_sm, _hself.getInstance());
    }

    public char getDirection() {
        return _hself.getDirection();
    }

    public QName getCallee() {
        String callee = _hself.getCallee();
        return callee == null ? null : QName.valueOf(callee);
    }

    public void setCallee(QName callee) {
        entering("MessageExchangeDaoImpl.setCallee");
        _hself.setCallee(callee == null ? null : callee.toString());
        update();
    }

    public String getProperty(String key) {
        entering("MessageExchangeDaoImpl.getProperty");
        return _hself.getProperties().get(key);
    }

    public void setProperty(String key, String value) {
        entering("MessageExchangeDaoImpl.setProperty");
        _hself.getProperties().put(key, value);
        update();
    }

    public void setPartnerLink(PartnerLinkDAO plinkDAO) {
        entering("MessageExchangeDaoImpl.setPartnerLink");
        _hself.setPartnerLink(((PartnerLinkDAOImpl) plinkDAO)._self);
        update();
    }

    public PartnerLinkDAO getPartnerLink() {
        entering("MessageExchangeDaoImpl.getPartnerLink");
        return new PartnerLinkDAOImpl(_sm, _hself.getPartnerLink());
    }

    public Set<String> getPropertyNames() {
        entering("MessageExchangeDaoImpl.getPropertyNames");
        return Collections.unmodifiableSet(_hself.getProperties().keySet());
    }

    public String getPipedMessageExchangeId() {
        return _hself.getPipedMessageExchangeId();
    }

    public void setPipedMessageExchangeId(String mexId) {
        entering("MessageExchangeDaoImpl.setPipedMessageExchangeId");
        _hself.setPipedMessageExchangeId(mexId);
    }

    public int getSubscriberCount() {
        return _hself.getSubscriberCount();
    }
    
    public void setSubscriberCount(int subscriberCount) {
        _hself.setSubscriberCount(subscriberCount);        
    }
    
    public void release(boolean doClean) {
        if( doClean ) {
            deleteMessages();
        }
    }

    @SuppressWarnings("unchecked")
    public void releasePremieMessages() {
        deleteByIds(HLargeData.class, getSession().getNamedQuery(HLargeData.SELECT_MESSAGE_LDATA_IDS_BY_MEX_1).setParameter("mex", _hself).list());
        deleteByIds(HLargeData.class, getSession().getNamedQuery(HLargeData.SELECT_MESSAGE_LDATA_IDS_BY_MEX_2).setParameter("mex", _hself).list());
        deleteByIds(HCorrelatorMessage.class, getSession().getNamedQuery(HCorrelatorMessage.SELECT_CORMESSAGE_IDS_BY_MEX).setParameter("mex", _hself).list());
    }

    public void incrementSubscriberCount() {
        _hself.incrementSubscriberCount();
    }
    
    @SuppressWarnings("unchecked")
    public void deleteMessages() {
        deleteByIds(HLargeData.class, getSession().getNamedQuery(HLargeData.SELECT_MESSAGE_LDATA_IDS_BY_MEX_1).setParameter("mex", _hself).list());
        deleteByIds(HLargeData.class, getSession().getNamedQuery(HLargeData.SELECT_MESSAGE_LDATA_IDS_BY_MEX_2).setParameter("mex", _hself).list());
        deleteByIds(HCorrelatorMessage.class, getSession().getNamedQuery(HCorrelatorMessage.SELECT_CORMESSAGE_IDS_BY_MEX).setParameter("mex", _hself).list());
          
        getSession().delete(_hself);
        // This deletes endpoint LData, callbackEndpoint LData, request HMessage, response HMessage, HMessageExchangeProperty 
    }
}
