/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.bpelc.ant;

import com.fs.pxe.tools.CommandTask;
import com.fs.pxe.tools.ExecutionException;
import com.fs.pxe.tools.bpelc.BpelCompileCommand;

import java.io.File;

import org.apache.tools.ant.BuildException;

public class BpelCTask extends CommandTask {
  
  BpelCompileCommand _bcc = new BpelCompileCommand();
  
  public void setRr(File f) {
    _bcc.setResourceRepository(f);
  }
  
  public void setTargetdir(File f) {
    _bcc.setOuputDirectory(f);
  }
  
  public void addConfiguredBpel(BpelSrcElement bse) {
    if (bse.getUrl() == null || bse.getUrl().trim().length() == 0) {
      throw new BuildException("The url attribute is required.");
    }
    _bcc.addBpelProcessUrl(bse.getUrl());
  }

  public void setWsdl(String uri) throws BuildException {
    _bcc.setWsdlImportUri(uri);
  }
  
  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException {
    try {
      _bcc.execute(this);
    } catch (ExecutionException ee) {
      throw new BuildException(ee.getMessage(),ee);
    }
  }
}
