/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.cline;

import com.fs.pxe.tools.mngmt.AllSystemsCommand;
import com.fs.pxe.tools.mngmt.SystemCommand;
import com.fs.pxe.tools.mngmt.Undeploy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UndeployTool extends BaseJmxSystemTool{ 

  private static final Log __log = LogFactory.getLog(UndeployTool.class);
  private static final String SYNOPSIS = "undeploy one or more PXE systems from a PXE domain.";
  
  private UndeployTool() {
    super(SYNOPSIS,UndeployTool.class,new CommandFactory() {
      public AllSystemsCommand newAllCommand() {
        AllSystemsCommand asc = new AllSystemsCommand();
        asc.setOperation(AllSystemsCommand.UNDEPLOY);
        return asc;
      }
      public SystemCommand newCommand() {
        return new Undeploy();
      }
    },__log);
  }
  
  public static void main(String[] argv) {
    UndeployTool ut = new UndeployTool();
    System.exit(ut.run(argv));
  }
  
}

