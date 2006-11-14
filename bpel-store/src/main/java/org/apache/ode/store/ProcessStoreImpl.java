package org.apache.ode.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dd.*;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.store.dao.ConfStoreConnection;
import org.apache.ode.store.dao.ConfStoreConnectionHib;
import org.apache.ode.store.dao.ConfStoreConnectionInMem;
import org.apache.ode.store.dao.ProcessConfDAO;
import org.apache.ode.store.deploy.DeploymentManager;
import org.apache.ode.store.deploy.DeploymentManagerImpl;
import org.apache.ode.store.deploy.DeploymentUnitImpl;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Node;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author mriou <mriou at apache dot org>
 */
public class ProcessStoreImpl implements ProcessStore {

    private static final Log __log = LogFactory.getLog(ProcessStoreImpl.class);
    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    /**
     * Management lock for synchronizing management operations and preventing
     * processing (transactions) from occuring while management operations are
     * in progress.
     */
    private ReadWriteLock _mngmtLock = new ReentrantReadWriteLock();

    private DataSource _ds;
    private DeploymentManager _deploymentManager;
    private Map<QName, DeploymentUnitImpl> _deploymentUnits = new HashMap<QName, DeploymentUnitImpl>();
    private File _appDir;
    private ConfStoreConnection _conn;

    public ProcessStoreImpl(File appDir, DataSource ds, TransactionManager txMgr) {
        this(appDir, ds, new DeploymentManagerImpl(new File(appDir, "processes")), txMgr);
    }

    // Both appdir and datasource could be null
    public ProcessStoreImpl(File appDir, DataSource ds, DeploymentManager deployer, TransactionManager txMgr) {
        _deploymentManager = deployer;
        _appDir = appDir;
        _ds = ds;
        // TODO in-memory if no datasource given
        if (_ds != null) _conn = new ConfStoreConnectionHib(_ds, appDir, txMgr);
        else _conn = new ConfStoreConnectionInMem();

        reloadDeploymentUnits();
    }

    public File getDeploymentDir() {
        return new File(_appDir, "processes");
    }

