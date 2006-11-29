package org.apache.ode.store;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dd.DeployDocument;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TDeployment.Process;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.store.DeploymentUnitDir.CBPInfo;
import org.apache.ode.store.hib.DbConfStoreConnectionFactory;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.msg.MessageBundle;
import org.hsqldb.jdbc.jdbcDataSource;
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

    /** Guards access to the _processes and _deploymentUnits */
    private final ReadWriteLock _rw = new ReentrantReadWriteLock();

    /** GUID used to create a unique in-memory db. */
    private String _guid = new GUID().toString();

    private DbConfStoreConnectionFactory _cf;

    /**
     * Executor used to process DB transactions. Allows us to isolate the TX context, and to ensure that only one TX gets executed a
     * time. We don't really care to parallelize these operations because: i) HSQL does not isolate transactions and we don't want
     * to get confused ii) we're already serializing all the operations with a read/write lock. iii) we don't care about
     * performance, these are infrequent operations.
     */
    private ExecutorService _executor = Executors.newSingleThreadExecutor();

    /**
     * In-memory DataSource, or <code>null</code> if we are using a real DS. We need this to shutdown the DB.
     */
    private DataSource _inMemDs;

    public ProcessStoreImpl() {
        this(null);
    }

    public ProcessStoreImpl(DataSource ds) {
        if (ds != null) {
            _cf = new DbConfStoreConnectionFactory(ds, false);
        } else {

            // If the datasource is not provided, then we create a HSQL-based in-memory
            // database. Makes testing a bit simpler.
            jdbcDataSource hsqlds = new jdbcDataSource();
            hsqlds.setDatabase("jdbc:hsqldb:mem:" + _guid);
            hsqlds.setUser("sa");
            hsqlds.setPassword("");
            _cf = new DbConfStoreConnectionFactory(hsqlds, true);
            _inMemDs = hsqlds;
        }

    }

    public void shutdown() {
        if (_inMemDs != null) {
            try {
                _inMemDs.getConnection().createStatement().execute("SHUTDOWN;");
            } catch (SQLException e) {
                __log.error("Error shutting down.", e);
            }
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
        du.compile();
        du.scan();
        DeployDocument dd = du.getDeploymentDescriptor();
        final ArrayList<ProcessConfImpl> processes = new ArrayList<ProcessConfImpl>();
        Collection<QName> deployed;

        _rw.writeLock().lock();

        try {
            if (_deploymentUnits.containsKey(du.getName())) {
                String errmsg = __msgs.msgDeployFailDuplicateDU(du.getName());
                __log.error(errmsg);
                throw new ContextException(errmsg);
            }

            for (TDeployment.Process processDD : dd.getDeploy().getProcessList()) {
                if (_processes.containsKey(processDD.getName())) {
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

                // final OProcess oprocess = loadCBP(cbpInfo.cbp);
                ProcessConfImpl pconf = new ProcessConfImpl(du, processDD, deployDate, calcInitialProperties(processDD),
                        calcInitialState(processDD));
                processes.add(pconf);

            }

            _deploymentUnits.put(du.getName(),du);
            
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
                        ProcessConfDAO newDao = dudao.createProcess(pc.getProcessId(), pc.getType());
                        newDao.setState(pc.getState());
                        for (Map.Entry<QName, Node> prop : pc.getProperties().entrySet()) {
                            newDao.setProperty(prop.getKey(), DOMUtils.domToString(prop.getValue()));
                        }
                        deployed.add(pc.getProcessId());
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
        for (ProcessConfImpl process : processes) {
            fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.DEPLOYED, process.getProcessId()));
            fireStateChange(process.getProcessId(), process.getState());
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
        _rw.writeLock().lock();
        try {
            DeploymentUnitDir du = _deploymentUnits.remove(dir.getName());
            if (du != null) {
                undeployed = du.getProcessNames();
                _processes.keySet().removeAll(undeployed);
            }
        } finally {
            _rw.writeLock().unlock();
        }

        for (QName pn : undeployed) {
            fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.UNDEPLOYED, pn));
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
            return new ArrayList<QName>(du.getProcessNames());
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
                return old;
            }
        });

        if (old != null && old != state)
            fireStateChange(pid, state);
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

        fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.PROPERTY_CHANGED, pid));
    }

    /**
     * Load all the deployment units out of the store. Called on start-up.
     * 
     */
    public void loadAll() {
        exec(new Callable<Object>() {
            public Object call(ConfStoreConnection conn) {
                Collection<DeploymentUnitDAO> dus = conn.getDeploymentUnits();
                for (DeploymentUnitDAO du : dus)
                    try {
                        load(du);
                    } catch (Exception ex) {
                        __log.error("Error loading DU from store: " + du.getName(), ex);
                    }
                return null;
            }
        });

    }

    public List<QName> getProcesses() {
        _rw.readLock().lock();
        try {
            return new ArrayList<QName>(_processes.keySet());
        } finally {
            _rw.readLock().unlock();
        }
    }

    protected void fireEvent(ProcessStoreEvent pse) {
        for (ProcessStoreListener psl : _listeners)
            try {
                psl.onProcessStoreEvent(pse);
            } catch (Throwable t) {
                __log.error("Exception in listener.", t);
            }
    }

    private void fireStateChange(QName processId, ProcessState state) {
        switch (state) {
        case ACTIVE:
            fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.ACTVIATED, processId));
            break;
        case DISABLED:
            fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.DISABLED, processId));
            break;
        case RETIRED:
            fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.RETIRED, processId));
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
     * @param <T> return type
     * @param callable transaction
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
     * Create a property mapping based on the initial values in the deployment 
     * descriptor.
     * @param dd
     * @return
     */
    private static Map<QName, Node> calcInitialProperties(TDeployment.Process dd) {
        HashMap<QName, Node> ret = new HashMap<QName, Node>();
        if (dd.getPropertyList().size() > 0) {
            for (TDeployment.Process.Property property : dd.getPropertyList()) {
                Element elmtContent = DOMUtils.getElementContent(property.getDomNode());
                if (elmtContent != null)
                    ret.put(property.getName(), elmtContent);
                else
                    ret.put(property.getName(), property.getDomNode().getFirstChild());

            }
        }
        return ret;
    }

    /**
     * Figure out the initial process state from the state in the deployment descriptor. 
     * @param dd deployment descriptor
     * @return
     */
    private static ProcessState calcInitialState(TDeployment.Process dd) {
        ProcessState state = ProcessState.DISABLED;

        if (dd.isSetActive())
            state = ProcessState.ACTIVE;
        if (dd.isSetRetired())
            state = ProcessState.RETIRED;

        return state;
    }

    /**
     * Load a deployment unit record stored in the db into memory.
     * @param dudao
     */
    private void load(DeploymentUnitDAO dudao) {

        __log.debug("Loading deployment unit record from db: " + dudao.getName());

        File dudir = new File(dudao.getDeploymentUnitDir());
        if (!dudir.exists())
            throw new ContextException("Deployed directory " + dudir + " no longer there!");
        DeploymentUnitDir dud = new DeploymentUnitDir(dudir);
        dud.scan();

        ArrayList<ProcessConfImpl> loaded = new ArrayList<ProcessConfImpl>();

        _rw.writeLock().lock();
        try {
            // NOTE: we don't try to reload things here.
            if (_deploymentUnits.containsKey(dudao.getName())) {
                __log.debug("Skipping load of " + dudao.getName() + ", it is already loaded.");
                return;
            }

            _deploymentUnits.put(dud.getName(),dud);
            
            for (ProcessConfDAO p : dudao.getProcesses()) {
                Process pinfo = dud.getProcessDeployInfo(p.getPID());
                if (pinfo == null) {
                    __log.warn("Cannot load " + p.getPID() + "; cannot find descriptor.");
                    continue;
                }

                Map<QName, Node> props = calcInitialProperties(pinfo);
                // TODO: update the props based on the values in the DB.

                ProcessConfImpl pconf = new ProcessConfImpl(dud, pinfo, dudao.getDeployDate(), props, p.getState());

                _processes.put(pconf.getProcessId(), pconf);
                loaded.add(pconf);
            }

        } finally {
            _rw.writeLock().unlock();
        }

        // Fire the events outside of the lock
        for (ProcessConfImpl p : loaded)
            fireStateChange(p.getProcessId(), p.getState());

    }

    /**
     * Wrapper for database transactions.
     * @author Maciej Szefler
     *
     * @param <V> return type
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
}
