/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.cline;

import com.fs.pxe.tools.mngmt.JmxCommand;
import com.fs.utils.cli.BaseCommandlineTool;
import com.fs.utils.cli.FlagWithArgument;


abstract class BaseJmxTool extends BaseCommandlineTool {
  
  protected static final FlagWithArgument JMX_URL_F = new FlagWithArgument("jmxurl","url",
      "JMX service URL (JSR-160) for connecting to the server.",true);
  
  protected static final FlagWithArgument JMX_USERNAME_F = new FlagWithArgument("jmxusername","username",
      "JMX Username (JSR-160) for connecting to the server.",true);
  
  protected static final FlagWithArgument JMX_PASSWORD_F = new FlagWithArgument("jmxpassword","password",
      "JMX Password (JSR-160) for connecting to the server.",true);
  
  protected static final FlagWithArgument DOMAIN_F = new FlagWithArgument("domain", 
      "dname","the name of the PXE domain to connect to.",true);
  
  protected static void processDomain(JmxCommand c) {
    if (DOMAIN_F.isSet()) {
      c.setDomainUuid(DOMAIN_F.getValue());
    }
  }
  
  protected static void processJmxUrl(JmxCommand c) {
    if (JMX_URL_F.isSet()) {
      c.setJmxUrl(JMX_URL_F.getValue());
    }
  }
  
  protected static void processJmxUsername(JmxCommand c) {
    if (JMX_USERNAME_F.isSet()) {
      c.setJmxUsername(JMX_USERNAME_F.getValue());
    }
  }
  
  protected static void processJmxPassword(JmxCommand c) {
    if (JMX_PASSWORD_F.isSet()) {
      c.setJmxPassword(JMX_PASSWORD_F.getValue());
    }
  }  
}
