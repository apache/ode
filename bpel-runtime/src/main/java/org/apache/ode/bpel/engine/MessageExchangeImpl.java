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

package org.apache.ode.bpel.engine;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;

/**
 * Base implementation of the {@link MessageExchange} interface. This interfaces is exposed to the Integration Layer (IL)
 * to allow it to implement incoming (via {@link ReliableMyRoleMessageExchangeImpl}) and outgoing (via {@link PartnerRoleMessageExchangeImpl})
 * communications. 
 * 
 * It should be noted that this class and its derived classes are in NO WAY THREADSAFE. It is imperative that the integration layer
 * not attempt to use {@link MessageExchange} objects from multiple threads. 
 * 
 * @author Maciej Szefler
 *
 */
abstract class MessageExchangeImpl implements MessageExchange {

    /** Namespace for WSDL (2.0) extenions; we adopt them in WSDL 1.1 as well. */
    static final String WSDL2_EXTENSIONS_NS = "http://www.w3.org/ns/wsdl-extensions";

    /** WSDL extension attribute indicating whether operation is "safe". */
    static final QName SAFE_ATTRIBUTE = new QName(WSDL2_EXTENSIONS_NS, "safe");

    private static final Log __log = LogFactory.getLog(MessageExchangeImpl.class);

    protected static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    /** Instance identifier. */
    Long _iid;

    PortType _portType;

    Operation _operation;

    EndpointReference _epr;

    Status _status;

    MessageExchangePattern _pattern;

    String _opname;

    String _mexId;

    Boolean _txflag;

    QName _fault;

    String _explanation;

    MessageImpl _response;

    MessageImpl _request;

    Contexts _contexts;

    QName _callee;
    
    BpelEngineImpl _engine;

    boolean _associated;
    
    InvocationStyle _istyle;
    
    /** The point at which this message-exchange will time out. */
    Date _timeout;
   
    enum Change { 
        EPR,
        RESPONSE, 
        RELEASE
    }

    final HashSet<Change> _changes = new HashSet<Change>();
    
    /** Properties that have been retrieved from the database. */
    final HashMap<String, String> _properties = new HashMap<String,String>();
    
    /** Names of properties that have been retrieved from the database. */
    final HashSet<String> _loadedProperties = new HashSet<String>();

    /** Names of proprties that have been modified. */
    final HashSet<String> _modifiedProperties = new HashSet<String>();
    
    private FailureType _failureType;

    private Set<String> _propNames;
    
    public MessageExchangeImpl(BpelEngineImpl engine, String mexId) {
        _contexts = engine._contexts;
        _engine = engine;
        _mexId = mexId;
    }


    void load(MessageExchangeDAO dao) {
        if (dao.getMessageExchangeId().equals(_mexId))
            throw new IllegalArgumentException("MessageExchangeId mismatch!");
        
        if (_pattern == null)
            _pattern = MessageExchangePattern.valueOf(dao.getPattern());
        if (_opname == null)
            _opname = dao.getOperation();
        if (_mexId == null)
            _mexId = dao.getMessageExchangeId();
        if (_txflag == null)
            _txflag = dao.getPropagateTransactionFlag();
        if (_fault == null)
            _fault = dao.getFault();
        if (_explanation == null)
            _explanation = dao.getFaultExplanation();
        if (_status == null)
            _status = Status.valueOf(dao.getStatus());
        if (_callee == null)
            _callee = dao.getCallee();
        if (_istyle == null)
            _istyle = InvocationStyle.valueOf(dao.getInvocationStyle());
    }
    
    public void save(MessageExchangeDAO dao) {
        dao.setStatus(_status.toString());
        dao.setInvocationStyle(_istyle.toString());
        dao.setFault(_fault);
        dao.setFaultExplanation(_explanation);
        //todo: set failureType
        
        if (_changes.contains(Change.RESPONSE)) {
            MessageDAO responseDao = dao.createMessage(_response.getType());
            responseDao.setData(_response.getMessage());
        }
        
        if (_changes.contains(Change.EPR)) {
            if (_epr != null)
                dao.setEPR(_epr.toXML().getDocumentElement());
            else
                dao.setEPR(null);
        }
        
        for (String modprop : _modifiedProperties) {
            dao.setProperty(modprop, _properties.get(modprop));
        }

    }
    
    public InvocationStyle getInvocationStyle() {
        return _istyle;
    }

    public boolean isSafe() {
        Object val = getOperation().getExtensionAttribute(SAFE_ATTRIBUTE);
        if (val == null)
            return false;
        try {
            return new Boolean(val.toString());
        } catch (Exception ex) {
            return false;
        }

    }

    public String getMessageExchangeId() throws BpelEngineException {
        return _mexId;
    }

    public String getOperationName() throws BpelEngineException {
        return _opname;
    }

