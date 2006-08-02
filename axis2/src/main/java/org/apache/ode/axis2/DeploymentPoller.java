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

package org.apache.ode.axis2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.deploy.DeploymentUnitImpl;
import org.apache.ode.bpel.iapi.DeploymentUnit;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Polls a directory for the deployment of a new deployment unit.
 */
public class DeploymentPoller {

  private static Log __log = LogFactory.getLog(DeploymentPoller.class);

  /** The polling interval. */
  private static final long POLL_TIME = 3000;

  private File _deployDir;
  private PollingThread _poller;
  private ODEServer _odeServer;
  private HashMap<String,Long> _quarantined = new HashMap<String, Long>();

  /**
   * Set of {@link DeploymentUnit} objects regarding all deployment units that have been inspected.
   */
  private final HashMap<String,DeploymentUnit> _inspectedFiles = new HashMap<String, DeploymentUnit>();

  /** Filter accepting directories containing a .odedd file. */
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

  public DeploymentPoller(File deployDir, ODEServer odeServer) {
    _odeServer = odeServer;
    _deployDir = deployDir;
    if (!_deployDir.exists())
      _deployDir.mkdir();
    for (DeploymentUnitImpl deploymentUnit : _odeServer.getBpelServer().getDeployedUnits()) {
      _inspectedFiles.put(deploymentUnit.getDuDirectory().getName(), deploymentUnit);
    }
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

    // Checking if a quarantined (failed) deployment has been updated
    if (_quarantined.size() > 0) {
      ArrayList<String> removals = new ArrayList<String>();
      for (Map.Entry<String, Long> entry : _quarantined.entrySet()) {
        if (new File(entry.getKey()).lastModified() > entry.getValue()) {
          removals.add(entry.getKey());
        }
      }
      for (String file : removals) _quarantined.remove(file);
    }

    // Checking for new deployment directories
    for (File file : files) {
      File deployXml = new File(file, "deploy.xml");
      if (checkIsNew(deployXml) && _quarantined.get(deployXml.getAbsolutePath()) == null) {
        try {
          DeploymentUnit du = _odeServer.getBpelServer().deploy(file);
          _inspectedFiles.put(file.getName(), du);
          __log.info("Deployment of artifact " + file.getName() + " successful.");
        } catch (Exception e) {
          __log.error("Deployment of " + file.getName() + " failed, aborting for now.", e);
          _quarantined.put(deployXml.getAbsolutePath(), deployXml.lastModified());
        }
      }
    }

    // Removing deployments that disappeared
    HashSet<String> removed = new HashSet<String>();
    for (String duName : _inspectedFiles.keySet()) {
      DeploymentUnit du = _inspectedFiles.get(duName);
      if (du.removed()) {
//        _odeServer.getBpelServer().undeploy(du);
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
        return deployed.checkForUpdate();
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
