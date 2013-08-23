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

import org.apache.derby.jdbc.EmbeddedXADataSource;
import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.Scheduler.MapSerializableRunnable;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.dao.jpa.BPELDAOConnectionFactoryImpl;
import org.apache.ode.il.EmbeddedGeronimoFactory;
import org.apache.ode.il.MockScheduler;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.hsqldb.jdbc.jdbcDataSource;
import org.tranql.connector.derby.EmbeddedLocalMCF;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.resource.spi.ConnectionManager;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


class MockBpelServer {

    BpelServerImpl            _server;
    ProcessStoreImpl          _store;
    TransactionManager        _txManager;
    Database                  _database;
    DataSource                _dataSource;
    SchedulerWrapper          _scheduler;
    BpelDAOConnectionFactory  _daoCF;
    EndpointReferenceContext  _eprContext;
    MessageExchangeContext    _mexContext;
    BindingContext            _bindContext;
    HashMap<String, QName>    _activated = new HashMap<String, QName>();
    @SuppressWarnings("unchecked")
    HashMap                   _endpoints = new HashMap();

     public MockBpelServer() {
        try {
            _server = new BpelServerImpl();
            createTransactionManager();
            createDataSource();
            createScheduler();
            createDAOConnection();
            if (_daoCF == null)
                throw new RuntimeException("No DAO");
            _server.setDaoConnectionFactory(_daoCF);
            _server.setInMemDaoConnectionFactory(new BpelDAOConnectionFactoryImpl(_scheduler));
            if (_scheduler == null)
                throw new RuntimeException("No scheduler");
            createEndpointReferenceContext();
            Properties storeProps = new Properties();
            storeProps.setProperty("hibernate.hbm2ddl.auto", "update");
            _store = new ProcessStoreImpl(_eprContext, _dataSource,"hib", new OdeConfigProperties(storeProps, ""), true);
            _server.setScheduler(_scheduler);
            _server.setEndpointReferenceContext(_eprContext);
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
        for (QName pid: pids)
            _server.register(_store.getProcessConfiguration(pid));
        return pids;
    }

    public void invoke(QName serviceName, String opName, Element body) throws Exception {
        try {
            String messageId = new GUID().toString();
            MyRoleMessageExchange mex;

            _txManager.begin();
            mex = _server.getEngine().createMessageExchange("" + messageId, serviceName, opName);
            if (mex.getOperation() == null)
                throw new Exception("Did not find operation " + opName + " on service " + serviceName);
            Message request = mex.createMessage(mex.getOperation().getInput().getMessage().getQName());
            Element wrapper = body.getOwnerDocument().createElementNS("", "main");
            wrapper.appendChild(body);
            Element message = body.getOwnerDocument().createElementNS("", "message");
            message.appendChild(wrapper);
            request.setMessage(message);
            mex.invoke(request);
            mex.complete();
            _txManager.commit();
        } catch (Exception except) {
              _txManager.rollback();
              throw except;
        }
    }

    public TransactionManager getTransactionManager() {
        return _txManager;
    }

    public void waitForBlocking() {
        try {
            long delay = 1000;
            while (true) {
                // Be warned: ugly hack and not safe for slow CPUs.
                long cutoff = System.currentTimeMillis() - delay;
                if (_scheduler._nextSchedule < cutoff)
                    break;
                Thread.sleep(delay);
            }
        } catch (InterruptedException except) { }
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
        TransactionSupport transactionSupport = LocalTransactions.INSTANCE;
        ConnectionTracker connectionTracker = new ConnectionTrackingCoordinator();

        PoolingSupport poolingSupport = new SinglePool(
                10,
                0,
                1000,
                1,
                true,
                false,
                false);

        ConnectionManager connectionManager = new GenericConnectionManager(
                    transactionSupport,
                    poolingSupport,
                    null,
                    connectionTracker,
                    (RecoverableTransactionManager) _txManager,
                    getClass().getName(),
                    getClass().getClassLoader());

        
            EmbeddedLocalMCF mcf = new org.tranql.connector.derby.EmbeddedLocalMCF();
            mcf.setCreateDatabase(true);
            mcf.setDatabaseName("target/testdb");
            mcf.setUserName("sa");
            mcf.setPassword("");
            _dataSource = (DataSource) mcf.createConnectionFactory(connectionManager);
            return _dataSource;
        
        
//        d = org.tranql.connector.jdbc.JDBCDriverMCF();
//        EmbeddedXADataSource ds = new EmbeddedXADataSource();
//        ds.setCreateDatabase("create");
//        ds.setDatabaseName("target/testdb");
//        ds.setUser("sa");
//        ds.setPassword("");
//        _dataSource = ds;
//        return _dataSource;
    }

    protected Scheduler createScheduler() throws Exception {
        if (_server == null)
            throw new RuntimeException("No BPEL server");
        if (_txManager == null)
            throw new RuntimeException("No transaction manager");
        if (_dataSource == null)
            throw new RuntimeException("No data source");
        _scheduler = new SchedulerWrapper(_server, _txManager, _dataSource);
        return _scheduler;
    }

    protected BpelDAOConnectionFactory createDAOConnection() throws Exception {
        if (_txManager == null)
            throw new RuntimeException("No transaction manager");
        if (_dataSource == null)
            throw new RuntimeException("No data source");

//        
//        BpelDAOConnectionFactoryJDBC daoCF = new BPELDAOConnectionFactoryImpl();
//        daoCF.setDataSource(_dataSource);
//        daoCF.setTransactionManager(_txManager);
//        Properties props = new Properties();
//        props.put("openjpa.Log", "log4j");
//        props.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=false)");
//        daoCF.init(props);
//        _daoCF = daoCF;
        org.apache.ode.daohib.bpel.BpelDAOConnectionFactoryImpl daoCF = new org.apache.ode.daohib.bpel.BpelDAOConnectionFactoryImpl();
        daoCF.setDataSource(_dataSource);
        daoCF.setTransactionManager(_txManager);
        Properties props = new Properties();
        props.setProperty("hibernate.hbm2ddl.auto", "update");
        daoCF.init(props);

        _daoCF = daoCF;
        return _daoCF;
    }

    protected EndpointReferenceContext createEndpointReferenceContext() {
        _eprContext = new EndpointReferenceContext() {
            public EndpointReference resolveEndpointReference(Element element) {
                String service = DOMUtils.getChildCharacterData(element);
                return (EndpointReference)_endpoints.get(service);
            }
            public EndpointReference convertEndpoint(QName qName, Element element) { return null; }

            @SuppressWarnings("unchecked")
            public Map getConfigLookup(EndpointReference epr) {
                return Collections.EMPTY_MAP;
            }
        };
        return _eprContext;
    }

    protected MessageExchangeContext createMessageExchangeContext() {
       _mexContext =  new MessageExchangeContext() {
            public void invokePartner(PartnerRoleMessageExchange mex) { }
            public void onAsyncReply(MyRoleMessageExchange myRoleMex) { }
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
                    public Document toXML() { return doc; }
                };
            }

            public void deactivateMyRoleEndpoint(Endpoint myRoleEndpoint) {
                _activated.remove(myRoleEndpoint);
            }

            @SuppressWarnings("unchecked")
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
                    public EndpointReference getInitialEndpointReference() { return epr; }
                    public void close() { };
                };
            }

            public long calculateSizeofService(EndpointReference epr) {
                return 0;
            }
        };
        return _bindContext;
    }


    private class SchedulerWrapper implements Scheduler {

        MockScheduler _scheduler;
        long _nextSchedule;

        SchedulerWrapper(BpelServerImpl server, TransactionManager txManager, DataSource dataSource) {
            ExecutorService executorService = Executors.newCachedThreadPool();
            _scheduler = new MockScheduler(_txManager);
            _scheduler.setExecutorSvc(executorService);
            _scheduler.setJobProcessor(server);
        }

        public String schedulePersistedJob(JobDetails jobDetail,Date when) throws ContextException {
            String jobId = _scheduler.schedulePersistedJob(jobDetail, when);
            // Invocation checks get scheduled much later, we don't want (or need) to wait for them
            if (jobDetail.getType() != JobType.INVOKE_CHECK)
                _nextSchedule = when == null ?  System.currentTimeMillis() : when.getTime();
            return jobId;
        }

        public String scheduleMapSerializableRunnable(MapSerializableRunnable runnable, Date when) throws ContextException {
            runnable.run();
            return new GUID().toString();
        }

        public String scheduleVolatileJob(boolean transacted, JobDetails jobDetail) throws ContextException {
            return scheduleVolatileJob(transacted, jobDetail, null);
        }

        public String scheduleVolatileJob(boolean transacted, JobDetails jobDetail, Date when) throws ContextException {
            String jobId = _scheduler.scheduleVolatileJob(transacted, jobDetail, when);
            _nextSchedule = System.currentTimeMillis();
            return jobId;
        }

        public void cancelJob(String jobId) throws ContextException {
            _scheduler.cancelJob(jobId);
        }

        public <T> T execTransaction(Callable<T> transaction) throws Exception, ContextException {
            return _scheduler.execTransaction(transaction, 0);
        }
        
        public <T> T execTransaction(Callable<T> transaction, int timeout) throws Exception, ContextException {
       		return _scheduler.execTransaction(transaction, timeout);
        }

        public void beginTransaction() throws Exception {
            _scheduler.beginTransaction();
        }

        public void commitTransaction() throws Exception {
            _scheduler.commitTransaction();
        }

        public void rollbackTransaction() throws Exception {
            _scheduler.rollbackTransaction();
        }

        public void setRollbackOnly() throws Exception {
            _scheduler.setRollbackOnly();
        }

        public <T> Future<T> execIsolatedTransaction(Callable<T> transaction) throws Exception, ContextException {
            return _scheduler.execIsolatedTransaction(transaction);
        }

        public boolean isTransacted() {
            return _scheduler.isTransacted();
        }

        public void start() { _scheduler.start(); }
        public void stop() { _scheduler.stop(); }
        public void shutdown() { _scheduler.shutdown(); }
        public void acquireTransactionLocks() { _scheduler.acquireTransactionLocks(); }

        public void registerSynchronizer(Synchronizer synch) throws ContextException {
            _scheduler.registerSynchronizer(synch);
        }

        public void setJobProcessor(JobProcessor processor) throws ContextException {
            _scheduler.setJobProcessor(processor);

        }

        public void setPolledRunnableProcesser(JobProcessor delegatedRunnableProcessor) {
        }

		public boolean amICoordinator() {
			return true;
		}
    }
}
