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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bom.wsdl.Definition4BPEL;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dd.*;
import org.apache.ode.bpel.deploy.DeploymentServiceImpl;
import org.apache.ode.bpel.deploy.DeploymentUnitImpl;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.BpelEngine;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.DeploymentService;
import org.apache.ode.bpel.iapi.DeploymentUnit;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.o.OExpressionLanguage;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.apache.ode.bpel.runtime.ExpressionLanguageRuntimeRegistry;
import org.apache.ode.utils.msg.MessageBundle;

/**
 * The BPEL server implementation.
 */
public class BpelServerImpl implements BpelServer {

    private static final Log __log = LogFactory.getLog(BpelServer.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private ReadWriteLock _mngmtLock = new ReentrantReadWriteLock();

    private Contexts _contexts = new Contexts();

    BpelEngineImpl _engine;

    private boolean _started;

    private boolean _initialized;

    private BpelDatabase _db;

    /** Should processes marked "active" in the DB be activated on server start? */
    private boolean _autoActivate = false;

    private List<BpelEventListener> _listeners = new CopyOnWriteArrayList<BpelEventListener>();

    private Map<QName, DeploymentUnitImpl> _deploymentUnits = new HashMap<QName,DeploymentUnitImpl>();

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
                        __log.error(msg, ex);
                    }
            }

            //readState();
            _contexts.scheduler.start();
            _started = true;
            __log.info(__msgs.msgServerStarted());
        } finally {
            _mngmtLock.writeLock().unlock();
        }

    }

    public boolean undeploy(File file) {
        DeploymentUnit du = null;
        for (DeploymentUnitImpl deploymentUnit : new HashSet<DeploymentUnitImpl>(_deploymentUnits.values())) {
            if (deploymentUnit.getDeployDir().getName().equals(file.getName())) du = deploymentUnit;
        }
        if (du == null) {
            __log.warn("Couldn't deploy " + file.getName() + ", package was not found.");
            return false;
        }

        boolean success = true;
        for (QName pName : du.getProcessNames()) {
            success = success && undeploy(pName);
        }
        rm(du.getDeployDir());
        for (QName pname : du.getProcessNames()) {
            _deploymentUnits.remove(pname);
        }
        return success;
    }

    private void rm(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles())
                rm(child);
            f.delete();
        } else {
            f.delete();
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
     * @return list of process qnames
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

            //writeState();

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
     * Load the parsed and compiled BPEL process definition from the database.
     *
     * @param processId
     *            process identifier
     * @return process information from configuration database
     */
    private OProcess loadProcess(final QName processId) {
        if (__log.isTraceEnabled()) {
            __log.trace("loadProcess: " + processId);
        }

        assert _initialized : "loadProcess() called before init()!";

        byte[] bits;

        try {
            bits = _db.exec(new BpelDatabase.Callable<byte[]>() {
                public byte[] run(BpelDAOConnection daoc) throws Exception {
                    ProcessDAO procdao = daoc.getProcess(processId);
                    return procdao.getCompiledProcess();
                }
            });
        } catch (Exception e) {
            throw new BpelEngineException("", e);
        }
        InputStream is = new ByteArrayInputStream(bits);
        OProcess compiledProcess;
        try {
            Serializer ofh = new Serializer(is);
            compiledProcess = ofh.readOProcess();
        } catch (Exception e) {
            String errmsg = __msgs.msgProcessLoadError(processId);
            __log.error(errmsg, e);
            throw new BpelEngineException(errmsg, e);
        }

        return compiledProcess;
    }

    public BpelManagementFacade getBpelManagementFacade() {
        return new BpelManagementFacadeImpl(_db, _engine, this);
    }

    public DeploymentService getDeploymentService() {
        return new DeploymentServiceImpl(this);
    }

    public void setMessageExchangeContext(MessageExchangeContext mexContext) throws BpelEngineException {
        _contexts.mexContext = mexContext;
    }

    public void setScheduler(Scheduler scheduler) throws BpelEngineException {
        _contexts.scheduler = scheduler;
    }

    public void setEndpointReferenceContext(EndpointReferenceContext eprContext) throws BpelEngineException {
        _contexts.eprContext = eprContext;
    }

    /**
     * Set the DAO connection factory. The DAO is used by the BPEL engine
     * to persist information about active processes.
     * @param daoCF {@link BpelDAOConnectionFactory} implementation.
     */
    public void setDaoConnectionFactory(BpelDAOConnectionFactory daoCF) throws BpelEngineException {
        _contexts.dao = daoCF;
    }

    public void setBindingContext(BindingContext bc) {
        _contexts.bindingContext = bc;
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
                __log.debug("skipping doActivateProcess(" + pid + ") -- process is already active");
                return;
            }

            if (__log.isDebugEnabled())
                __log.debug("Process " + pid + " is not active, creating new entry.");

            // Figure out where on the local file system we can find the
            // deployment directory for this process
            DeploymentUnitImpl du  = _deploymentUnits.get(pid);
            if (du == null) {
                // Indicates process not deployed.
                String errmsg = "Process " + pid + " is not deployed, it cannot be activated";
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }

            // Load the compiled process from the database, note we do not want
            // to recompile / or read from the file system as
            // the user could have changed it, thereby corrupting all the
            // previously instantiated processes
            OProcess compiledProcess = loadProcess(pid);

            TDeployment.Process deployInfo = du.getProcessDeployInfo(pid);
            if (deployInfo == null) {
                String errmsg = " <process> element not found in deployment descriptor for " + pid;
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }


            // Create an expression language registry for this process
            ExpressionLanguageRuntimeRegistry elangRegistry = new ExpressionLanguageRuntimeRegistry();
            for (OExpressionLanguage elang : compiledProcess.expressionLanguages) {
                try {
                    elangRegistry.registerRuntime(elang);
                } catch (ConfigurationException e) {
                    String msg = "Expression language registration error.";
                    __log.error(msg, e);
                    throw new BpelEngineException(msg, e);
                }
            }

            // Create local message-exchange interceptors.
            List<MessageExchangeInterceptor> localMexInterceptors = new LinkedList<MessageExchangeInterceptor>();
            if (deployInfo.getMexInterceptors() != null)
                for (TMexInterceptor mexi : deployInfo.getMexInterceptors().getMexInterceptorList()) {
                    try {
                        Class cls = Class.forName(mexi.getClassName());
                        localMexInterceptors.add((MessageExchangeInterceptor) cls.newInstance());
                    } catch (Throwable t) {
                       String errmsg = "Error instantiating message-exchange interceptor " + mexi.getClassName();
                       __log.error(errmsg,t);
                    }
                }

            // Create myRole endpoint name mapping (from deployment descriptor)
            HashMap<OPartnerLink, Endpoint> myRoleEndpoints = new HashMap<OPartnerLink, Endpoint>();
            for (TProvide provide : deployInfo.getProvideList()) {
                String plinkName = provide.getPartnerLink();
                TService service = provide.getService();
                if (service == null) {
                    // TODO: proper error message.
                    String errmsg = "Error in <provide> element for process " + pid + "; partnerlink " + plinkName
                            + "did not identify an endpoint";
                    __log.error(errmsg);
                    throw new BpelEngineException(errmsg);
                }

                __log.debug("Processing <provide> element for process " + pid + ": partnerlink " + plinkName + " --> "
                        + service.getName() + " : " + service.getPort());

                OPartnerLink plink = compiledProcess.getPartnerLink(plinkName);
                if (plink == null) {
                    String errmsg = "Error in deployment descriptor for process " + pid
                            + "; reference to unknown partner link " + plinkName;
                    __log.error(errmsg);
                    throw new BpelEngineException(errmsg);
                }
                myRoleEndpoints.put(plink,new Endpoint(service.getName(), service.getPort()));
            }

            // Create partnerRole initial value mapping
            HashMap<OPartnerLink, Endpoint> partnerRoleIntialValues = new HashMap<OPartnerLink, Endpoint>();
            for (TInvoke invoke : deployInfo.getInvokeList()) {
                String plinkName = invoke.getPartnerLink();

                OPartnerLink plink = compiledProcess.getPartnerLink(plinkName);
                if (plink == null) {
                    String errmsg = "Error in deployment descriptor for process " + pid
                            + "; reference to unknown partner link " + plinkName;
                    __log.error(errmsg);
                    throw new BpelEngineException(errmsg);
                }


                TService service = invoke.getService();
                // NOTE: service can be null for partner links
                if (service == null)
                    continue;

                __log.debug("Processing <invoke> element for process " + pid + ": partnerlink " + plinkName + " --> "
                        + service);

                partnerRoleIntialValues.put(plink, new Endpoint(service.getName(), service.getPort()));
            }

            BpelProcess process = new BpelProcess(pid,
                    du,
                    compiledProcess, myRoleEndpoints,
                    partnerRoleIntialValues, null, elangRegistry,
                    localMexInterceptors);

            _engine.registerProcess(process);

            __log.info(__msgs.msgProcessActivated(pid));
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    public DeploymentUnit getDeploymentUnit(QName pid) {
        return _deploymentUnits.get(pid);
    }

    public Collection<DeploymentUnit> getDeploymentUnits() {
        ArrayList<DeploymentUnit> dus = new ArrayList<DeploymentUnit>(_deploymentUnits.size());
        for (DeploymentUnitImpl du : _deploymentUnits.values()) {
            dus.add(du);
        }
        return dus;
    }

    private void dbSetProcessActive(final QName pid, final boolean val) {
        if (__log.isTraceEnabled())
            __log.trace("dbSetProcessActive:" + pid + " = " + val);

        try {
            if (_db.exec(new BpelDatabase.Callable<ProcessDAO>() {
                public ProcessDAO run(BpelDAOConnection conn) throws Exception {
                    // Hack, but at least for now we need to ensure that we are
                    // the only
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
     * Deploys a process.
     */
    public Collection<QName> deploy(File deploymentUnitDirectory) {

        __log.info(__msgs.msgDeployStarting(deploymentUnitDirectory));

        DeploymentUnitImpl du = new DeploymentUnitImpl(deploymentUnitDirectory);

        ArrayList<QName> deployed = new ArrayList<QName> ();
        BpelEngineException failed = null;
        // Going trough each process declared in the dd
        for (TDeployment.Process processDD : du.getDeploymentDescriptor().getDeploy().getProcessList()) {

            // If a type is not specified, assume the process id is also the type.
            QName type = processDD.getType() != null ? processDD.getType() : processDD.getName();
            OProcess oprocess = du.getProcesses().get(type);
            if (oprocess == null)
                throw new BpelEngineException("Could not find the compiled process definition for BPEL" +
                        "type " +  type + " when deploying process " + processDD.getName()
                        + " in " +  deploymentUnitDirectory);
            try {

                deploy(processDD.getName(), du, oprocess, du.getDocRegistry()
                        .getDefinitions(), processDD);

                deployed.add(processDD.getName());
            } catch (Throwable e) {
                String errmsg = __msgs.msgDeployFailed(processDD.getName(), deploymentUnitDirectory);
                __log.error(errmsg, e);
                failed = new BpelEngineException(errmsg,e);
                break;
            }
        }

        // Roll back succesfull deployments if we failed.
        if (failed != null) {
            if (!deployed.isEmpty()) {
                __log.error(__msgs.msgDeployRollback(deploymentUnitDirectory));
                for (QName pid : deployed) {
                    try {
                        undeploy(pid);
                    } catch (Throwable t) {
                        __log.fatal("Unexpect error undeploying process " + pid, t);
                    }
                }
            }
            throw failed;
        } else
            return du.getProcesses().keySet();
    }

    private void deploy(final QName processId, final DeploymentUnitImpl du, final OProcess oprocess,
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

        final ProcessDDInitializer pi = new ProcessDDInitializer(oprocess, processDD);

        try {

            _db.exec(new BpelDatabase.Callable<ProcessDAO>() {
                public ProcessDAO run(BpelDAOConnection conn) throws Exception {
                    // Hack, but at least for now we need to ensure that we are
                    // the only process with this process id.
                    ProcessDAO old = conn.getProcess(processId);
                    if (old != null) {
                        String errmsg = __msgs.msgProcessDeployErrAlreadyDeployed(processId);
                        __log.error(errmsg);
                        throw new BpelEngineException(errmsg);
                    }

                    ProcessDAO newDao = conn.createProcess(processId, oprocess.getQName());
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

        _deploymentUnits.put(processDD.getName(), du);

        doActivateProcess(processId);
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
     *
     * @return
     */
    public boolean isAutoActivate() {
        return _autoActivate;
    }

    /**
     * Set the flag the determines whether processes marked as active are
     * automatically activated at startup.
     *
     * @param autoActivate
     */
    public void setAutoActivate(boolean autoActivate) {
        _autoActivate = autoActivate;
    }


    /**
     * Register a global message exchange interceptor. 
     * @param interceptor message-exchange interceptor
     */
    public void registerMessageExchangeInterceptor(MessageExchangeInterceptor interceptor) {
        // We don't really care all that much about concurrency here, we just want to 
        // avoid ConcurrentModificationEx, so use an array instead of a collection. 
        synchronized(_contexts) {
            MessageExchangeInterceptor[] r = new MessageExchangeInterceptor[1+_contexts.globalIntereceptors.length];
            System.arraycopy(_contexts.globalIntereceptors,0,r,0,_contexts.globalIntereceptors.length);
            r[r.length-1] = interceptor;
            _contexts.globalIntereceptors = r;
        }
    }


}
