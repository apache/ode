/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.ant;

import com.fs.pxe.tools.mngmt.SystemCommand;

public abstract class SystemTask extends JmxCommandTask {

  private String _systemName;

  public void setSystemname(String s) {
    _systemName = s;
  }
  
  public String getSystemName() {
    return _systemName;
  }
     
  public void setup(SystemCommand c) {
    super.setup(c);
    c.setSystemName(_systemName);
  }
}
