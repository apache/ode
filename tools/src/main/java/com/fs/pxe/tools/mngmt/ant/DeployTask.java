/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.ant;

import com.fs.pxe.tools.ExecutionException;
import com.fs.pxe.tools.mngmt.Deploy;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class DeployTask extends JmxCommandTask {
  
  private String _sarUrl;
  private boolean _activate = true;
  
  public void setSarUrl(String s) {
    _sarUrl= s;
  }
  
  public void setActivateOnDeploy(boolean f) {
    _activate = f;
  }
  public void execute() throws BuildException {
    Deploy d = new Deploy();
    if (getDomain() != null) {
      d.setDomainUuid(getDomain());
    }
    if (getJmxurl() != null) {
      d.setJmxUrl(getJmxurl());
    }
    if (getJmxusername() != null) {
      d.setJmxUsername(getJmxusername());
    }
    if (getJmxpassword() != null) {
      d.setJmxPassword(getJmxpassword());
    }
    if (_sarUrl != null) {
      d.setSarUrl(_sarUrl);
    }
    d.setActivateOnDeploy(_activate);
    try {
      d.execute(this);
    } catch (ExecutionException ee) {
      if (getFailOnError()) {
        throw new BuildException(ee);
      }
      log(ee.getMessage(),Project.MSG_ERR);
    }
  }
}
