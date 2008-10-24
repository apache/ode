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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

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
import org.apache.ode.bpel.rapi.PartnerLinkModel;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;

/**
 * Base implementation of the {@link MessageExchange} interface. This interfaces is exposed to the Integration Layer (IL) to allow
 * it to implement incoming (via {@link ReliableMyRoleMessageExchangeImpl}) and outgoing (via
 * {@link PartnerRoleMessageExchangeImpl}) communications.
 * 
 * It should be noted that this class and its derived classes are in NO WAY THREADSAFE. It is imperative that the integration layer
 * not attempt to use {@link MessageExchange} objects from multiple threads (although it is permitted to use the object from 
 * a different/new thread than the one from which the object originated, so long as only one thread is manipulating the object
 * at a time). 
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

    //
    // The following are immutable. 
    //
    
    final Contexts _contexts;
    final ODEProcess _process;
    final PartnerLinkModel _oplink;
    /** Message-exchange id. */
    final String _mexId;
    final PortType _portType;
    final Operation _operation;

    /** Instance identifier. */
    Long _iid;
    EndpointReference _epr;
    MessageImpl _request;

    /** The point at which this message-exchange will time out. */
    long _timeout = 30 * 1000;

    //
    // The following fields need to be volatile, since a random  IL thread may set them.
    //
    private volatile Status _status = Status.NEW;
    volatile QName _fault;
    volatile String _explanation;
    volatile MessageImpl _response;

    /** 
     * Used internally to sync memory between the thread that created this object (engine thread) and the thread that is
     * manipulating it (possibly a foregin IL thread). This is an alternative to needlessly maniuplating one of the above 
     */
    private volatile int _syncdummy;

    enum Change {
        EPR, ACK, RELEASE, REQUEST
    }

    final HashSet<Change> _changes = new HashSet<Change>();

    /** Properties that have been retrieved from the database. */
    final HashMap<String, String> _properties = new HashMap<String, String>();

    /** Names of properties that have been retrieved from the database. */
    final HashSet<String> _loadedProperties = new HashSet<String>();

    /** Names of proprties that have been modified. */
    final HashSet<String> _modifiedProperties = new HashSet<String>();

    protected FailureType _failureType;

    private Set<String> _propNames;

    private AckType _ackType;

    public MessageExchangeImpl(
            ODEProcess process,
            Long iid,
            String mexId, 
            PartnerLinkModel oplink,
            PortType ptype, Operation operation) {
        _process = process;
        _contexts = process._contexts;
        _mexId = mexId;
        _iid = iid;
        _oplink = oplink;
        _portType  = ptype;
        _operation = operation;
    }

    @Override
    public boolean equals(Object other) {
        return _mexId.equals(((MessageExchangeImpl)other)._mexId);
    }

    Long getIID() {
        return _iid;
    }
    
    void load(MessageExchangeDAO dao) {
        _timeout = dao.getTimeout();
        _iid = dao.getInstance() != null ? dao.getInstance().getInstanceId() : null;
        _ackType = dao.getAckType();
        _status = dao.getStatus();
        _fault = dao.getFault();
        _explanation = dao.getFaultExplanation();
    }

    void save(MessageExchangeDAO dao) {
        if (_oplink != null) dao.setPartnerLinkModelId(_oplink.getId());
        if (_operation != null) dao.setOperation(_operation.getName());
        dao.setStatus(_status);
        dao.setInvocationStyle(getInvocationStyle());
        dao.setFault(_fault);
        dao.setFaultExplanation(_explanation);
        dao.setTimeout(_timeout);
        dao.setFailureType(_failureType);
        dao.setAckType(_ackType);
       
        if (_changes.contains(Change.EPR)) {
            _changes.remove(Change.EPR);
            if (_epr != null) dao.setEPR(_epr.toXML().getDocumentElement());
            else dao.setEPR(null);
        }

        for (String modprop : _modifiedProperties) dao.setProperty(modprop, _properties.get(modprop));

        _modifiedProperties.clear();
    }

    void save() {
        doInTX(new InDbAction<Void>() {
            public Void call(MessageExchangeDAO mexdao) {
                save(mexdao);
                return null;
            }
        });
    }

    public abstract InvocationStyle getInvocationStyle();

    public boolean isSafe() {
        Object val = getOperation().getExtensionAttribute(SAFE_ATTRIBUTE);
        if (val == null)
            return false;
        try {
            return Boolean.valueOf(val.toString());
        } catch (Exception ex) {
            return false;
        }

    }

    public String getMessageExchangeId() throws BpelEngineException {
        return _mexId;
    }

    public String getOperationName() throws BpelEngineException {
        return getOperation().getName();
    }

    public MessageExchangePattern getMessageExchangePattern() {
        return _operation.getOutput()==null ? MessageExchangePattern.REQUEST_ONLY : MessageExchangePattern.REQUEST_RESPONSE; 
    }

    public boolean isTransactional() throws BpelEngineException {
        switch (getInvocationStyle()) {
            case TRANSACTED:
            case RELIABLE:
                return true;
        }
        
        return false;
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

    public AckType getAckType() {
        return _ackType;
    }
    
    public Operation getOperation() {
        return _operation;
    }

    public PortType getPortType() {
        return _portType;
    }

    public Message getRequest() {
        if (_request != null) return _request;

        return _request = doInTX(new InDbAction<MessageImpl>() {
            public MessageImpl call(MessageExchangeDAO dao) {
                MessageDAO req = dao.getRequest();
                if (req == null) return null;
                return new MemBackedMessageImpl(req.getHeader(), req.getData(), req.getType(), true);
            }
        });

    }

    public Message getResponse() {
        if (_response != null) return _response;

        return _response = doInTX(new InDbAction<MessageImpl>() {
            public MessageImpl call(MessageExchangeDAO dao) {
                MessageDAO req = dao.getResponse();
                if (req == null) return null;
                return new MemBackedMessageImpl(req.getHeader(), req.getData(), req.getType(), true);
            }
        });
    }

    
    void request() {
        _status = Status.REQ;
    }
    
    void ack(AckType ackType) {
        _status = Status.ACK;
        _ackType = ackType;
    }

    public Message createMessage(javax.xml.namespace.QName msgType) {
        return new MemBackedMessageImpl(null, null, msgType, false);
    }

    public void setEndpointReference(EndpointReference ref) {
        _epr = ref;
        _changes.add(Change.EPR);
    }

    public EndpointReference getEndpointReference() throws BpelEngineException {
        if (_epr != null)
            return _epr;

        return _epr = doInTX(new InDbAction<EndpointReference>() {

            public EndpointReference call(MessageExchangeDAO mexdao) {
                Element eprdao = mexdao.getEPR();
                return _epr = eprdao == null ? null : _contexts.eprContext.resolveEndpointReference(mexdao.getEPR());
            }

        });

    }

    public String getProperty(final String key) {
        if (!_loadedProperties.contains(key)) {
            _properties.put(key, doInTX(new InDbAction<String>() {
                public String call(MessageExchangeDAO mexdao) {
                    return mexdao.getProperty(key);
                }

            }));
            _loadedProperties.add(key);
        }

        return _properties.get(key);
    }

    public void setProperty(String key, String value) {
        _properties.put(key, value);
        _loadedProperties.add(key);
        _modifiedProperties.add(key);
    }

    public Set<String> getPropertyNames() {
        if (_propNames != null)
            return _propNames;

        return _propNames = doInTX(new InDbAction<Set<String>>() {
            public Set<String> call(MessageExchangeDAO mexdao) {
                return mexdao.getPropertyNames();
            }
        });

    }

    public long getTimeout() {
        return _timeout;
    }

    public void setTimeout(long timeout) {
        _timeout = timeout;
    }

    public void release() {
        __log.debug("Releasing mex " + getMessageExchangeId());
        _changes.add(Change.RELEASE);
    }

    public String toString() {
        return "MEX[" + _mexId + "]";
    }

    protected void assertTransaction() {
        if (!_contexts.isTransacted())
            throw new BpelEngineException("Operation must be performed in a transaction!");
    }

    protected <T> T doInTX(final InDbAction<T> action) {
        if (isTransactional()) {
            assertTransaction();
            return action.call(getDAO());
        } else {
            try {
                return _process.enqueueTransaction(new Callable<T>() {
                    public T call() throws Exception {
                        assertTransaction();
                        return action.call(getDAO());
                    }

                }).get();
            } catch (Exception ie) {
                __log.error("Internal error executing transaction.", ie);
                throw new BpelEngineException("Internal Error",ie);
            }
        }
    }

    /**
     * Get the DAO object. Note, we can do this only when we are running in a transaction.
     * 
     * @return 
     */
    protected MessageExchangeDAO getDAO() {
        assertTransaction();
        return _process.loadMexDao(_mexId);
    }

    interface InDbAction<T> {
        public T call(MessageExchangeDAO mexdao);
    }

    /** 
     * Force memory sync between the thread that updated the object and the engine thread. Note that this does not make 
     * the Mex objects "thread-safe", it just allows us to manipulate it from a different (single) thread than the original
     * one. Otherwise, Java memory model could render changes non-visible to the engine. 
     *
     */
    void sync() {
        ++_syncdummy;
    }

    protected static class ResponseFuture implements Future<Status> {
        private Status _status;

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public Status get() throws InterruptedException, ExecutionException {
            try {
                return get(0, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // If it's thrown it's definitely a bug
                throw new RuntimeException(e);
            }
        }

        public Status get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            synchronized (this) {
                if (_status != null)
                    return _status;

                this.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));

                if (_status == null) throw new TimeoutException();
                return _status;
            }
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return _status != null;
        }

        void done(Status status) {
            synchronized (this) {
                _status = status;
                this.notifyAll();
            }
        }
    }


}

