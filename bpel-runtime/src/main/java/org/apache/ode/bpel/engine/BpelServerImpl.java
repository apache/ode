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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bom.wsdl.Definition4BPEL;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dd.DeploymentDescriptorDocument;
import org.apache.ode.bpel.dd.TDeploymentDescriptor;
import org.apache.ode.bpel.dd2.TDeployment;
import org.apache.ode.bpel.dd2.TInvoke;
import org.apache.ode.bpel.dd2.TProvide;
import org.apache.ode.bpel.deploy.DeploymentUnitImpl;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.iapi.BpelEngine;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.DeploymentUnit;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.o.OExpressionLanguage;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.apache.ode.bpel.runtime.ExpressionLanguageRuntimeRegistry;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The BPEL server implementation. 
 */
public class BpelServerImpl implements BpelServer {

  private static final Log __log = LogFactory.getLog(BpelServer.class);
  private static final Messages __msgs = MessageBundle
      .getMessages(Messages.class);

  private ReadWriteLock _mngmtLock = new ReentrantReadWriteLock();
  private Contexts _contexts = new Contexts();
  BpelEngineImpl _engine;
  private boolean _started;
  private boolean _initialized;
  private BpelDatabase _db;
  private File _deploymentDir;
  /** Should processes marked "active" in the DB be activated on server start? */
  private boolean _autoActivate = false;
  private List<BpelEventListener> _listeners =
    new CopyOnWriteArrayList<BpelEventListener>();
  private List<DeploymentUnitImpl> _deployedUnits = new ArrayList<DeploymentUnitImpl>();

  public BpelServerImpl() {

  }

  public void start() {
    _mngmtLock.writeLock().lock();
    try {
      if (!_initialized) {
        String err = "start() called before init()!";
        __log.fatal(err);
        throw new IllegalStateException(err);
      }

      if (_started) {
        if (__log.isDebugEnabled())
          __log.debug("start() ignored -- already started");
        return;
      }

      if (__log.isDebugEnabled()) {
        __log.debug("BPEL SERVER starting.");
      }

      _engine = new BpelEngineImpl(_contexts);
      if (_autoActivate) {
        List<QName> pids = findActive();
        for (QName pid : pids) 
          try {
            doActivateProcess(pid);
          } catch (Exception ex) {
            String msg = __msgs.msgProcessActivationError(pid);
            __log.error(msg,ex);
          }
      }

      readState();
      _contexts.scheduler.start();
      _started = true;
      __log.info(__msgs.msgServerStarted());
    } finally {
      _mngmtLock.writeLock().unlock();
    }

  }

  
  public void registerBpelEventListener(BpelEventListener listener) {
    _listeners.add(listener);
  }
  
  public void unregisterBpelEventListener(BpelEventListener listener) {
    _listeners.remove(listener);
  }
  
  void fireEvent(BpelEvent event) {
    for (BpelEventListener l : _listeners) {
      l.onEvent(event);
    }
  }
  
  /**
   * Find the active processes in the database.
   * @return
   */
  private List<QName> findActive() {

    try {
      return _db.exec(new BpelDatabase.Callable<List<QName>>() {
        public List<QName> run(BpelDAOConnection conn) throws Exception {
          Collection<ProcessDAO> proc = conn.processQuery(null);
          ArrayList<QName> list = new ArrayList<QName>();
          for (ProcessDAO p : proc)
            if (p.isActive())
              list.add(p.getProcessId());
          return list;
        }
      });
    } catch (Exception ex) {
      String msg = __msgs.msgDbError();
      __log.error(msg, ex);
      throw new BpelEngineException(msg, ex);
    }
  }

  public void stop() {
    _mngmtLock.writeLock().lock();
    try {
      if (!_started) {
        if (__log.isDebugEnabled())
          __log.debug("stop() ignored -- already stopped");
        return;
      }
      if (__log.isDebugEnabled()) {
        __log.debug("BPEL SERVER STOPPING");
      }

      writeState();

      _contexts.scheduler.stop();
      _engine = null;
      _started = false;

      __log.info(__msgs.msgServerStopped());
    } finally {
      _mngmtLock.writeLock().unlock();
    }
  }

