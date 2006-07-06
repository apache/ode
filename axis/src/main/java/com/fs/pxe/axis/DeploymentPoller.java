package com.fs.pxe.axis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.HashSet;

/**
 * Polls a directory for the deployment of a new deployment unit.
 */
public class DeploymentPoller {

  private static Log __log = LogFactory.getLog(DeploymentPoller.class);

  /** The polling interval. */
  private static final long POLL_TIME = 3000;

  private File _deployDir;
  private PollingThread _poller;
  private PXEServer _pxeServer;
  private boolean _initDone = false;

  /**
   * Set of {@link DeploymentUnit} objects regarding all deployment units that have been inspected.
   */
  private final HashSet<DeploymentUnit> _inspectedFiles = new HashSet<DeploymentUnit>();

  /** Filter accepting directories containing a .pxedd file. */
  private static final FileFilter _fileFilter = new FileFilter(){
    public boolean accept(File path) {
      if (path.isDirectory()) {
        return path.listFiles(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.toLowerCase().equals("deploy.xml");
          }
        }).length == 1;
      } else return false;
    }
  };

  public DeploymentPoller(File deployDir, PXEServer pxeServer) {
    _pxeServer = pxeServer;
    _deployDir = deployDir;
    if (!_deployDir.exists())
      _deployDir.mkdir();
  }

  public void start() {
    _poller = new PollingThread();
    _poller.start();
    __log.info("Poller started.");
  }

  public void stop() {
    _poller.kill();
    _poller = null;
  }

  /**
   * Scan the directory for new (or removed) files (called mainly from {@link PollingThread})
   * and calls whoever is in charge of the actual deployment (or undeployment).
   */
  private void check(){
    File[] files = _deployDir.listFiles(_fileFilter);

    // Checking for new deployment directories
    for (File file : files) {
      if (checkIsNew(new File(file, "deploy.xml"))) {
        try {
          DeploymentUnit du = new DeploymentUnit(file, _pxeServer);
          _inspectedFiles.add(du);
          du.deploy(!_initDone);
        } catch (Exception e) {
          __log.error("Deployment of " + file.getName() + " failed, aborting for now.", e);
        }
      }
    }

    // Removing deployments that disappeared
    HashSet<DeploymentUnit> removed = new HashSet<DeploymentUnit>(2);
    for (DeploymentUnit du : _inspectedFiles) {
      if (!du.exists()) {
        du.undeploy();
        removed.add(du);
      }
    }
    if (removed.size() > 0) {
      for (DeploymentUnit du : removed) {
        _inspectedFiles.remove(du);
      }
    }

    _initDone = true;
  }

  /**
   * Check if a file has not been seen before.
   * @param f file to check
   * @return <code>true</code> if new
   */
  private boolean checkIsNew(File f){
    for (DeploymentUnit deployed : _inspectedFiles) {
      if (deployed.matches(f))
        return false;
    }
    return true;
  }

  /**
   * Thread that does the actual polling for new files.
   */
  private class PollingThread extends Thread {
    private boolean _active = true;

    /** Stop this poller, and block until it terminates. */
    void kill() {
      synchronized(this) {
        _active = false;
        this.notifyAll();
      }
      try {
        join();
      } catch (InterruptedException ie) {
        __log.fatal("Thread unexpectedly interrupted.", ie);
      }
    }

    public void run(){
      try {
        while(_active) {
          check();
          synchronized(this){
            try{
              this.wait(POLL_TIME);
            } catch(InterruptedException e){}
          }
        }
      } catch(Throwable t){
        __log.fatal("Encountered an unexpected error.  Exiting poller...", t);
      }
    }
  }

}
