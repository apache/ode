package org.apache.ode.jbi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.jbi.management.DeploymentException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Representation of a JBI service unit. A JBI service unit may actually
 * consist of multiple processes.
 */
class OdeServiceUnit {
  private static final Log __log = LogFactory.getLog(OdeServiceUnit.class);
  private static final Messages __msgs = Messages.getMessages(Messages.class);

  /** The ever-present context. */
  private OdeContext _ode;

  /** Our own directory managed by JBI */
  private File _serviceUnitRootPath;

  /** Our JBI indentifier. */
  private String _serviceUnitID;
  
  /** List of files that contain BPEL deployment descriptors. */
  private List<File> _descriptors;
  
  /** List of process IDs for each of the above files. */
  
  private List<QName> _pids;
    
  /** Ctor. */
  OdeServiceUnit(OdeContext ode, String serviceUnitID, String serviceUnitRootPath) {
    _ode = ode;
    _serviceUnitID = serviceUnitID;
    _serviceUnitRootPath = new File(serviceUnitRootPath);
    // Scan the SU root path for deployment descriptors (.dd) files.
    _descriptors = findDescriptors(_serviceUnitRootPath);
    _pids = new ArrayList<QName>(_descriptors.size());
    for (File d : _descriptors)
      _pids.add(genPid(serviceUnitID,d.getName()));
  }

  public void deploy() throws DeploymentException {
    boolean abortOnFailure = !_ode._config.getAllowIncompleteDeployment();


    List<QName> deployed = new ArrayList<QName>(_descriptors.size());
    Exception ex1 = null;
    for (File dd : _descriptors) {
      try {
        QName pid = genPid(_serviceUnitID, dd.getName());
        _ode._server.deploy(pid, dd.toURI());
        deployed.add(pid);
      } catch (Exception ex) {
        __log
            .error(__msgs.msgOdeProcessDeploymentFailed(dd, _serviceUnitID), ex);
        ex1 = ex;
        if (abortOnFailure)
          break;
      }
    }

    if ((abortOnFailure && (deployed.size() != _descriptors.size()))
        || (_descriptors.size() == 1 && deployed.size() == 0)) {
      for (QName x : deployed)
        try {
          _ode._server.undeploy(x);
        } catch (Exception ex) {
          __log
              .error("Unexpected, failed to undeploy " + x + "; ignoring.", ex);
        }
      throw new DeploymentException(ex1.getMessage(),ex1);
    }
    
  }

  public void undeploy() throws Exception {
    Exception ex1 = null;
    for (QName x : _pids)
      try {
        boolean undeployed = _ode._server.undeploy(x);
        if (undeployed)
          __log.debug("undeployed ode process: " + x);
        else
          __log.debug("skipped undeployment (not deployed): " +x);
      } catch (Exception ex) {
        String errmsg = __msgs.msgOdeProcessUndeploymentFailed(x);
        __log.error(errmsg,ex);
        ex1 = new DeploymentException(errmsg,ex);
      }
      
    // Throw the first exception we got.
    if (ex1 != null)
      throw ex1;
      
  }

  public void init() throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void shutdown() throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void start() throws Exception {
    List<QName> activated = new ArrayList<QName>(_pids.size());
    Exception e = null;
    for (QName pid : _pids) {
      try {
        _ode._server.activate(pid,false);
        activated.add( pid );
      } catch (Exception ex) {
        e = ex;
        __log.error("Unable to activate " + pid,ex);
        break;
      }
    }
    if (activated.size() != _pids.size()) {
      for (QName pid : activated)
        try {
          _ode._server.deactivate(pid,true);
        } catch (Exception ex) {
          __log.error("Unable to deactivate " + pid,ex);
        }
    }
    
    if(e != null)
      throw e;
  }

  public void stop() throws Exception {
    for (QName pid : _pids) {
      try {
        _ode._server.deactivate(pid,true);
      } catch (Exception ex) {
        __log.error("Unable to deactivate " + pid,ex);
      }
    }
  }


  /**
   * Generate a "PID" (process identifier) for a service unit and deployment
   * descriptor file. 
   * @param serviceUnitID service unit id
   * @param ddName deployment descriptor file name
   * @return
   */
  private QName genPid(String serviceUnitID, String ddName) {
    return new QName(_ode._config.getPidNamespace(), serviceUnitID + "/"
        + ddName);
  }

  /**
   * Find all the BPEL deployment descriptors in the given directory.
   * @param supath directory
   * @return
   */
  private List<File> findDescriptors(File supath) {
    File[] files = supath.listFiles();
    List<File> ret = new ArrayList<File>(files.length);
    for (File f : files)
      if (f.isFile() && 
          (f.getName().toLowerCase().endsWith(".dd") 
              || f.getName().toLowerCase().endsWith(".dd.xml")))
        ret.add(f);
    return ret;
  }

}
