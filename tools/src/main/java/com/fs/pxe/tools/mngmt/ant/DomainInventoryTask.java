/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.ant;

import com.fs.pxe.tools.ExecutionException;
import com.fs.pxe.tools.mngmt.DomainInventory;

import org.apache.tools.ant.BuildException;

public class DomainInventoryTask extends JmxCommandTask {
  
  public void execute() throws BuildException {
    DomainInventory d = new DomainInventory();
    super.setup(d);
    try {
      d.execute(this);
    } catch (ExecutionException ee) {
      if (getFailOnError()) {
        throw new BuildException(ee);
      }
      handleErrorOutput(ee.getMessage());
    }
  }
}
