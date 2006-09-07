package org.apache.ode.bpel.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple implementation of the
 * {@link org.apache.ode.bpel.deploy.DeploymentManager} interface.
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 * 
 */
public class DeploymentManagerImpl implements DeploymentManager {

    private static final Log __log = LogFactory.getLog(DeploymentManagerImpl.class);
    private File _deployStateFile;

    private ArrayList<DeploymentUnitImpl> _knownDeployments = new ArrayList<DeploymentUnitImpl>();

    /** Lock to prevent clobbering of the file. */
    private ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();

    private long _lastRead = 0;

    public DeploymentManagerImpl(File deployStateFile) {
        _deployStateFile = deployStateFile;
    }

    public DeploymentUnitImpl createDeploymentUnit(String location) {
        return createDeploymentUnit(new File(location));
    }

    public DeploymentUnitImpl createDeploymentUnit(File deploymentUnitDirectory) {
        read();
        
        _rwLock.writeLock().lock();
        try {
            DeploymentUnitImpl du = new DeploymentUnitImpl(deploymentUnitDirectory);
            _knownDeployments.add(du);
            write();
            return du;
        } finally {
            _rwLock.writeLock().unlock();
        }
    }

    public void remove(DeploymentUnitImpl du) {
        read();
        
        _rwLock.writeLock().lock();
        try {
            if (!_knownDeployments.remove(du))
                return;

            write();
            rm(du.getDeployDir());
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
     * Remove a file or directory, possibly recursively.
     * 
     * @param f
     */
    private void rm(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles())
                rm(child);
            f.delete();
        } else {
            f.delete();
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
                        try {
                        _knownDeployments.add(new DeploymentUnitImpl(new File(lin)));
                        } catch (Exception ex) {
                            __log.debug("Failed to load DU (skipping): " + lin,ex);
                            ; // skip it. 
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
                    writer.println(du.getDeployDir().toString());
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
}