  public boolean undeploy(final QName process) {
    _mngmtLock.writeLock().lock();
    try {
      if (!_initialized) {
        String err = "Server must be initialized!";
        __log.error(err);
        throw new IllegalStateException(err, null);
      }

      if (_engine != null)
        _engine.unregisterProcess(process);

      // Delete it from the database.
      boolean found = _db.exec(new BpelDatabase.Callable<Boolean>() {
        public Boolean run(BpelDAOConnection conn) throws Exception {
          ProcessDAO proc = conn.getProcess(process);
          if (proc != null) {
            proc.delete();
            return true;
          }
          return false;
        }
      });

      if (found) {
        __log.info(__msgs.msgProcessUndeployed(process));
        return true;
      }
      return false;
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      __log.error(__msgs.msgProcessUndeployFailed(process), ex);
      throw new BpelEngineException(ex);
    } finally {
      _mngmtLock.writeLock().unlock();
    }

  }

  /**
   * Load the parsed and compiled BPEL process definition from the "net".
   * 
   * @param uri
   *          process identifier
   * @return compiled process representation
   */
  private OProcess loadProcess(URI uri) throws IOException {
    InputStream is = uri.toURL().openStream();

    try {
      Serializer ofh = new Serializer(is);
      return ofh.readOProcess();
    } catch (Exception e) {
      String msg = __msgs.msgBarProcessLoadErr();
      __log.error(msg, e);
      throw new BpelEngineException(msg, e);
    } finally {
      is.close();
    }
  }

  /**
   * Load the parsed and compiled BPEL process definition from the database.
   * 
   * @param processId
   *          process identifier
   * @return compiled process representation
   */
  private ProcessInfo loadProcess(final QName processId) {
    if (__log.isTraceEnabled()) {
      __log.trace("loadProcess: " + processId);
    }

    assert _initialized : "loadProcess() called before init()!";

    byte[] bits;


    final ProcessInfo ret = new ProcessInfo();
    ret.serviceNames = new HashMap<Integer,QName>();
    ret.myEprs = new HashMap<Integer,Element>();
    try {
      bits = _db.exec(new BpelDatabase.Callable<byte[]>() {
        public byte[] run(BpelDAOConnection daoc) throws Exception {
          ProcessDAO procdao = daoc.getProcess(processId);
          Collection<PartnerLinkDAO> plinks = procdao.getDeployedEndpointReferences();
          for (PartnerLinkDAO p : plinks) {
            if (p.getMyRoleName() != null) {
              ret.serviceNames.put(p.getPartnerLinkModelId(), p.getMyRoleServiceName());
              ret.myEprs.put(p.getPartnerLinkModelId(), p.getMyEPR());
            }
          }
          return procdao.getCompiledProcess();
        }
      });
    } catch (Exception e) {
      throw new BpelEngineException("", e);
    }
    InputStream is = new ByteArrayInputStream(bits);
    try {
      Serializer ofh = new Serializer(is);
      ret.compiledProcess = ofh.readOProcess();
    } catch (Exception e) {
      String errmsg = __msgs.msgProcessLoadError(processId);
      __log.error(errmsg, e);
      throw new BpelEngineException(errmsg, e);
    }
    
    
    return ret;
  }
  
 /**
   * Calculate the URI for the compiled BPEL process based on the deployment
   * descriptor URI and its content.
   * 
   * @param dd
   *          deployment descriptor
   * @param dduri
   *          deployment descriptor uri
   * @return
   */
  private URI calcCbpUri(TDeploymentDescriptor dd, URI dduri) {
    URI cbpLocation;
    if (dd.getCompiledProcessLocation() != null)
      cbpLocation = dduri.resolve(dd.getCompiledProcessLocation());
    else {
      String path = dduri.getPath();
      String newpath = path.replaceFirst("\\.[^\\.]*$", ".cbp");
      if (newpath.equals(path))
        newpath = newpath + ".cbp";

      cbpLocation = dduri.resolve(newpath);
    }
    return cbpLocation;
  }

  public BpelManagementFacade getBpelManagementFacade() {
    return new BpelManagementFacadeImpl(_db, _engine);
  }

  public void setMessageExchangeContext(MessageExchangeContext mexContext)
      throws BpelEngineException {
    _contexts.mexContext = mexContext;
  }

  public void setScheduler(Scheduler scheduler) throws BpelEngineException {
    _contexts.scheduler = scheduler;
  }

  public void setEndpointReferenceContext(EndpointReferenceContext eprContext)
      throws BpelEngineException {
    _contexts.eprContext = eprContext;
  }

  public void setDaoConnectionFactory(BpelDAOConnectionFactory daoCF)
      throws BpelEngineException {
    _contexts.dao = daoCF;
  }

  public void init() throws BpelEngineException {
    _mngmtLock.writeLock().lock();
    try {
      if (_initialized)
        throw new IllegalStateException("init() called twice.");

      if (__log.isDebugEnabled()) {
        __log.debug("BPEL SERVER initializing ");
      }

      _db = new BpelDatabase(_contexts.dao, _contexts.scheduler);
      _initialized = true;
    } finally {
      _mngmtLock.writeLock().unlock();
    }
  }

