/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.cline;

import com.fs.pxe.tools.ClineCommandContext;
import com.fs.pxe.tools.ExecutionException;
import com.fs.pxe.tools.mngmt.Deploy;
import com.fs.utils.cli.CommandlineFragment;
import com.fs.utils.cli.ConsoleFormatter;
import com.fs.utils.cli.FlagWithArgument;
import com.fs.utils.cli.Fragments;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeployTool extends BaseJmxTool {
  private static final Log __log = LogFactory.getLog(DeployTool.class);
  
  private static final String SYNOPSIS =
    "enumerate the systems (and their services) deployed in a PXE domain.";
  
  private static final FlagWithArgument SAR_F = new FlagWithArgument("sar","sarfile",
      "the SAR that contains the system to deploy",false);
  
  private static final FlagWithArgument URL_F = new FlagWithArgument("url","sarurl",
      "the URL that contains the SAR to deploy",false);
  
  private static final Fragments SARSTYLE = new Fragments(new CommandlineFragment[] {
      LOGGING, JMX_URL_F, JMX_USERNAME_F, JMX_PASSWORD_F, DOMAIN_F, SAR_F
   });

  private static final Fragments URLSTYLE = new Fragments(new CommandlineFragment[] {
      LOGGING, JMX_URL_F, JMX_USERNAME_F, JMX_PASSWORD_F, DOMAIN_F, URL_F
   });

  
  public static void main(String[] args) {
    setClazz(DeployTool.class);
    if (args.length ==0 || HELP.matches(args)) {
      ConsoleFormatter.printSynopsis(
          getProgramName(),SYNOPSIS,
          new Fragments[] {SARSTYLE,URLSTYLE, HELP});
      System.exit(0);
    }
    if (SARSTYLE.matches(args) || URLSTYLE.matches(args)) {
      registerTempFileManager();
      initLogging();
      Deploy d = new Deploy();
      processJmxUrl(d);
      processJmxUsername(d);
      processJmxPassword(d);
      processDomain(d);
      if (SAR_F.isSet()) {
        d.setSarFile(SAR_F.getValue());
      } else {
        d.setSarUrl(URL_F.getValue());
      }
      try {
        d.execute(new ClineCommandContext(__log));
        System.exit(0);
      } catch (ExecutionException ee) {
        ee.printStackTrace();
        consoleErr(ee.getMessage());
        System.exit(-1);
      }
    } else {
      consoleErr("INVALID COMMANDLINE: Try \"" + getProgramName() + " -h\" for help.");
      System.exit(-1);
    }
  }
}
