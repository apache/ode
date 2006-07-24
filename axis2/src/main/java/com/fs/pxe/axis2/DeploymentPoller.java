package com.fs.pxe.axis2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.util.HashSet;
import java.util.HashMap;

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

  /**
   * Set of {@link DeploymentUnit} objects regarding all deployment units that have been inspected.
   */
  private final HashMap<String,DeploymentUnit> _inspectedFiles = new HashMap<String,DeploymentUnit>();

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
    readState();
    _poller.start();
    __log.info("Poller started.");
  }

  public void stop() {
    _poller.kill();
    writeState();
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
          _inspectedFiles.put(file.getName(), du);
          du.deploy(false);
          __log.info("Deployment of artifact " + file.getName() + " successful.");
        } catch (Exception e) {
          __log.error("Deployment of " + file.getName() + " failed, aborting for now.", e);
        }
      }
    }

    // Removing deployments that disappeared
    HashSet<String> removed = new HashSet<String>();
    for (String duName : _inspectedFiles.keySet()) {
      DeploymentUnit du = _inspectedFiles.get(duName);
      if (!du.exists()) {
        du.undeploy();
        removed.add(duName);
      }
    }
    if (removed.size() > 0) {
      for (String duName : removed) {
        _inspectedFiles.remove(duName);
      }
    }
  }

  /**
   * Check if a file has not been seen before.
   * @param f file to check
   * @return <code>true</code> if new
   */
  private boolean checkIsNew(File f){
    for (DeploymentUnit deployed : _inspectedFiles.values()) {
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

  private void readState() {
    File duState = new File(_deployDir, ".state");
    if (duState.exists()) {
      try {
        BufferedReader duStateReader = new BufferedReader(new FileReader(duState));
        String line;
        while ((line = duStateReader.readLine()) != null) {
          String filename = line.substring(0, line.indexOf("|"));
          String timestamp = line.substring(line.indexOf("|") + 1 , line.length());
          File duFile = new File(_deployDir, filename);
          if (duFile.exists()) {
            DeploymentUnit du = new DeploymentUnit(duFile, _pxeServer);
            du.setLastModified(Long.valueOf(timestamp));
            _inspectedFiles.put(duFile.getName(), du);
            du.deploy(true);
          }
        }
      } catch (FileNotFoundException e) {
        // Shouldn't happen
      } catch (IOException e) {
        __log.error("An error occured while reading past deployments states, some " +
                "processes will be redeployed.", e);
      }
    } else {
      __log.info("Couldn't find any deployment history, all processes will " +
              "be redeployed.");
    }
  }

  private void writeState() {
    try {
      __log.debug("Writing current deployment state.");
      FileWriter duStateWriter = new FileWriter(new File(_deployDir, ".state"), false);
      for (DeploymentUnit deploymentUnit : _inspectedFiles.values()) {
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
}