  public void shutdown() throws BpelEngineException {

  }

  public BpelEngine getEngine() {
    acquireWorkLockForTx();
    if (!_started) {
      __log.debug("call on getEngine() on server that has not been started!");
      throw new IllegalStateException("Server must be started!");
    }
    return _engine;
  }

  public void activate(final QName pid, boolean sticky) {
    if (__log.isTraceEnabled())
      __log.trace("activate: " + pid);

    try {
      _mngmtLock.writeLock().lockInterruptibly();
    } catch (InterruptedException ie) {
      __log.debug("activate() interrupted.", ie);
      throw new BpelEngineException(__msgs.msgOperationInterrupted());
    }
    try {
      if (sticky)
          dbSetProcessActive(pid, true);
    
      doActivateProcess(pid);
    } finally {
      _mngmtLock.writeLock().unlock();
    }
  }

  public void deactivate(QName pid, boolean sticky) throws BpelEngineException {
    if (__log.isTraceEnabled())
      __log.trace("deactivate " + pid);

    try {
      _mngmtLock.writeLock().lockInterruptibly();
    } catch (InterruptedException ie) {
      __log.debug("deactivate() interrupted.", ie);
      throw new BpelEngineException(__msgs.msgOperationInterrupted());
    }

    try {
      if (sticky)
        dbSetProcessActive(pid, false);
      
      doActivateProcess(pid);
    } finally {
      _mngmtLock.writeLock().unlock();
    }
  }

  /**
   * Activate the process in the engine.
   * 
   * @param pid
   */
  private void doActivateProcess(final QName pid) {
    _mngmtLock.writeLock().lock();
    try {
      // If the process is already active, do nothing.
      if (_engine.isProcessRegistered(pid)) {
        __log.debug("skipping activate(" + pid
            + ") -- process is already active");
        return;
      }

      if (__log.isDebugEnabled()) {
        __log.debug("service not active, creating new entry.");
      }

      ProcessInfo pinfo = loadProcess(pid);

      ExpressionLanguageRuntimeRegistry elangRegistry = new ExpressionLanguageRuntimeRegistry();
      for (OExpressionLanguage elang : pinfo.compiledProcess.expressionLanguages) {
        try {
          elangRegistry.registerRuntime(elang);
        } catch (ConfigurationException e) {
          String msg = "Expression language registration error.";
          __log.error(msg, e);
          throw new BpelEngineException(msg, e);
        }
      }

      _engine.registerProcess(pid, pinfo.compiledProcess, pinfo.serviceNames, pinfo.myEprs, elangRegistry);

      __log.info(__msgs.msgProcessActivated(pid));
    } finally {
      _mngmtLock.writeLock().unlock();
    }
  }

  private void dbSetProcessActive(final QName pid, final boolean val) {
    if (__log.isTraceEnabled())
      __log.trace("dbSetProcessActive:" + pid + " = " + val);

    try {
      if (_db.exec(new BpelDatabase.Callable<ProcessDAO>() {
        public ProcessDAO run(BpelDAOConnection conn) throws Exception {
          // Hack, but at least for now we need to ensure that we are the only
          // process with this process id.
          ProcessDAO pdao = conn.getProcess(pid);
          if (pdao == null)
            return null;
          pdao.setActive(val);
          return pdao;
        }
      }) == null) {
        String errmsg = __msgs.msgProcessNotFound(pid);
        __log.error(errmsg);
        throw new BpelEngineException(errmsg, null);
      }
    } catch (BpelEngineException bpe) {
      throw bpe;
    } catch (Exception ex) {
      String errmsg = __msgs.msgDbError();
      __log.error(errmsg);
      throw new BpelEngineException(ex);
    }
  }

