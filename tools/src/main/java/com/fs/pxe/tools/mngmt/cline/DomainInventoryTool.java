/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.cline;

import com.fs.pxe.tools.ClineCommandContext;
import com.fs.pxe.tools.ExecutionException;
import com.fs.pxe.tools.mngmt.DomainInventory;
import com.fs.utils.cli.CommandlineFragment;
import com.fs.utils.cli.ConsoleFormatter;
import com.fs.utils.cli.Fragments;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DomainInventoryTool extends BaseJmxTool {
  
  private static final Log __log = LogFactory.getLog(DomainInventoryTool.class);
  
  private static final String SYNOPSIS =
    "enumerate the systems (and their services) deployed in a PXE domain.";
  
  private static final Fragments DEFAULT = new Fragments(new CommandlineFragment[] {
     LOGGING, JMX_URL_F, JMX_USERNAME_F, JMX_PASSWORD_F, DOMAIN_F 
  });
  
  public static void main(String[] args) {
    setClazz(DomainInventoryTool.class);
    if (HELP.matches(args)) {
      ConsoleFormatter.printSynopsis(
          getProgramName(),SYNOPSIS,
          new Fragments[] {DEFAULT, HELP});
      System.exit(0);
    } if (DEFAULT.matches(args)) {
      initLogging();
      DomainInventory di = new DomainInventory();
      if (VERBOSE_F.isSet() || VERYVERBOSE_F.isSet()) {
        di.setVerbosity(DomainInventory.VERBOSE);
      } else if (QUIET_F.isSet()) {
        di.setVerbosity(DomainInventory.TERSE);
      }
      processJmxUrl(di);
      processJmxUsername(di);
      processJmxPassword(di);
      processDomain(di);
      try {
        di.execute(new ClineCommandContext(__log));
        System.exit(0);
      } catch (ExecutionException ee) {
        consoleErr(ee.getMessage());
        System.exit(-1);
      }
    } else {
      consoleErr(DEFAULT.getReason().getMessage());
      System.exit(-1);
    }
  }
}
