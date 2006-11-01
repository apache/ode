package org.apache.ode.store.deploy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.fs.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A simple implementation of the
 * {@link org.apache.ode.store.deploy.DeploymentManager} interface.
 *
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class DeploymentManagerImpl implements DeploymentManager {

    private static final Log __log = LogFactory.getLog(DeploymentManagerImpl.class);
    private File _deployDir;
    private File _deployStateFile;

    private HashSet<DeploymentUnitImpl> _knownDeployments = new HashSet<DeploymentUnitImpl>();
    private HashSet<String> _deploymentsList = new HashSet<String>();

    /** Lock to prevent clobbering of the file. */
    private ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();

    private long _lastRead = 0;

    public DeploymentManagerImpl(File deployDir) {
        _deployDir = deployDir;
        _deployStateFile = new File(deployDir.getParentFile(), "ode-deployed.dat");
    }

    public DeploymentUnitImpl createDeploymentUnit(String location) {
        return createDeploymentUnit(new File(location));
    }

    public DeploymentUnitImpl createDeploymentUnit(File deploymentUnitDirectory) {
        read();
        _rwLock.writeLock().lock();
        try {
            _deploymentsList.add(deploymentUnitDirectory.getName());
            DeploymentUnitImpl du = new DeploymentUnitImpl(deploymentUnitDirectory);
            _knownDeployments.add(du);
            write();
            return du;
        } finally {
            _rwLock.writeLock().unlock();
        }
    }

    public void remove(DeploymentUnitImpl du) {
//        read();
        _rwLock.writeLock().lock();
        try {
            if (!_knownDeployments.remove(du))
                return;
            _deploymentsList.remove(du.getDeployDir().getName());
            write();
            FileUtils.deepDelete(du.getDeployDir());
        } finally {
            _rwLock.writeLock().unlock();
        }
    }

    public Collection<DeploymentUnitImpl> getDeploymentUnits() {
        read();
        _rwLock.writeLock().lock();
        try {
            return new ArrayList<DeploymentUnitImpl>(_knownDeployments);
        } finally {
            _rwLock.writeLock().unlock();
        }
    }

    /**
     * Read the file containing list of deployment units from disk.
     *
     */
    private void read() {
        _rwLock.writeLock().lock();
        try {
            if (!_deployStateFile.exists()) {
                _knownDeployments.clear();
                return;
            }

            if (_deployStateFile.lastModified() > _lastRead) {
                LineNumberReader reader = new LineNumberReader(new FileReader(_deployStateFile));
                _knownDeployments.clear();
                try {
                    String lin;
                    while ((lin = reader.readLine()) != null) {
                        _deploymentsList.add(lin);
                        try {
                            _knownDeployments.add(new DeploymentUnitImpl(new File(_deployDir, lin)));
                        } catch (Exception ex) {
                            DeploymentManagerImpl.__log.debug("Failed to load DU (skipping): " + lin,ex);
                        }
                    }

                    _lastRead = _deployStateFile.lastModified();
                } finally {
                    reader.close();
                }
            }
        } catch (IOException ioex) {
            throw new RuntimeException(ioex);
        } finally {
            _rwLock.writeLock().unlock();
        }
    }

    /**
     * Write the file containing the list of deployment units to disk.
     *
     */
    private void write() {
        _rwLock.writeLock().lock();
        try {
            PrintWriter writer = new PrintWriter(_deployStateFile);
            try {
                for (DeploymentUnitImpl du : _knownDeployments) {
                    writer.println(du.getDeployDir().getName());
                }
            } finally {
                writer.close();
            }

        } catch (IOException ioex) {
            throw new RuntimeException(ioex);
        } finally {
            _rwLock.writeLock().unlock();
        }
    }

    public Set<String> getDeploymentsList() {
        return _deploymentsList;
    }
}
