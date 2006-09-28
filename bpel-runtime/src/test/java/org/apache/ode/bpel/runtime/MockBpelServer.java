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
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.DeploymentUnit;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.apache.ode.bpel.scheduler.quartz.QuartzSchedulerImpl;
import org.apache.ode.daohib.DataSourceConnectionProvider;
import org.apache.ode.daohib.HibernateTransactionManagerLookup;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.BpelDAOConnectionFactoryImpl;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.hibernate.cfg.Environment;
import org.objectweb.jotm.Jotm;
import org.opentools.minerva.MinervaPool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fs.naming.mem.InMemoryContextFactory;


class MockBpelServer {

  BpelServerImpl            _server;
  TransactionManager        _txManager;
  Jotm                      _jotm;
  MinervaPool               _minervaPool;
  DataSource                _dataSource;
  SchedulerWrapper          _scheduler;
  BpelDAOConnectionFactory  _daoCF;
  EndpointReferenceContext  _eprContext;
  MessageExchangeContext    _mexContext;
  BindingContext            _bindContext;
  HashMap<String, QName>    _activated = new HashMap();
  HashMap                   _endpoints = new HashMap();


  public MockBpelServer() {
    try {
      _server = new BpelServerImpl();
      createTransactionManager();
      createDataSource();
      createDAOConnection();
      createScheduler();

      if (_daoCF == null)
        throw new RuntimeException("No DAO");
      _server.setDaoConnectionFactory(_daoCF);
      if (_scheduler == null)
        throw new RuntimeException("No scheduler");
      _server.setScheduler(_scheduler);
      _server.setEndpointReferenceContext(createEndpointReferenceContext());
      _server.setMessageExchangeContext(createMessageExchangeContext());
      _server.setBindingContext(createBindingContext());
      _server.setDeployDir("processes");
      _server.init();
      _server.start();
    } catch (Exception except) {
      System.err.println(except.getMessage());
      except.printStackTrace(System.err);
      throw new RuntimeException(except);
    }
  }