  /**
   * Deploy a process.
   * 
   * @param dduri
   *          URI of the deployment descriptor
   */
  public void deploy(final QName processId, final URI dduri) throws IOException {
    if (__log.isDebugEnabled()) {
      __log.debug("deployService: " + dduri);
    }

    // First, make sure we are undeployed.
    undeploy(processId);

    final TDeploymentDescriptor dd;
    try {
      dd = readDeploymentDescriptor(dduri);
    } catch (MalformedURLException e) {
      String errmsg = __msgs.msgDeployFailDescriptorURIInvalid(dduri);
      __log.error(errmsg, e);
      throw new BpelEngineException(errmsg, e);
    } catch (XmlException e) {
      String errmsg = __msgs.msgDeployFailDescriptorInvalid(dduri);
      __log.error(errmsg, e);
      throw new BpelEngineException(errmsg, e);
    } catch (IOException e) {
      String errmsg = __msgs.msgDeployFailDescriptorIOError(dduri);
      __log.error(errmsg, e);
      throw new BpelEngineException(errmsg, e);
    }

    final OProcess compiledBpelProcess = loadProcess(calcCbpUri(dd, dduri));
    Serializer serializer = new Serializer(compiledBpelProcess.compileDate
        .getTime(), 1);
    final byte[] bits;
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      serializer.write(bos);
      serializer.writeOProcess(compiledBpelProcess, bos);
      bos.close();
      bits = bos.toByteArray();
    } catch (Exception ex) {
      String errmsg = "Error re-serializing CBP";
      __log.fatal(errmsg, ex);
      throw new BpelEngineException(errmsg, ex);
    }

    final ProcessInitializer pi = new ProcessInitializer(compiledBpelProcess,
        dd);
    
    try {

      _db.exec(new BpelDatabase.Callable<ProcessDAO>() {
        public ProcessDAO run(BpelDAOConnection conn) throws Exception {
          // Hack, but at least for now we need to ensure that we are the only
          // process with this process id.
          ProcessDAO old = conn.getProcess(processId);
          if (old != null) {
            String errmsg = __msgs
                .msgProcessDeployErrAlreadyDeployed(processId);
            __log.error(errmsg);
            throw new BpelEngineException(errmsg);
          }
          
          ProcessDAO newDao = conn.createProcess(processId,compiledBpelProcess.getQName());
          newDao.setDeployURI(dduri);
          newDao.setCompiledProcess(bits);
          pi.init(newDao);
          pi.update(newDao);
          return newDao;

        }
      });
      __log.info(__msgs.msgProcessDeployed(processId));
    } catch (BpelEngineException ex) {
      throw ex;
    } catch (Exception dce) {
      __log.error("", dce);
      throw new BpelEngineException("", dce);
    }