    /**
     * Deploys a process.
     */
    public Collection<QName> deploy(File deploymentUnitDirectory) {
        __log.info(__msgs.msgDeployStarting(deploymentUnitDirectory));

        _mngmtLock.writeLock().lock();
        try {
            DeploymentUnitImpl du = _deploymentManager.createDeploymentUnit(deploymentUnitDirectory);

            // Checking first that the same process isn't deployed elsewhere
            for (TDeployment.Process processDD : du.getDeploymentDescriptor().getDeploy().getProcessList()) {
                if (_deploymentUnits.get(processDD.getName()) != null) {
                    String duName = _deploymentUnits.get(processDD.getName()).getDeployDir().getName();
                    if (!duName.equals(deploymentUnitDirectory.getName()))
                        throw new BpelEngineException("Process " + processDD.getName() + " is already deployed in " +
                                duName + "");
                }
            }

            ArrayList<QName> deployed = new ArrayList<QName>();
            BpelEngineException failed = null;
            // Going trough each process declared in the dd
            for (TDeployment.Process processDD : du.getDeploymentDescriptor().getDeploy().getProcessList()) {
                // If a type is not specified, assume the process id is also the
                // type.
                QName type = processDD.getType() != null ? processDD.getType() : processDD.getName();
                OProcess oprocess = du.getProcesses().get(type);
                if (oprocess == null)
                    throw new BpelEngineException("Could not find the compiled process definition for BPEL" + "type "
                            + type + " when deploying process " + processDD.getName() + " in "
                            + deploymentUnitDirectory);
                try {
                    deploy(processDD.getName(), du, oprocess, processDD);
                    deployed.add(processDD.getName());
                } catch (Throwable e) {
                    String errmsg = __msgs.msgDeployFailed(processDD.getName(), deploymentUnitDirectory);
                    __log.error(errmsg, e);
                    failed = new BpelEngineException(errmsg, e);
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
            }

            return new HashSet<QName>(du.getProcesses().keySet());
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    private void deploy(final QName processId, final DeploymentUnitImpl du,
                        final OProcess oprocess, TDeployment.Process processDD) {

        _mngmtLock.writeLock().lock();
        try {
            // First, make sure we are undeployed.
            undeploy(processId);

            final ProcessDDInitializer pi = new ProcessDDInitializer(oprocess, processDD);
            try {
                _conn.exec(new ConfStoreConnection.Callable<ProcessConfDAO>() {
                    public ProcessConfDAO run() throws Exception {
                        // Hack, but at least for now we need to ensure that we
                        // are
                        // the only process with this process id.
                        ProcessConfDAO old = _conn.getProcessConf(processId);
                        if (old != null) {
                            String errmsg = __msgs.msgProcessDeployErrAlreadyDeployed(processId);
                            __log.error(errmsg);
                            throw new BpelEngineException(errmsg);
                        }

                        ProcessConfDAO newDao = _conn.createProcess(processId, oprocess.getQName());
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
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    public Collection<QName> undeploy(File file) {
        _mngmtLock.writeLock().lock();
        try {
            ArrayList<QName> undeployed = new ArrayList<QName>();
            DeploymentUnitImpl du = null;
            for (DeploymentUnitImpl deploymentUnit : new HashSet<DeploymentUnitImpl>(_deploymentUnits.values())) {
                if (deploymentUnit.getDeployDir().getName().equals(file.getName()))
                    du = deploymentUnit;
            }
            if (du == null) return undeployed;

            for (QName pName : du.getProcessNames()) {
                if (undeploy(pName)) undeployed.add(pName);
            }

            for (QName pname : du.getProcessNames()) {
                _deploymentUnits.remove(pname);
            }
            _deploymentManager.remove(du);

            return undeployed;
        } finally {
            _mngmtLock.writeLock().unlock();

        }
    }

    public boolean undeploy(final QName process) {
        _mngmtLock.writeLock().lock();
        try {
            if (__log.isDebugEnabled())
                __log.debug("Unregistering process " + process);

            // Delete it from the database.
            boolean deleted = _conn.exec(new ConfStoreConnection.Callable<Boolean>() {
                public Boolean run() throws Exception {
                    ProcessConfDAO proc = _conn.getProcessConf(process);
                    if (proc != null) {
                        proc.delete();
                        __log.info(__msgs.msgProcessUndeployed(process));
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
            });
            return deleted;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            __log.error(__msgs.msgProcessUndeployFailed(process), ex);
            throw new BpelEngineException(ex);
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    public Map<QName, byte[]> getActiveProcesses() {
        _mngmtLock.readLock().lock();
        try {
            if (__log.isDebugEnabled())
                __log.debug("Looking for active processes.");

            return _conn.exec(new ConfStoreConnection.Callable<Map<QName, byte[]>>() {
                public Map<QName, byte[]> run() throws Exception {
                    List<ProcessConfDAO> procs = _conn.getActiveProcesses();
                    HashMap<QName, byte[]> result = new HashMap<QName, byte[]>(procs.size());
                    for (ProcessConfDAO confDAO : procs) {
                        QName processId = confDAO.getProcessId();
                        System.out.println("### Process " + processId + " is active.");
                        if (_deploymentUnits.get(processId) == null) {
                            __log.error("The process " + processId + " appears to exist in the database but no " +
                                    "deployment exists in the file system. Please undeploy it properly.");
                            continue;
                        }
                        OProcess oprocess = _deploymentUnits.get(processId).getProcesses().get(processId);
                        System.out.println("### Process " + processId + " has oprocess " + oprocess + " serializing to " + serialize(oprocess));
                        result.put(processId, serialize(oprocess));
                    }
                    return result;
                }
            });
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BpelEngineException(ex);
        } finally {
            _mngmtLock.readLock().unlock();
        }
    }

    public Map<String, Endpoint> getInvokeEndpoints(QName processId) {
        HashMap<String, Endpoint> partnerRoleIntialValues = new HashMap<String, Endpoint>();
        TDeployment.Process processInfo = getProcessInfo(processId);
        if (processInfo.getInvokeList() != null) {
            for (TInvoke invoke : processInfo.getInvokeList()) {
                String plinkName = invoke.getPartnerLink();
                TService service = invoke.getService();
                // NOTE: service can be null for partner links
                if (service == null) continue;
                __log.debug("Processing <invoke> element for process " + processId + ": partnerlink " + plinkName + " --> "
                        + service);
                partnerRoleIntialValues.put(plinkName, new Endpoint(service.getName(), service.getPort()));
            }
        }
        return partnerRoleIntialValues;
    }

    public Map<String, Endpoint> getProvideEndpoints(QName processId) {
        HashMap<String, Endpoint> myRoleEndpoints = new HashMap<String, Endpoint>();
        TDeployment.Process processInfo = getProcessInfo(processId);
        if (processInfo.getProvideList() != null) {
            for (TProvide provide : processInfo.getProvideList()) {
                String plinkName = provide.getPartnerLink();
                TService service = provide.getService();
                if (service == null) {
                    String errmsg = "Error in <provide> element for process " + processId + "; partnerlink " + plinkName
                            + "did not identify an endpoint";
                    __log.error(errmsg);
                    throw new BpelEngineException(errmsg);
                }
                __log.debug("Processing <provide> element for process " + processId + ": partnerlink " + plinkName + " --> "
                        + service.getName() + " : " + service.getPort());
                myRoleEndpoints.put(plinkName, new Endpoint(service.getName(), service.getPort()));
            }
        }
        return myRoleEndpoints;
    }

    public String[] listDeployedPackages() {
        HashSet<String> deployed = new HashSet<String>();
        for (DeploymentUnitImpl unit : _deploymentUnits.values()) {
            deployed.add(unit.getDeployDir().getName());
        }
        return deployed.toArray(new String[0]);
    }

    public QName[] listProcesses(String packageName) {
        return _deploymentUnits.keySet().toArray(new QName[0]);
    }

    public void markActive(final QName processId, final boolean status) {
        _mngmtLock.writeLock().lock();
        try {
            if (__log.isDebugEnabled())
                __log.debug("Setting property on process " + processId);

            _conn.exec(new ConfStoreConnection.Callable<Object>() {
                public Object run() throws Exception {
                    ProcessConfDAO dao = _conn.getProcessConf(processId);
                    if (dao != null)
                        dao.setActive(status);
                    return null;
                }
            });
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BpelEngineException(ex);
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    public boolean isActive(final QName processId) {
        _mngmtLock.readLock().lock();
        try {
            if (__log.isDebugEnabled())
                __log.debug("Setting property on process " + processId);

            return _conn.exec(new ConfStoreConnection.Callable<Boolean>() {
                public Boolean run() throws Exception {
                    return _conn.getProcessConf(processId).isActive();
                }
            });
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BpelEngineException(ex);
        } finally {
            _mngmtLock.readLock().unlock();
        }
    }

    public ProcessConf getProcessConfiguration(final QName processId) {
        _mngmtLock.readLock().lock();
        try {
            if (__log.isDebugEnabled())
                __log.debug("Setting property on process " + processId);

            return _conn.exec(new ConfStoreConnection.Callable<ProcessConf>() {
                public ProcessConf run() throws Exception {
                    ProcessConfDAO confDAO = _conn.getProcessConf(processId);
                    return buildConf(confDAO);
                }
            });
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BpelEngineException(ex);
        } finally {
            _mngmtLock.readLock().unlock();
        }
    }

    public List<String> getMexInterceptors(QName processId) {
        ArrayList<String> mexi = new ArrayList<String>();
        TDeployment.Process processInfo = getProcessInfo(processId);
        if (processInfo.getMexInterceptors() != null) {
            for (TMexInterceptor mexInterceptor : processInfo.getMexInterceptors().getMexInterceptorList()) {
                mexi.add(mexInterceptor.getClassName());
            }
        }
        return mexi;
    }

    public Definition getDefinitionForService(QName processId, QName serviceName) {
        DeploymentUnit du = _deploymentUnits.get(processId);
        return du.getDefinitionForService(serviceName);
    }

    private TDeployment.Process getProcessInfo(QName pid) {
        DeployDocument deployDoc = _deploymentUnits.get(pid).getDeploymentDescriptor();
        for (TDeployment.Process procInfo : deployDoc.getDeploy().getProcessList()) {
            if (procInfo.getName().equals(pid)) return procInfo;
        }
        throw new BpelEngineException("Process not found: " + pid);
    }

    public void setProperty(final QName processId, final String name, final String namespace, final Node value) {
        _mngmtLock.writeLock().lock();
        try {
            if (__log.isDebugEnabled())
                __log.debug("Setting property on process " + processId);

            // Delete it from the database.
            _conn.exec(new ConfStoreConnection.Callable<Object>() {
                public Object run() throws Exception {
                    ProcessConfDAO proc = _conn.getProcessConf(processId);
                    if (proc == null) {
                        String msg = __msgs.msgProcessNotFound(processId);
                        __log.info(msg);
                        throw new BpelEngineException(msg);
                    }
                    proc.setProperty(name, namespace, value);
                    return null;
                }
            });
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BpelEngineException(ex);
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    public void setProperty(final QName processId, final String name, final String namespace, final String value) {
        _mngmtLock.writeLock().lock();
        try {
            if (__log.isDebugEnabled())
                __log.debug("Setting property on process " + processId);

            // Delete it from the database.
            _conn.exec(new ConfStoreConnection.Callable<Object>() {
                public Object run() throws Exception {
                    ProcessConfDAO proc = _conn.getProcessConf(processId);
                    if (proc == null) {
                        String msg = __msgs.msgProcessNotFound(processId);
                        __log.info(msg);
                        throw new BpelEngineException(msg);
                    }
                    proc.setProperty(name, namespace, value);
                    return null;
                }
            });
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BpelEngineException(ex);
        } finally {
            _mngmtLock.writeLock().unlock();
        }
    }

    public List<String> getEventsSettings(QName processId, List<String> scopeNames) {
        List<String> result = null;
        TDeployment.Process processInfo = getProcessInfo(processId);
        TProcessEvents processEvents = processInfo.getProcessEvents();
        if (processEvents == null || (processEvents.getGenerate() != null
                && processEvents.getGenerate().equals(TProcessEvents.Generate.ALL))) {
            result = new ArrayList<String>(1);
            result.add("all");
            return result;
        }

        if (processEvents.getEnableEventList() != null) result = processEvents.getEnableEventList();
        if (processEvents.getScopeEventsList() != null && scopeNames != null) {
            List<String> scopeEvents = processScopeEvents(scopeNames, processEvents.getScopeEventsList());
            if (scopeEvents != null) {
                result = scopeEvents;
            }
        }

        if (result == null) {
            return new ArrayList<String>(1);
        } else {
            return result;
        }
    }

    private List<String> processScopeEvents(List<String> scopeNames, List<TScopeEvents> scopeEventsList) {
        for (String scopeName : scopeNames) {
            for (TScopeEvents scopeEvents : scopeEventsList) {
                if (scopeEvents.getName().equals(scopeName)) {
                    return scopeEvents.getEnableEventList();
                }
            }
        }
        return null;
    }

    private ProcessConf buildConf(ProcessConfDAO dao) {
        DeploymentUnit du = _deploymentUnits.get(dao.getProcessId());
        ProcessConfImpl conf = new ProcessConfImpl();
        TDeployment.Process processInfo = getProcessInfo(dao.getProcessId());
        conf.setActive(dao.isActive());
        conf.setDeployDate(dao.getDeployDate());
        conf.setDeployer(dao.getDeployer());
        conf.setFiles(du.allFiles().toArray(new File[0]));
        conf.setPackageName(du.getDeployDir().getName());
        conf.setProcessId(dao.getProcessId());
        conf.setProps(dao.getProperties());
        conf.setInMemory(processInfo.isSetInMemory() && processInfo.getInMemory());
        return conf;
    }

    private void reloadDeploymentUnits() {
        for (DeploymentUnitImpl du : _deploymentManager.getDeploymentUnits())
            try {
                for (QName procName : du.getProcessNames()) {
                    _deploymentUnits.put(procName, du);
                }
            } catch (Exception ex) {
                String errmsg = "Error processing deployment unit " + du.getDeployDir()
                        + "; some processes may not be loaded.";
                __log.error(errmsg, ex);
            }
    }

    private byte[] serialize(OProcess oprocess) {
        Serializer serializer = new Serializer(oprocess.compileDate.getTime(), 1);
        final byte[] bits;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            serializer.write(bos);
            serializer.writeOProcess(oprocess, bos);
            bos.close();
            bits = bos.toByteArray();
            return bits;
        } catch (Exception ex) {
            String errmsg = "Error re-serializing CBP";
            __log.fatal(errmsg, ex);
            throw new BpelEngineException(errmsg, ex);
        }
    }

}
