/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.cline;

import com.fs.pxe.tools.mngmt.Activate;
import com.fs.pxe.tools.mngmt.AllSystemsCommand;
import com.fs.pxe.tools.mngmt.SystemCommand;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ActivateTool extends BaseJmxSystemTool{ 

  private static final Log __log = LogFactory.getLog(ActivateTool.class);
  private static final String SYNOPSIS = "activate one or more PXE systems in a PXE domain.";
  
  private ActivateTool() {
    super(SYNOPSIS,ActivateTool.class,new CommandFactory() {
      public AllSystemsCommand newAllCommand() {
        AllSystemsCommand asc = new AllSystemsCommand();
        asc.setOperation(AllSystemsCommand.ACTIVATE);
        return asc;
      }
      public SystemCommand newCommand() {
        return new Activate();
      }
    },__log);
  }
  
  public static void main(String[] argv) {
    ActivateTool ut = new ActivateTool();
    System.exit(ut.run(argv));
  }
  
}