  public Collection<QName> deploy(File deploymentUnitDirectory) {
    Collection<QName> pids = _server.deploy(deploymentUnitDirectory);
    for (QName pid: pids)
      _server.activate(pid, true);
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
      request.setMessage(body);
      mex.invoke(request);
      mex.complete();
      _txManager.commit();
    } catch (Exception except) {
      _txManager.rollback();
      throw except;
    }
/*
        // Preparing a callback just in case we would need one.
        if (odeMex.getOperation().getOutput() != null) {
          callback = new ResponseCallback();
          _waitingCallbacks.put(odeMex.getClientId(), callback);
        }

        if (__log.isDebugEnabled()) {
          __log.debug("Invoking ODE using MEX " + odeMex);
          __log.debug("Message content:  " + DOMUtils.domToString(odeRequest.getMessage()));
        }
        // Invoking ODE
        odeMex.invoke(odeRequest);
      } else {
        success = false;
      }
*/
  }

  public BpelManagementFacade getBpelManagementFacade() {
    return _server.getBpelManagementFacade();
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
    _jotm.stop();
  }

  protected TransactionManager createTransactionManager() throws Exception {
    _jotm = new Jotm(true, false);
    _txManager = _jotm.getTransactionManager();
    _txManager.setTransactionTimeout(30);
    Reference txm = new Reference("javax.transaction.TransactionManager",
                                  JotmTransactionManagerFactory.class.getName(), null);
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                       InMemoryContextFactory.class.getName());
    System.setProperty(Context.PROVIDER_URL, "pxe");
    InitialContext ctx = new InitialContext();
    ctx.rebind("TransactionManager", txm);
    ctx.close();
    return _txManager;
  }

  protected DataSource createDataSource() throws Exception {
    if (_txManager == null)
      throw new RuntimeException("No transaction manager");
    String url = "jdbc:derby:target/test-classes/derby-db/data";
    _minervaPool = new MinervaPool();
    _minervaPool.setTransactionManager(_txManager);
    _minervaPool.getConnectionFactory().setConnectionURL(url);
    _minervaPool.getConnectionFactory().setUserName("sa");
    _minervaPool.getConnectionFactory().setDriver(org.apache.derby.jdbc.EmbeddedDriver.class.getName());
    _minervaPool.getPoolParams().minSize = 0;
    _minervaPool.getPoolParams().maxSize = 10;
    _minervaPool.setType(MinervaPool.PoolType.MANAGED);
    _minervaPool.start();
    _dataSource = _minervaPool.createDataSource();
    return _dataSource;
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
    Properties properties = new Properties();
    properties.put(Environment.CONNECTION_PROVIDER,
                   DataSourceConnectionProvider.class.getName());
    properties.put(Environment.TRANSACTION_MANAGER_STRATEGY,
                   HibernateTransactionManagerLookup.class.getName());
    properties.put(Environment.SESSION_FACTORY_NAME, "jta");
    properties.put(Environment.DIALECT, "org.hibernate.dialect.DerbyDialect");
    SessionManager sm = new SessionManager(properties, _dataSource, _txManager);
    _daoCF = new BpelDAOConnectionFactoryImpl(sm);
    Reference bpelSscfRef = new Reference(BpelDAOConnectionFactory.class.getName(),
                                          HibernateDaoObjectFactory.class.getName(), null);
    InitialContext ctx = new InitialContext();
    ctx.rebind("bpelSSCF", bpelSscfRef);
    ctx.close();
    return _daoCF;
  }

  protected EndpointReferenceContext createEndpointReferenceContext() {
    _eprContext = new EndpointReferenceContext() {
      public EndpointReference resolveEndpointReference(Element element) { 
        String service = DOMUtils.getChildCharacterData(element);
        return (EndpointReference)_endpoints.get(service);
      }
      public EndpointReference convertEndpoint(QName qName, Element element) { return null; }
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
      public EndpointReference activateMyRoleEndpoint(QName processId, DeploymentUnit deploymentUnit, Endpoint myRoleEndpoint, PortType portType) {
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

      public PartnerRoleChannel createPartnerRoleChannel(QName processId, DeploymentUnit deploymentUnit, PortType portType, 
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
          public void close() { };
        }; 
      }
    };
    return _bindContext;
  }

  /**
   * An {@link javax.naming.spi.ObjectFactory} implementation that can be used to bind the
   * JOTM {@link javax.transaction.TransactionManager} implementation in JNDI.
   */
  private class JotmTransactionManagerFactory implements ObjectFactory {
    public Object getObjectInstance(Object objref, Name name, Context ctx, Hashtable env) throws Exception {
      Reference ref = (Reference) objref;
      if (ref.getClassName().equals(TransactionManager.class.getName())) {
        return _jotm.getTransactionManager();
      }
      throw new RuntimeException("The reference class name \"" + ref.getClassName() + "\" is unknown.");
    }
  }

  /**
   * JNDI {@link ObjectFactory} implementation for Hibernate-based
   * connection factory objects.
   */
  private class HibernateDaoObjectFactory implements ObjectFactory {
    public Object getObjectInstance(Object objref, Name name, Context ctx, Hashtable env) throws Exception {
      Reference ref = (Reference) objref;
      if (ref.getClassName().equals(BpelDAOConnectionFactory.class.getName())) {
        return _daoCF;
      }
      throw new RuntimeException("The reference class name \"" + ref.getClassName() + "\" is unknown.");
    }
  }

  private class SchedulerWrapper implements Scheduler {

    QuartzSchedulerImpl _quartz;
    long                _nextSchedule;

    SchedulerWrapper(BpelServer server, TransactionManager txManager, DataSource dataSource) {
      ExecutorService executorService = new ExecutorServiceWrapper();
      _quartz = new QuartzSchedulerImpl();
      _quartz.setBpelServer(server);
      _quartz.setExecutorService(executorService, 20);
      _quartz.setTransactionManager(txManager);
      _quartz.setDataSource(dataSource);
      _quartz.init();
    }

    public String schedulePersistedJob(Map<String,Object>jobDetail,Date when) throws ContextException {
      String jobId = _quartz.schedulePersistedJob(jobDetail, when);
      _nextSchedule = when == null ?  System.currentTimeMillis() : when.getTime();
      return jobId;
    }
  
    public String scheduleVolatileJob(boolean transacted, Map<String,Object> jobDetail, Date when) throws ContextException {
      String jobId = _quartz.scheduleVolatileJob(transacted, jobDetail, when);
      _nextSchedule = when == null ?  System.currentTimeMillis() : when.getTime();
      return jobId;
    }
  
    public void cancelJob(String jobId) throws ContextException {
      _quartz.cancelJob(jobId);
    }
  
    public <T> T execTransaction(Callable<T> transaction) throws Exception, ContextException {
      return _quartz.execTransaction(transaction);
    }
    public void start() { _quartz.start(); }
    public void stop() { _quartz.stop(); }
    public void shutdown() { _quartz.shutdown(); }
  }

  private class ExecutorServiceWrapper extends AbstractExecutorService {
    private ExecutorService _service = Executors.newCachedThreadPool();
    public void execute(Runnable command) {
      _service.execute(command);
    }
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return _service.awaitTermination(timeout, unit);
    }
    public void shutdown() { _service.shutdown(); }
    public List<Runnable> shutdownNow() { return _service.shutdownNow(); }
    public boolean isShutdown() { return _service.isShutdown(); }
    public boolean isTerminated() { return _service.isTerminated(); }
  }

}
