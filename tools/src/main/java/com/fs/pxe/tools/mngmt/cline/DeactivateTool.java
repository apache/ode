/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.cline;

import com.fs.pxe.tools.mngmt.AllSystemsCommand;
import com.fs.pxe.tools.mngmt.Deactivate;
import com.fs.pxe.tools.mngmt.SystemCommand;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DeactivateTool extends BaseJmxSystemTool{ 

  private static final Log __log = LogFactory.getLog(DeactivateTool.class);
  private static final String SYNOPSIS = "activate one or more PXE systems in a PXE domain.";
  
  private DeactivateTool() {
    super(SYNOPSIS,DeactivateTool.class,new CommandFactory() {
      public AllSystemsCommand newAllCommand() {
        AllSystemsCommand asc = new AllSystemsCommand();
        asc.setOperation(AllSystemsCommand.DEACTIVATE);
        return asc;
      }
      public SystemCommand newCommand() {
        return new Deactivate();
      }
    },__log);
  }
  
  public static void main(String[] argv) {
    DeactivateTool ut = new DeactivateTool();
    System.exit(ut.run(argv));
  }
  
}

