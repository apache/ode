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
package org.apache.ode.store;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.extension.ExtensionValidator;
import org.apache.ode.bpel.dd.DeployDocument;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.store.DeploymentUnitDir.CBPInfo;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.il.config.OdeConfigProperties;
import org.hsqldb.jdbc.jdbcDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>
 * JDBC-based implementation of a process store. Also provides an "in-memory" store by way of HSQL database.
 * </p>
 *
 * <p>
 * The philsophy here is to keep things simple. Process store operations are relatively infrequent. Performance of the public
 * methods is not a concern. However, note that the {@link org.apache.ode.bpel.iapi.ProcessConf} objects returned by the class are
 * going to be used from within the engine runtime, and hence their performance needs to be very good. Similarly, these objects
 * should be immutable so as not to confuse the engine.
 *
 * Note the way that the database is used in this class, it is more akin to a recovery log, this is intentional: we want to start
 * up, load stuff from the database and then pretty much forget about it when it comes to reads.
 *
 * @author Maciej Szefler <mszefler at gmail dot com>
 * @author mriou <mriou at apache dot org>
 */
public class ProcessStoreImpl implements ProcessStore {

    private static final Log __log = LogFactory.getLog(ProcessStoreImpl.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private final CopyOnWriteArrayList<ProcessStoreListener> _listeners = new CopyOnWriteArrayList<ProcessStoreListener>();

    private Map<QName, ProcessConfImpl> _processes = new HashMap<QName, ProcessConfImpl>();

    private Map<String, DeploymentUnitDir> _deploymentUnits = new HashMap<String, DeploymentUnitDir>();
    
    private Map<QName, ExtensionValidator> _extensionValidators = new HashMap<QName, ExtensionValidator>();

    /** Guards access to the _processes and _deploymentUnits */
    private final ReadWriteLock _rw = new ReentrantReadWriteLock();

    /** GUID used to create a unique in-memory db. */
    private String _guid = new GUID().toString();

    private ConfStoreConnectionFactory _cf;

    private EndpointReferenceContext eprContext;

    protected File _deployDir;

    protected File _configDir;

    /**
     * Executor used to process DB transactions. Allows us to isolate the TX context, and to ensure that only one TX gets executed a
     * time. We don't really care to parallelize these operations because: i) HSQL does not isolate transactions and we don't want
     * to get confused ii) we're already serializing all the operations with a read/write lock. iii) we don't care about
     * performance, these are infrequent operations.
     */
    private ExecutorService _executor = Executors.newSingleThreadExecutor(new SimpleThreadFactory());

    /**
     * In-memory DataSource, or <code>null</code> if we are using a real DS. We need this to shutdown the DB.
     */
    private DataSource _inMemDs;

    public ProcessStoreImpl() {
        this(null, null, "", new OdeConfigProperties(new Properties(), ""), true);
    }

    public ProcessStoreImpl(EndpointReferenceContext eprContext, DataSource ds, String persistenceType, OdeConfigProperties props, boolean createDatamodel) {
        this.eprContext = eprContext;
        if (ds != null) {
            // ugly hack
            if (persistenceType.toLowerCase().indexOf("hib") != -1)
                _cf = new org.apache.ode.store.hib.DbConfStoreConnectionFactory(ds, props.getProperties(), createDatamodel);
            else
                _cf = new org.apache.ode.store.jpa.DbConfStoreConnectionFactory(ds, createDatamodel);
        } else {
            // If the datasource is not provided, then we create a HSQL-based in-memory
            // database. Makes testing a bit simpler.
            DataSource hsqlds = createInternalDS(_guid);
            if ("hibernate".equalsIgnoreCase(persistenceType))
                _cf = new org.apache.ode.store.hib.DbConfStoreConnectionFactory(hsqlds, props.getProperties(), createDatamodel);
            else
                _cf = new org.apache.ode.store.jpa.DbConfStoreConnectionFactory(hsqlds, createDatamodel);
            _inMemDs = hsqlds;
        }

    }

    /**
     * Constructor that hardwires OpenJPA on a new in-memory database. Suitable for tests.
     */
    public ProcessStoreImpl(EndpointReferenceContext eprContext, DataSource inMemDs) {
        this.eprContext = eprContext;
        DataSource hsqlds = createInternalDS(new GUID().toString());
        //when in memory we always create the model as we are starting from scratch
        _cf = new org.apache.ode.store.jpa.DbConfStoreConnectionFactory(hsqlds, true);
        _inMemDs = hsqlds;
    }

    public void shutdown() {
        if (_inMemDs != null) {
            shutdownInternalDB(_inMemDs);
            _inMemDs = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        // force a shutdown so that HSQL cleans up its mess.
        try {
            shutdown();
        } catch (Throwable t) {
            ; // we tried, no worries.
        }
        super.finalize();
    }

    /**
     * Deploys a process.
     */
    public Collection<QName> deploy(final File deploymentUnitDirectory) {
        __log.info(__msgs.msgDeployStarting(deploymentUnitDirectory));

        final Date deployDate = new Date();

        // Create the DU and compile/scan it before acquiring lock.
        final DeploymentUnitDir du = new DeploymentUnitDir(deploymentUnitDirectory);
        du.setExtensionValidators(_extensionValidators);
        try {
            du.compile();
        } catch (CompilationException ce) {
            String errmsg = __msgs.msgDeployFailCompileErrors(ce);
            __log.error(errmsg, ce);
            throw new ContextException(errmsg, ce);
        }

        du.scan();
        final DeployDocument dd = du.getDeploymentDescriptor();
        final ArrayList<ProcessConfImpl> processes = new ArrayList<ProcessConfImpl>();
        Collection<QName> deployed;

        _rw.writeLock().lock();
        // Process and DU use a monotonically increased single version number.
        long version = exec(new Callable<Long>() {
            public Long call(ConfStoreConnection conn) {
                return conn.getNextVersion();
            }
        });

        try {
            if (_deploymentUnits.containsKey(du.getName())) {
                String errmsg = __msgs.msgDeployFailDuplicateDU(du.getName());
                __log.error(errmsg);
                throw new ContextException(errmsg);
            }

            du.setVersion(version);

            for (TDeployment.Process processDD : dd.getDeploy().getProcessList()) {
                QName pid = toPid(processDD.getName(), version);

                // Retires older version if we can find one
                DeploymentUnitDir oldDU = findOldDU(du.getName());
                if (oldDU != null)
                    setRetiredPackage(oldDU.getName(), true);

                if (_processes.containsKey(pid)) {
                    String errmsg = __msgs.msgDeployFailDuplicatePID(processDD.getName(), du.getName());
                    __log.error(errmsg);
                    throw new ContextException(errmsg);
                }

                QName type = processDD.getType() != null ? processDD.getType() : processDD.getName();

                CBPInfo cbpInfo = du.getCBPInfo(type);
                if (cbpInfo == null) {
                    String errmsg = __msgs.msgDeployFailedProcessNotFound(processDD.getName(), du.getName());
                    __log.error(errmsg);
                    throw new ContextException(errmsg);
                }

                ProcessConfImpl pconf = new ProcessConfImpl(pid, processDD.getName(), version, du, processDD, deployDate,
                        calcInitialProperties(processDD), calcInitialState(processDD), eprContext, _configDir);
                processes.add(pconf);
            }

            _deploymentUnits.put(du.getName(), du);

            for (ProcessConfImpl process : processes) {
                __log.info(__msgs.msgProcessDeployed(du.getDeployDir(), process.getProcessId()));
                _processes.put(process.getProcessId(), process);

            }

        } finally {
            _rw.writeLock().unlock();
        }

        // Do the deployment in the DB. We need this so that we remember deployments across system shutdowns.
        // We don't fail if there is a DB error, simply print some errors.
        deployed = exec(new Callable<Collection<QName>>() {
            public Collection<QName> call(ConfStoreConnection conn) {
                // Check that this deployment unit is not deployed.
                DeploymentUnitDAO dudao = conn.getDeploymentUnit(du.getName());
                if (dudao != null) {
                    String errmsg = "Database out of synch for DU " + du.getName();
                    __log.warn(errmsg);
                    dudao.delete();
                }

                dudao = conn.createDeploymentUnit(du.getName());
                try {
                    dudao.setDeploymentUnitDir(deploymentUnitDirectory.getCanonicalPath());
                } catch (IOException e1) {
                    String errmsg = "Error getting canonical path for " + du.getName()
                            + "; deployment unit will not be available after restart!";
                    __log.error(errmsg);

                }

                ArrayList<QName> deployed = new ArrayList<QName>();
                // Going trough each process declared in the dd
                for (ProcessConfImpl pc : processes) {
                    try {
                        ProcessConfDAO newDao = dudao.createProcess(pc.getProcessId(), pc.getType(), pc.getVersion());
                        newDao.setState(pc.getState());
                        for (Map.Entry<QName, Node> prop : pc.getProcessProperties().entrySet()) {
                            newDao.setProperty(prop.getKey(), DOMUtils.domToString(prop.getValue()));
                        }
                        deployed.add(pc.getProcessId());
                        conn.setVersion(pc.getVersion());
                    } catch (Throwable e) {
                        String errmsg = "Error persisting deployment record for " + pc.getProcessId()
                                + "; process will not be available after restart!";
                        __log.error(errmsg, e);
                    }
                }
                return deployed;
            }

        });

        // We want the events to be fired outside of the bounds of the writelock.
        try {
            for (ProcessConfImpl process : processes) {
                fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.DEPLOYED, process.getProcessId(), process.getDeploymentUnit()
                        .getName()));
                fireStateChange(process.getProcessId(), process.getState(), process.getDeploymentUnit().getName());
            }
        } catch (Exception e) {
            // A problem at that point means that engine deployment failed, we don't want the store to keep the du
            __log.warn("Deployment failed within the engine, store undeploying process.");
            undeploy(deploymentUnitDirectory);
            if (e instanceof ContextException) throw (ContextException) e;
            else throw new ContextException("Deployment failed within the engine.", e);
        }

        return deployed;
    }

