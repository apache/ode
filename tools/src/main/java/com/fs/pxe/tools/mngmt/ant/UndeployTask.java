/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.ant;

import com.fs.pxe.tools.ExecutionException;
import com.fs.pxe.tools.mngmt.Undeploy;

import org.apache.tools.ant.BuildException;

public class UndeployTask extends SystemTask {
  
  public void execute() throws BuildException {
    Undeploy u = new Undeploy();
    super.setup(u);
    try {
      u.execute(this);
    } catch (ExecutionException ee) {
      if (getFailOnError()) {
        throw new BuildException(ee);
      }
      handleErrorOutput(ee.getMessage());
    }
  }
}
