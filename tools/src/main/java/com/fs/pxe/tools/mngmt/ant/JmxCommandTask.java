/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.ant;

import com.fs.pxe.tools.CommandTask;
import com.fs.pxe.tools.mngmt.JmxCommand;

public abstract class JmxCommandTask extends CommandTask {
  
  private String _jmxUrl;
  private String _jmxUsername;
  private String _jmxPassword;
  private String _domain;
  private boolean _failOnError = true;
  
  public void setJmxurl(String s) {
    _jmxUrl = s;
  }
  
  public void setJmxusername(String s) {
    _jmxUsername = s;
  }

  public void setJmxpassword(String s) {
    _jmxPassword = s;
  }
  
  public void setDomain(String s) {
    _domain = s;
  }
  
  public String getJmxurl() {
    return _jmxUrl;
  }
  
  public String getJmxusername() {
    return _jmxUsername;
  }
  
  public String getJmxpassword() {
    return _jmxPassword;
  }
  
  public String getDomain() {
    return _domain;
  }
  
  public void setFailOnError(boolean f) {
    _failOnError = f;
  }
  
  public boolean getFailOnError() {
    return _failOnError;
  }

  public void setup(JmxCommand c) {
    c.setJmxUrl(getJmxurl());
    c.setJmxUsername(getJmxusername());
    c.setJmxPassword(getJmxpassword());
    c.setDomainUuid(_domain);
  }
}
