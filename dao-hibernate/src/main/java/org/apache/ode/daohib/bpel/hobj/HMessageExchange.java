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
package org.apache.ode.daohib.bpel.hobj;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Hibernate-managed table for keeping track of message exchanges.
 * 
 * @hibernate.class table="BPEL_MESSAGE_EXCHANGE" dynamic-update="true"
 * @hibernate.query name="SELECT_MEX_IDS_BY_INSTANCES" query="select id from HMessageExchange as m where m.instance in (:instances)"
 * @hibernate.query name="SELECT_UNMATCHED_MEX_BY_INSTANCES" query="from HMessageExchange as m where m in(select cm.messageExchange from HCorrelatorMessage as cm where cm.messageExchange.instance in (:instances))"
 */
public class HMessageExchange extends HObject {
    public final static String SELECT_MEX_IDS_BY_INSTANCES = "SELECT_MEX_IDS_BY_INSTANCES";
    public final static String SELECT_UNMATCHED_MEX_BY_INSTANCES = "SELECT_UNMATCHED_MEX_BY_INSTANCES";

    private String _channelName;

    private String _operationName;

    private String _state;

    private Date _insertTime;

    private String _portType;

    private HLargeData _endpoint;

    private HLargeData _callbackEndpoint;

    private HMessage _request;

    private HMessage _response;
    
    private HPartnerLink _partnerLink;

    private String _clientKey;

    private HProcessInstance _instance;

    private HProcess _process;

    private char _dir;

    private int _plinkModelId;

    private String _pattern;

    private String _corrstatus;

    private String _faultType;

    private String _faultExplanation;

    private String _callee;

    private String _pipedMessageExchangeId;
    
    private int _subscriberCount;

    private Map<String, String> _properties = new HashMap<String, String>();

    /**
     * 
     */
    public HMessageExchange() {
        super();
    }

    /**
     * @hibernate.property column="PORT_TYPE"
     */
    public String getPortType() {
        return _portType;
    }

    public void setPortType(String portType) {
        _portType = portType;
    }

    /**
     * @hibernate.property column="CHANNEL_NAME"
     */
    public String getChannelName() {
        return _channelName;
    }

    public void setChannelName(String channelName) {
        _channelName = channelName;
    }

    /**
     * @hibernate.property column="CLIENTKEY"
     */
    public String getClientKey() {
        return _clientKey;
    }

    public void setClientKey(String clientKey) {
        _clientKey = clientKey;
    }

    /**
     * @hibernate.many-to-one column="LDATA_EPR_ID" cascade="delete" foreign-key="none"
     */
    public HLargeData getEndpoint() {
        return _endpoint;
    }

    public void setEndpoint(HLargeData endpoint) {
        _endpoint = endpoint;
    }

    /**
     * @hibernate.many-to-one column="LDATA_CEPR_ID" cascade="delete" foreign-key="none"
     */
    public HLargeData getCallbackEndpoint() {
        return _callbackEndpoint;
    }

    public void setCallbackEndpoint(HLargeData endpoint) {
        _callbackEndpoint = endpoint;
    }

    /**
     * @hibernate.many-to-one column="REQUEST" cascade="delete" foreign-key="none"
     */
    public HMessage getRequest() {
        return _request;
    }

    public void setRequest(HMessage request) {
        _request = request;
    }

    /**
     * @hibernate.many-to-one column="RESPONSE" cascade="delete" foreign-key="none"
     */
    public HMessage getResponse() {
        return _response;
    }

    public void setResponse(HMessage response) {
        _response = response;
    }

    /**
     * @hibernate.property column="INSERT_DT"
     */
    public Date getInsertTime() {
        return _insertTime;
    }

    public void setInsertTime(Date insertTime) {
        _insertTime = insertTime;
    }

    /**
     * @hibernate.property column="OPERATION"
     */
    public String getOperationName() {
        return _operationName;
    }

    public void setOperationName(String operationName) {
        _operationName = operationName;
    }

    /**
     * @hibernate.property column="STATE"
     */
    public String getState() {
        return _state;
    }

    public void setState(String state) {
        _state = state;
    }

    /**
     * @hibernate.many-to-one column="PROCESS" foreign-key="none"
     */
    public HProcess getProcess() {
        return _process;
    }

    public void setProcess(HProcess process) {
        _process = process;
    }

    /**
     * @hibernate.many-to-one column="PIID" foreign-key="none"
     */
    public HProcessInstance getInstance() {
        return _instance;
    }

    public void setInstance(HProcessInstance instance) {
        _instance = instance;
    }

    public void setDirection(char dir) {
        _dir = dir;
    }

    /**
     * @hibernate.property column="DIR"
     */
    public char getDirection() {
        return _dir;
    }

    /**
     * @hibernate.property column="PLINK_MODELID"
     */
    public int getPartnerLinkModelId() {
        return _plinkModelId;
    }

    public void setPartnerLinkModelId(int id) {
        _plinkModelId = id;
    }

    /**
     * @hibernate.property column="PATTERN"
     */
    public String getPattern() {
        return _pattern;
    }

    public void setPattern(String pattern) {
        _pattern = pattern;

    }

    /**
     * @hibernate.property column="CORR_STATUS"
     * @return
     */
    public String getCorrelationStatus() {
        return _corrstatus;
    }

    public void setCorrelationStatus(String cstatus) {
        _corrstatus = cstatus;

    }

    /**
     * @hibernate.property column="FAULT_TYPE"
     * @return
     */
    public String getFault() {
        return _faultType;
    }

    public void setFault(String faultType) {
        _faultType = faultType;

    }

    /**
     * @hibernate.property column="FAULT_EXPL"
     * @return
     */
    public String getFaultExplanation() {
        return _faultExplanation;
    }

    public void setFaultExplanation(String faultExplanation) {
        if (faultExplanation != null && faultExplanation.length() > 255)
            faultExplanation = faultExplanation.substring(0, 254);
        _faultExplanation = faultExplanation;
    }

    /**
     * @hibernate.property column="CALLEE"
     */
    public String getCallee() {
        return _callee;
    }

    public void setCallee(String callee) {
        _callee = callee;
    }

    /**
     * @hibernate.map name="properties" table="BPEL_MEX_PROPS" lazy="true" cascade="delete"
     * @hibernate.collection-key name="mex" column="MEX" foreign-key="none"
     * @hibernate.collection-index column="NAME" type="string"
     * @hibernate.collection-element column="VALUE" type="string" length="8000"
     */
    public Map<String, String> getProperties() {
        return _properties;
    }

    public void setProperties(Map<String, String> props) {
        _properties = props;
    }

    public void setPartnerLink(HPartnerLink link) {
        _partnerLink = link;
    }

    /**
     * @hibernate.many-to-one column="PARTNERLINK" foreign-key="none"
     */
    public HPartnerLink getPartnerLink() {
        return _partnerLink;
    }

    /**
     * @hibernate.property column="PIPED_ID"
     */
    public String getPipedMessageExchangeId() {
        return _pipedMessageExchangeId;
    }

    public void setPipedMessageExchangeId(String pipedMessageExchangeId) {
        _pipedMessageExchangeId = pipedMessageExchangeId;
    }
    
    /**
     * @hibernate.property column="SUBSCRIBER_COUNT"
     */
    public int getSubscriberCount() {
    	return _subscriberCount;
    }
    
    public void setSubscriberCount(int subscriberCount) {
    	this._subscriberCount = subscriberCount;
    }
    
    public void incrementSubscriberCount() {
    	setSubscriberCount(getSubscriberCount() + 1);
    }
}