    if (dd.isSetActive())
      doActivateProcess(processId);

  }

  /**
   * Deploys a process directly from its compiled representation.
   */
  public DeploymentUnit deploy(File deploymentUnitDirectory) {
    if (__log.isDebugEnabled()) {
      __log.debug("deployService from oprocess");
    }
    return deploy(deploymentUnitDirectory, false);
  }

  private DeploymentUnit deploy(File deploymentUnitDirectory, boolean activateOnly) {
    DeploymentUnitImpl du = new DeploymentUnitImpl(deploymentUnitDirectory);
    // Going trough each process declared in the dd
    for (TDeployment.Process processDD : du.getDeployDocument().getDeploy().getProcessList()) {
      OProcess oprocess = du.getProcesses().get(processDD.getName());
      if (oprocess == null) throw new BpelEngineException("Could not find the compiled process definition for a " +
              "process referenced in the deployment descriptor: " + processDD.getName());
      try {

        if (activateOnly) {
          activate(processDD.getName(), false);
        } else
          deploy(processDD.getName(),
                deploymentUnitDirectory.toURI(), oprocess, du.getDocRegistry().getDefinitions(), processDD);

        // But we still need to declare our services internally
        for (TProvide provide : processDD.getProvideList()) {
          Definition4BPEL def = du.getDocRegistry().getDefinition(
                  provide.getService().getName().getNamespaceURI());
          _contexts.eprContext.activateEndpoint(provide.getService().getName(),
                  provide.getService().getPort(), def);
        }
        for (TInvoke invoke : processDD.getInvokeList()) {
          Definition4BPEL def = du.getDocRegistry().getDefinition(
                  invoke.getService().getName().getNamespaceURI());
          _contexts.eprContext.activateExternalEndpoint(invoke.getService().getName(),
                  invoke.getService().getPort(), def);
        }
      } catch (Throwable e) {
        __log.error("Service deployment failed!", e);
      }
    }
    _deployedUnits.add(du);
    return du;
  }

  private void deploy(final QName processId, final URI deployedURI, final OProcess oprocess,
                      final Definition4BPEL[] defs, TDeployment.Process processDD) {
  // First, make sure we are undeployed.
    undeploy(processId);

    Serializer serializer = new Serializer(oprocess.compileDate.getTime(), 1);
    final byte[] bits;
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      serializer.write(bos);
      serializer.writeOProcess(oprocess, bos);
      bos.close();
      bits = bos.toByteArray();
    } catch (Exception ex) {
      String errmsg = "Error re-serializing CBP";
      __log.fatal(errmsg, ex);
      throw new BpelEngineException(errmsg, ex);
    }

    final ProcessDDInitializer pi = new ProcessDDInitializer(oprocess, defs, processDD, _contexts.eprContext);

    try {

      _db.exec(new BpelDatabase.Callable<ProcessDAO>() {
        public ProcessDAO run(BpelDAOConnection conn) throws Exception {
          // Hack, but at least for now we need to ensure that we are the only
          // process with this process id.
          ProcessDAO old = conn.getProcess(processId);
          if (old != null) {
            String errmsg = __msgs
                .msgProcessDeployErrAlreadyDeployed(processId);
            __log.error(errmsg);
            throw new BpelEngineException(errmsg);
          }
          
          ProcessDAO newDao = conn.createProcess(processId,oprocess.getQName());
          newDao.setDeployURI(deployedURI);
          newDao.setCompiledProcess(bits);
          pi.init(newDao);
          pi.update(newDao);
          return newDao;

        }
      });
      __log.info(__msgs.msgProcessDeployed(processId));
    } catch (BpelEngineException ex) {
      throw ex;
    } catch (Exception dce) {
      __log.error("", dce);
      throw new BpelEngineException("", dce);
    }

    doActivateProcess(processId);
  }

  private org.apache.ode.bpel.dd.TDeploymentDescriptor readDeploymentDescriptor(
      URI url) throws MalformedURLException, XmlException, IOException {
    if (url == null)
      throw new NullPointerException("null url!");

    DeploymentDescriptorDocument ddDoc = DeploymentDescriptorDocument.Factory
        .parse(url.toURL());
    return ddDoc.getDeploymentDescriptor();
  }

  /**
   * Acquire a work lock for the current transaction, releasing the lock once
   * the transaction completes.
   */
  private void acquireWorkLockForTx() {
    // TODO We need to have support for a TransactionContext in order to 
    // do this.
    
  }

  /**
   * Get the flag that determines whether processes marked as active are
   * automatically activated at startup.
   * @return
   */
  public boolean isAutoActivate() {
    return _autoActivate;
  }

  /**
   * Set the flag the determines whether processes marked as active are
   * automatically activated at startup.
   * @param autoActivate
   */
  public void setAutoActivate(boolean autoActivate) {
    _autoActivate = autoActivate;
  }

  public void setDeploymentDir(File deploymentDir) {
    _deploymentDir = deploymentDir;
  }

  public void readState() {
    File duState = new File(_deploymentDir, ".state");
    if (duState.exists()) {
      try {
        BufferedReader duStateReader = new BufferedReader(new FileReader(duState));
        String line;
        while ((line = duStateReader.readLine()) != null) {
          String filename = line.substring(0, line.indexOf("|"));
          long timestamp = Long.valueOf(line.substring(line.indexOf("|") + 1 , line.length()));
          File duFile = new File(_deploymentDir, filename);
          if (duFile.exists()) {
            if (new File(duFile, "deploy.xml").lastModified() > timestamp) {
              deploy(duFile);
            } else {
              DeploymentUnitImpl du = (DeploymentUnitImpl) deploy(duFile, true);
              du.setLastModified(timestamp);
            }
          }
        }
      } catch (FileNotFoundException e) {
        // Shouldn't happen
      } catch (Exception e) {
        __log.error("An error occured while reading past deployments states, some " +
                "processes will be redeployed.", e);
      }
    } else {
      __log.info("Couldn't find any deployment history, all processes will " +
              "be redeployed.");
    }
  }

  public void writeState() {
    try {
      __log.debug("Writing current deployment state.");
      FileWriter duStateWriter = new FileWriter(new File(_deploymentDir, ".state"), false);
      for (DeploymentUnitImpl deploymentUnit : _deployedUnits) {
        // Somebody using pipe in their directory names don't deserve to deploy anything
        duStateWriter.write(deploymentUnit.getDuDirectory().getName());
        duStateWriter.write("|");
        duStateWriter.write(""+deploymentUnit.getLastModified());
        duStateWriter.write("\n");
      }
      duStateWriter.flush();
      duStateWriter.close();
    } catch (IOException e) {
      __log.error("Couldn't write deployment state! Processes could be redeployed (or not) " +
              "even they don't (or do) need to.", e);
    }
  }

  public List<DeploymentUnitImpl> getDeployedUnits() {
    return _deployedUnits;
  }

  class ProcessInfo {
    OProcess compiledProcess;
    Map<Integer, QName> serviceNames;
    Map<Integer, Element> myEprs;
  }
}
