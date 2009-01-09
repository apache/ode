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
package org.apache.ode.bpel.runtime;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.dao.jpa.BPELDAOConnectionFactoryImpl;
import org.apache.ode.il.EmbeddedGeronimoFactory;
import org.apache.ode.il.MockScheduler;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.hsqldb.jdbc.jdbcDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class MockBpelServer {

    DebugBpelServerImpl _server;

    ProcessStoreImpl _store;

    TransactionManager _txManager;

    Database _database;

    DataSource _dataSource;

    MockScheduler _scheduler;

    BpelDAOConnectionFactory _daoCF;

    EndpointReferenceContext _eprContext;

    MessageExchangeContext _mexContext;

    BindingContext _bindContext;

    HashMap<String, QName> _activated = new HashMap<String, QName>();

    HashMap<String, EndpointReference> _endpoints = new HashMap<String, EndpointReference>();

    public MockBpelServer() {
        try {
            _server = new DebugBpelServerImpl() ;
            
            createTransactionManager();
            createDataSource();
            createDAOConnection();
            createScheduler();
            if (_daoCF == null)
                throw new RuntimeException("No DAO");
            _server.setDaoConnectionFactory(_daoCF);
            if (_scheduler == null)
                throw new RuntimeException("No scheduler");
            _store = new ProcessStoreImpl(_eprContext, _dataSource,"jpa", new OdeConfigProperties(new Properties(), ""), true);
            _server.setTransactionManager(_txManager);
            _server.setScheduler(_scheduler);
            _server.setEndpointReferenceContext(createEndpointReferenceContext());
            _server.setMessageExchangeContext(createMessageExchangeContext());
            _server.setBindingContext(createBindingContext());
            _server.init();
            _server.start();
        } catch (Exception except) {
            System.err.println(except.getMessage());
            except.printStackTrace(System.err);
            throw new RuntimeException(except);
        }
    }

    public Collection<QName> deploy(File deploymentUnitDirectory) {
        Collection<QName> pids = _store.deploy(deploymentUnitDirectory);
        for (QName pid : pids)
            _server.register(_store.getProcessConfiguration(pid));
        return pids;
    }

    public void invoke(QName serviceName, String opName, Element body) throws Exception {
        String messageId = new GUID().toString();
        MyRoleMessageExchange mex;

        mex = _server.createMessageExchange(InvocationStyle.UNRELIABLE, serviceName, opName, "" + messageId);
        if (mex.getOperation() == null)
            throw new Exception("Did not find operation " + opName + " on service " + serviceName);
        Message request = mex.createMessage(mex.getOperation().getInput().getMessage().getQName());
        Element wrapper = body.getOwnerDocument().createElementNS("", "main");
        wrapper.appendChild(body);
        Element message = body.getOwnerDocument().createElementNS("", "message");
        message.appendChild(wrapper);
        request.setMessage(message);
        mex.setRequest(request);
        mex.invokeBlocking();
        mex.complete();

    }

    public TransactionManager getTransactionManager() {
        return _txManager;
    }

    public void shutdown() throws Exception {
        _server.stop();
        _scheduler.stop();
        _scheduler.shutdown();
    }

    protected TransactionManager createTransactionManager() throws Exception {
        EmbeddedGeronimoFactory factory = new EmbeddedGeronimoFactory();
        _txManager = factory.getTransactionManager();
        _txManager.setTransactionTimeout(30);
        return _txManager;
    }

    protected DataSource createDataSource() throws Exception {
        jdbcDataSource hsqlds = new jdbcDataSource();
        hsqlds.setDatabase("jdbc:hsqldb:mem:" + new GUID().toString());
        hsqlds.setUser("sa");
        hsqlds.setPassword("");
        _dataSource = hsqlds;
        return _dataSource;
    }

    protected Scheduler createScheduler() throws Exception {
        if (_server == null)
            throw new RuntimeException("No BPEL server");
        if (_txManager == null)
            throw new RuntimeException("No transaction manager");
        if (_dataSource == null)
            throw new RuntimeException("No data source");
        _scheduler = new MockScheduler(_txManager);
        _scheduler.setJobProcessor(_server);
        return _scheduler;
    }

    protected BpelDAOConnectionFactory createDAOConnection() throws Exception {
        if (_txManager == null)
            throw new RuntimeException("No transaction manager");
        if (_dataSource == null)
            throw new RuntimeException("No data source");

        BpelDAOConnectionFactoryJDBC daoCF = new BPELDAOConnectionFactoryImpl();
        daoCF.setDataSource(_dataSource);
        daoCF.setTransactionManager(_txManager);
        Properties props = new Properties();
        props.put("openjpa.Log", "log4j");
        props.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=false)");
        daoCF.init(props);
        _daoCF = daoCF;

        return _daoCF;
    }

    protected EndpointReferenceContext createEndpointReferenceContext() {
        _eprContext = new EndpointReferenceContext() {
            public EndpointReference resolveEndpointReference(Element element) {
                String service = DOMUtils.getChildCharacterData(element);
                return (EndpointReference) _endpoints.get(service);
            }

            public EndpointReference convertEndpoint(QName qName, Element element) {
                return null;
            }

            public Map getConfigLookup(EndpointReference epr) {
                return Collections.EMPTY_MAP;
            }
        };
        return _eprContext;
    }

    protected MessageExchangeContext createMessageExchangeContext() {
        _mexContext = new MessageExchangeContext() {
           
            public void onMyRoleMessageExchangeStateChanged(MyRoleMessageExchange myRoleMex) {
            }

            public void cancel(PartnerRoleMessageExchange mex) throws ContextException {
            }

            public Set<InvocationStyle> getSupportedInvocationStyle(PartnerRoleChannel prc, EndpointReference partnerEpr) {
                return Collections.singleton(InvocationStyle.UNRELIABLE);
            }

            public void invokePartnerUnreliable(PartnerRoleMessageExchange mex) throws ContextException {
            }

            public void invokePartnerReliable(PartnerRoleMessageExchange mex) throws ContextException {
            }

            public void invokePartnerTransacted(PartnerRoleMessageExchange mex) throws ContextException {
            }

            public void invokeRestful(RESTOutMessageExchange mex) throws ContextException {
            }
        };
        return _mexContext;
    }

    protected BindingContext createBindingContext() {
        _bindContext = new BindingContext() {
            public EndpointReference activateMyRoleEndpoint(QName processId, Endpoint myRoleEndpoint) {
                final Document doc = DOMUtils.newDocument();
                Element serviceRef = doc.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
                        EndpointReference.SERVICE_REF_QNAME.getLocalPart());
                serviceRef.appendChild(doc.createTextNode(myRoleEndpoint.serviceName.toString()));
                doc.appendChild(serviceRef);
                _activated.put(myRoleEndpoint.toString(), processId);
                return new EndpointReference() {
                    public Document toXML() {
                        return doc;
                    }
                };
            }

            public void deactivateMyRoleEndpoint(Endpoint myRoleEndpoint) {
                _activated.remove(myRoleEndpoint);
            }

            public PartnerRoleChannel createPartnerRoleChannel(QName processId, PortType portType,
                    final Endpoint initialPartnerEndpoint) {
                final EndpointReference epr = new EndpointReference() {
                    public Document toXML() {
                        Document doc = DOMUtils.newDocument();
                        Element serviceRef = doc.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
                                EndpointReference.SERVICE_REF_QNAME.getLocalPart());
                        serviceRef.appendChild(doc.createTextNode(initialPartnerEndpoint.serviceName.toString()));
                        doc.appendChild(serviceRef);
                        return doc;
                    }
                };
                _endpoints.put(initialPartnerEndpoint.serviceName.toString(), epr);
                return new PartnerRoleChannel() {
                    public EndpointReference getInitialEndpointReference() {
                        return epr;
                    }

                    public void close() {
                    };
                };
            }

            public void activateProvidedResource(Resource resource) {
                throw new UnsupportedOperationException();
            }

            public void deactivateProvidedResource(Resource resource) {
                throw new UnsupportedOperationException();
            }
        };
        return _bindContext;
    }

    
    static class DebugBpelServerImpl extends BpelServerImpl {
        public void waitForQuiessence() {
            super.waitForQuiessence();
        }        
    }


    public void waitForBlocking() {
        _server.waitForQuiessence();
    }
}
