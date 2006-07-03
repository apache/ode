/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.ant;

import com.fs.pxe.tools.ExecutionException;
import com.fs.pxe.tools.mngmt.Activate;

import org.apache.tools.ant.BuildException;

public class ActivateTask extends SystemTask {
  

  public void execute() throws BuildException {
    Activate a = new Activate();
    super.setup(a);
    try {
      a.execute(this);
    } catch (ExecutionException ee) {
      if (getFailOnError()) {
        throw new BuildException(ee);
      }
      handleErrorOutput(ee.getMessage());
    }
  }
}