    public MessageExchangePattern getMessageExchangePattern() {
        return _pattern;
    }

    public boolean isTransactional() throws BpelEngineException {
        return _txflag;
    }

    public QName getFault() {
        return _fault;
    }

    public Message getFaultResponse() {
        return _fault == null ? null : getResponse();
    }

    public String getFaultExplanation() {
        return _explanation;
    }

    public Status getStatus() {
        return _status;
    }

    public Operation getOperation() {
        return _operation;
    }

    public PortType getPortType() {
        return _portType;
    }

    public Message getRequest() {
        if (_request != null)
            return _request;

        return _request = doInDb(new InDbAction<MessageImpl>() {
            public MessageImpl call(MessageExchangeDAO dao) {
                MessageDAO req = dao.getRequest();
                if (req == null)
                    return null;
                return new MemBackedMessageImpl(req.getData(),req.getType(),true);
            }
        });

    }

    public Message getResponse() {
        if (_response != null)
            return _response;

        return _response = doInDb(new InDbAction<MessageImpl>() {
            public MessageImpl call(MessageExchangeDAO dao) {
                MessageDAO req = dao.getResponse();
                if (req == null)
                    return null;
                return new MemBackedMessageImpl(req.getData(),req.getType(),true);
                
            }
        });
    }

    void setPortOp(PortType portType, Operation operation) {
        if (__log.isTraceEnabled())
            __log.trace("Mex[" + getMessageExchangeId() + "].setPortOp(" + portType + "," + operation + ")");
        _portType = portType;
        _operation = operation;
    }

    void setFault(QName faultType, Message outputFaultMessage) throws BpelEngineException {
        setStatus(Status.FAULT);
        _fault = faultType;
        _response = (MessageImpl) outputFaultMessage;
        
        _changes.add(Change.RESPONSE);
    }

    void setFaultExplanation(String explanation) {
        _explanation = explanation;
    }

    void setResponse(Message outputMessage) throws BpelEngineException {
        if (getStatus() != Status.REQUEST && getStatus() != Status.ASYNC)
            throw new IllegalStateException("Not in REQUEST state!");

        setStatus(Status.RESPONSE);
        _fault = null;
        _explanation = null;
        _response = (MessageImpl) outputMessage;
        _response.makeReadOnly();
        _changes.add(Change.RESPONSE);
        
    }

    void setFailure(FailureType type, String reason, Element details) throws BpelEngineException {
        // TODO not using FailureType, nor details
        setStatus(Status.FAILURE);
        _failureType = type;
        _explanation = reason;
        
        _changes.add(Change.RESPONSE);
    }

    void setStatus(Status status) {
        _status = status;
    }

    public Message createMessage(javax.xml.namespace.QName msgType) {
        return new MemBackedMessageImpl(null,msgType,false);
    }

    public void setEndpointReference(EndpointReference ref) {
        _epr = ref;
        _changes.add(Change.EPR);
    }

    public EndpointReference getEndpointReference() throws BpelEngineException {
        if (_epr != null)
            return _epr;

        return _epr = doInDb(new InDbAction<EndpointReference>() {

            public EndpointReference call(MessageExchangeDAO mexdao) {
                Element eprdao = mexdao.getEPR();
                return _epr = eprdao == null ? null : _contexts.eprContext.resolveEndpointReference(mexdao.getEPR());
            }

        });

    }


    public String getProperty(final String key) {
        if (!_loadedProperties.contains(key)) {
            _properties.put(key, doInDb(new InDbAction<String> () {
                public String call(MessageExchangeDAO mexdao) {
                    return mexdao.getProperty(key);
                }
                
            }));
            _loadedProperties.add(key);
        }

        return _properties.get(key);
    }

    public void setProperty(String key, String value) {
        _properties.put(key,value);
        _loadedProperties.add(key);
        _modifiedProperties.add(key);
    }

    public Set<String> getPropertyNames() {
        if (_propNames != null)
            return _propNames;
        
        return _propNames = doInDb(new InDbAction<Set<String>>() {
            public Set<String> call(MessageExchangeDAO mexdao) {
                return mexdao.getPropertyNames();
            }
        });
        
    }

    public void release() {
        __log.debug("Releasing mex " + getMessageExchangeId());
        _changes.add(Change.RELEASE);
    }

    public String toString() {
        return "MEX[" + _mexId + "]";
    }
    
    protected void assertTransaction() {
        if (!_contexts.scheduler.isTransacted())
            throw new BpelEngineException("Operation must be performed in a transaction!");
    }

    protected <T> T doInDb(InDbAction<T> action) {
        if (_txflag) {
            MessageExchangeDAO mexDao;
            action.call(mexDao);
        } else {
        }
    }

    interface InDbAction<T> {
        public T call(MessageExchangeDAO mexdao);
    }
    
}