    public Collection<QName> undeploy(final File dir) {
        try {
            exec(new Callable<Collection<QName>>() {
                public Collection<QName> call(ConfStoreConnection conn) {
                    DeploymentUnitDAO dudao = conn.getDeploymentUnit(dir.getName());
                    if (dudao != null)
                        dudao.delete();
                    return null;
                }
            });
        } catch (Exception ex) {
            __log.error("Error synchronizing with data store; " + dir.getName() + " may be reappear after restart!");
        }

        Collection<QName> undeployed = Collections.emptyList();
        DeploymentUnitDir du;
        _rw.writeLock().lock();
        try {
            du = _deploymentUnits.remove(dir.getName());
            if (du != null) {
                undeployed = toPids(du.getProcessNames(), du.getVersion());
                _processes.keySet().removeAll(undeployed);
            }
        } finally {
            _rw.writeLock().unlock();
        }

        for (QName pn : undeployed) {
            fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.UNDEPLOYED, pn, du.getName()));
            __log.info(__msgs.msgProcessUndeployed(pn));
        }

        return undeployed;
    }

    public Collection<String> getPackages() {
        _rw.readLock().lock();
        try {
            return new ArrayList<String>(_deploymentUnits.keySet());
        } finally {
            _rw.readLock().unlock();
        }
    }

    public List<QName> listProcesses(String packageName) {
        _rw.readLock().lock();
        try {
            DeploymentUnitDir du = _deploymentUnits.get(packageName);
            if (du == null)
                return null;
            return toPids(du.getProcessNames(), du.getVersion());
        } finally {
            _rw.readLock().unlock();
        }
    }

    public void setState(final QName pid, final ProcessState state) {
        __log.debug("Changing process state for " + pid + " to " + state);

        final ProcessConfImpl pconf;

        _rw.readLock().lock();
        try {
            pconf = _processes.get(pid);
            if (pconf == null) {
                String msg = __msgs.msgProcessNotFound(pid);
                __log.info(msg);
                throw new ContextException(msg);
            }
        } finally {
            _rw.readLock().unlock();
        }

        final DeploymentUnitDir dudir = pconf.getDeploymentUnit();

        // Update in the database.
        ProcessState old = exec(new Callable<ProcessState>() {
            public ProcessState call(ConfStoreConnection conn) {
                DeploymentUnitDAO dudao = conn.getDeploymentUnit(dudir.getName());
                if (dudao == null) {
                    String errmsg = __msgs.msgProcessNotFound(pid);
                    __log.error(errmsg);
                    throw new ContextException(errmsg);
                }

                ProcessConfDAO dao = dudao.getProcess(pid);
                if (dao == null) {
                    String errmsg = __msgs.msgProcessNotFound(pid);
                    __log.error(errmsg);
                    throw new ContextException(errmsg);
                }

                ProcessState old = dao.getState();
                dao.setState(state);
                pconf.setState(state);
                return old;
            }
        });

        pconf.setState(state);
        if (old != null && old != state)
            fireStateChange(pid, state, pconf.getDeploymentUnit().getName());
    }

    public void setRetiredPackage(String packageName, boolean retired) {
        DeploymentUnitDir duDir = _deploymentUnits.get(packageName);
        if (duDir == null) throw new ContextException("Could not find package " + packageName);
        for (QName processName : duDir.getProcessNames()) {
            setState(toPid(processName, duDir.getVersion()), retired ? ProcessState.RETIRED : ProcessState.ACTIVE);
        }
    }

    public ProcessConf getProcessConfiguration(final QName processId) {
        _rw.readLock().lock();
        try {
            return _processes.get(processId);
        } finally {
            _rw.readLock().unlock();
        }
    }

    public void setProperty(final QName pid, final QName propName, final Node value) {
        setProperty(pid, propName, DOMUtils.domToStringLevel2(value));
    }

    public void setProperty(final QName pid, final QName propName, final String value) {
        if (__log.isDebugEnabled())
            __log.debug("Setting property " + propName + " on process " + pid);

        ProcessConfImpl pconf = _processes.get(pid);
        if (pconf == null) {
            String msg = __msgs.msgProcessNotFound(pid);
            __log.info(msg);
            throw new ContextException(msg);
        }

        final DeploymentUnitDir dudir = pconf.getDeploymentUnit();
        exec(new ProcessStoreImpl.Callable<Object>() {
            public Object call(ConfStoreConnection conn) {
                DeploymentUnitDAO dudao = conn.getDeploymentUnit(dudir.getName());
                if (dudao == null)
                    return null;
                ProcessConfDAO proc = dudao.getProcess(pid);
                if (proc == null)
                    return null;
                proc.setProperty(propName, value);
                return null;
            }
        });

        fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.PROPERTY_CHANGED, pid, dudir.getName()));
    }

    /**
     * Load all the deployment units out of the store. Called on start-up.
     *
     */
    public void loadAll() {
        final ArrayList<ProcessConfImpl> loaded = new ArrayList<ProcessConfImpl>();
        exec(new Callable<Object>() {
            public Object call(ConfStoreConnection conn) {
                Collection<DeploymentUnitDAO> dus = conn.getDeploymentUnits();
                for (DeploymentUnitDAO du : dus)
                    try {
                        loaded.addAll(load(du));
                    } catch (Exception ex) {
                        __log.error("Error loading DU from store: " + du.getName(), ex);
                    }
                return null;
            }
        });

        for (ProcessConfImpl p : loaded) {
            try {
                fireStateChange(p.getProcessId(), p.getState(), p.getDeploymentUnit().getName());
            } catch (Exception except) {
                __log.error("Error while activating process: pid=" + p.getProcessId() + " package="+p.getDeploymentUnit().getName(), except);
            }
        }

    }

    public List<QName> getProcesses() {
        _rw.readLock().lock();
        try {
            return new ArrayList<QName>(_processes.keySet());
        } finally {
            _rw.readLock().unlock();
        }
    }

    public long getCurrentVersion() {
        long version = exec(new Callable<Long>() {
            public Long call(ConfStoreConnection conn) {
                return conn.getNextVersion();
            }
        });
        return version;
    }

    protected void fireEvent(ProcessStoreEvent pse) {
        __log.debug("firing event: " + pse);
        for (ProcessStoreListener psl : _listeners)
            psl.onProcessStoreEvent(pse);
    }

    private void fireStateChange(QName processId, ProcessState state, String duname) {
        switch (state) {
        case ACTIVE:
            fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.ACTIVATED, processId, duname));
            break;
        case DISABLED:
            fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.DISABLED, processId, duname));
            break;
        case RETIRED:
            fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.RETIRED, processId, duname));
            break;
        }

    }

    public void registerListener(ProcessStoreListener psl) {
        __log.debug("Registering listener " + psl);
        _listeners.add(psl);
    }

    public void unregisterListener(ProcessStoreListener psl) {
        __log.debug("Unregistering listener " + psl);
        _listeners.remove(psl);
    }

    /**
     * Execute database transactions in an isolated context.
     *
     * @param <T>
     *            return type
     * @param callable
     *            transaction
     * @return
     */
    synchronized <T> T exec(Callable<T> callable) {
        // We want to submit db jobs to an executor to isolate
        // them from the current thread,
        Future<T> future = _executor.submit(callable);
        try {
            return future.get();
        } catch (Exception e) {
            throw new ContextException("DbError", e);
        }
    }

    private ConfStoreConnection getConnection() {
        return _cf.getConnection();
    }

    /**
     * Create a property mapping based on the initial values in the deployment descriptor.
     *
     * @param dd
     * @return
     */
    public static Map<QName, Node> calcInitialProperties(TDeployment.Process dd) {
        HashMap<QName, Node> ret = new HashMap<QName, Node>();
        if (dd.getPropertyList().size() > 0) {
            for (TDeployment.Process.Property property : dd.getPropertyList()) {
                Element elmtContent = DOMUtils.getElementContent(property.getDomNode());
                if (elmtContent != null) {
                    // We'll need DOM Level 3
                    Document doc = DOMUtils.newDocument();
                    doc.appendChild(doc.importNode(elmtContent, true));
                    ret.put(property.getName(), doc.getDocumentElement());
                } else
                    ret.put(property.getName(), property.getDomNode().getFirstChild());

            }
        }
        return ret;
    }

    /**
     * Figure out the initial process state from the state in the deployment descriptor.
     *
     * @param dd
     *            deployment descriptor
     * @return
     */
    private static ProcessState calcInitialState(TDeployment.Process dd) {
        ProcessState state = ProcessState.ACTIVE;

        if (dd.isSetActive() && dd.getActive() == false)
            state = ProcessState.DISABLED;
        if (dd.isSetRetired() && dd.getRetired() == true)
            state = ProcessState.RETIRED;

        return state;
    }

    /**
     * Load a deployment unit record stored in the db into memory.
     *
     * @param dudao
     */
    protected List<ProcessConfImpl> load(DeploymentUnitDAO dudao) {

        __log.debug("Loading deployment unit record from db: " + dudao.getName());

        File dudir = findDeployDir(dudao);

        if (dudir == null || !dudir.exists())
            throw new ContextException("Deployed directory " +
                    (dudir == null ? "(unknown)" : dudir) + " no longer there!");
        DeploymentUnitDir dud = new DeploymentUnitDir(dudir);
        dud.scan();

        ArrayList<ProcessConfImpl> loaded = new ArrayList<ProcessConfImpl>();

        _rw.writeLock().lock();
        try {
            _deploymentUnits.put(dud.getName(), dud);

            long version = 0;
            for (ProcessConfDAO p : dudao.getProcesses()) {
                TDeployment.Process pinfo = dud.getProcessDeployInfo(p.getType());
                if (pinfo == null) {
                    __log.warn("Cannot load " + p.getPID() + "; cannot find descriptor.");
                    continue;
                }

                Map<QName, Node> props = calcInitialProperties(pinfo);
                // TODO: update the props based on the values in the DB.

                ProcessConfImpl pconf = new ProcessConfImpl(p.getPID(), p.getType(), p.getVersion(), dud, pinfo, dudao
                        .getDeployDate(), props, p.getState(), eprContext, _configDir);
                version = p.getVersion();

                _processes.put(pconf.getProcessId(), pconf);
                loaded.add(pconf);
            }

            // All processes and the DU have the same version
            dud.setVersion(version);
        } finally {
            _rw.writeLock().unlock();
        }

        return loaded;
    }

    protected File findDeployDir(DeploymentUnitDAO dudao) {
        File f = new File(dudao.getDeploymentUnitDir());
        if (f.exists())
            return f;
        f = new File(_deployDir, dudao.getName());
        if (f.exists())
            return f;

        return null;
    }

    /**
     * Make sure that the deployment unit is loaded.
     *
     * @param duName
     *            deployment unit name
     */
    protected boolean load(final String duName) {
        _rw.writeLock().lock();
        try {
            if (_deploymentUnits.containsKey(duName))
                return true;
        } finally {
            _rw.writeLock().unlock();
        }

        try {
            return exec(new Callable<Boolean>() {
                public Boolean call(ConfStoreConnection conn) {
                    DeploymentUnitDAO dudao = conn.getDeploymentUnit(duName);
                    if (dudao == null)
                        return false;
                    load(dudao);
                    return true;
                }
            });
        } catch (Exception ex) {
            __log.error("Error loading deployment unit: " + duName);
            return false;
        }

    }

    /**
     * Wrapper for database transactions.
     *
     * @author Maciej Szefler
     *
     * @param <V>
     *            return type
     */
    abstract class Callable<V> implements java.util.concurrent.Callable<V> {
        public V call() {
            boolean success = false;
            ConfStoreConnection conn = getConnection();
            try {
                conn.begin();
                V r = call(conn);
                conn.commit();
                success = true;
                return r;
            } finally {
                if (!success)
                    try {
                        conn.rollback();
                    } catch (Exception ex) {
                        __log.error("DbError", ex);
                    }
                try {
                    conn.close();
                } catch (Exception ex) {
                    __log.error("DbError", ex);
                }
            }

        }

        abstract V call(ConfStoreConnection conn);
    }

    public void setDeployDir(File depDir) {
        if (depDir != null && !depDir.isDirectory())
            throw new IllegalArgumentException("Deploy directory is not a directory or does not exist:  " + depDir);
        _deployDir = depDir;
    }

    public File getDeployDir() {
        return _deployDir;
    }

    public File getConfigDir() {
        return _configDir;
    }

    public void setConfigDir(File configDir) {
        if (configDir != null && !configDir.isDirectory())
            throw new IllegalArgumentException("Config directory is not a directory or does not exist: " + configDir);
        this._configDir = configDir;
    }

    public static DataSource createInternalDS(String guid) {
        jdbcDataSource hsqlds = new jdbcDataSource();
        hsqlds.setDatabase("jdbc:hsqldb:mem:" + guid);
        hsqlds.setUser("sa");
        hsqlds.setPassword("");
        return hsqlds;
    }

    public static void shutdownInternalDB(DataSource ds) {
        try {
            ds.getConnection().createStatement().execute("SHUTDOWN;");
        } catch (SQLException e) {
            __log.error("Error shutting down.", e);
        }
    }

    private List<QName> toPids(Collection<QName> processTypes, long version) {
        ArrayList<QName> result = new ArrayList<QName>();
        for (QName pqName : processTypes) {
            result.add(toPid(pqName, version));
        }
        return result;
    }

    private QName toPid(QName processType, long version) {
        return new QName(processType.getNamespaceURI(), processType.getLocalPart() + "-" + version);
    }

    private DeploymentUnitDir findOldDU(String newName) {
        DeploymentUnitDir old = null;
        int dashIdx = newName.lastIndexOf("-");
        if (dashIdx > 0 && dashIdx + 1 < newName.length()) {
            String radical = newName.substring(0, dashIdx);
            int newVersion = -1;
            try {
                newVersion = Integer.parseInt(newName.substring(newName.lastIndexOf("-") + 1));
            } catch (NumberFormatException e) {
                // Swallowing, if we can't parse then we just can't find an old version
            }
            while (old == null && newVersion >= 0)
                old = _deploymentUnits.get(radical + "-" + (newVersion--));
        }
        return old;
    }

	public void setExtensionValidators(Map<QName, ExtensionValidator> extensionValidators) {
		_extensionValidators = extensionValidators;
	}
    

    private class SimpleThreadFactory implements ThreadFactory {
        int threadNumber = 0;
        public Thread newThread(Runnable r) {
            threadNumber += 1;
            Thread t = new Thread(r, "ProcessStoreImpl-"+threadNumber);
            t.setDaemon(true);
            return t;
        }
    }
    
}
