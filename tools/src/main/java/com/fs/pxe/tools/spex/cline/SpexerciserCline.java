/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.spex.cline;

import java.io.File;

import com.fs.pxe.tools.ClineCommandContext;
import com.fs.pxe.tools.ExecutionException;
import com.fs.pxe.tools.spex.Spexerciser;
import com.fs.utils.cli.*;


import org.apache.commons.logging.LogFactory;

public class SpexerciserCline extends BaseCommandlineTool {
  
  static final String TEST_DIR = "examples/spexerciser";

  private static final FlagWithArgument PXE_CONN_FWA = new FlagWithArgument(
      "pxeurl","url","PxeConnection URL to connect to the PXE runtime.",true);
  
  private static final FlagWithArgument JMX_SVC_FWA = new FlagWithArgument(
      "jmxurl","url","JMX service URL to connect to the PXE runtime.",true);

  protected static final FlagWithArgument JMX_USERNAME_F = new FlagWithArgument("jmxusername","username",
      "JMX Username (JSR-160) for connecting to the server.",true);
  
  protected static final FlagWithArgument JMX_PASSWORD_F = new FlagWithArgument("jmxpassword","password",
      "JMX Password (JSR-160) for connecting to the server.",true);

  private static final MultiArgument TESTS_MA = new MultiArgument("test",
      "then name or names of tests to be run, with the default being all.",true);
  
  private static final Fragments CLINE = new Fragments(new CommandlineFragment[] {
      LOGGING, PXE_CONN_FWA, JMX_SVC_FWA, TESTS_MA
  });
  
  private static final String SYNOPSIS =
    "run one or more tests against the BPEL runtime using synthetic endpoints " + 
    "deployed in a live PXE instance.";
  
	public static void main(String[] args) throws Exception {
    if (HELP.matches(args)) {
      ConsoleFormatter.printSynopsis(getProgramName(),SYNOPSIS,new Fragments[] {
        HELP, CLINE});
      System.exit(0);
    } else if (!CLINE.matches(args) && args.length != 0) {
      consoleErr("INVALID COMMANDLINE: Try \"" + getProgramName() + " -h\" for help.");
      System.exit(-1);
    }
    initLogging();

    registerTempFileManager();

    Spexerciser spex = new Spexerciser();
    
    if (QUIET_F.isSet()) {
      spex.setQuiet(true);
    }
    
    if (VERBOSE_F.isSet() || VERYVERBOSE_F.isSet()) {
      spex.setVerbose(true);
    }
    if(JMX_SVC_FWA.isSet()) {
    	  spex.setJmxUrl(JMX_SVC_FWA.getValue());
    }
    if(JMX_USERNAME_F.isSet()) {
    	  spex.setJmxUsername(JMX_USERNAME_F.getValue());
    }
    if(JMX_PASSWORD_F.isSet()) {
    	  spex.setJmxPassword(JMX_PASSWORD_F.getValue());
    }
    if(PXE_CONN_FWA.isSet()){
      spex.setPxeConnectionUrl(PXE_CONN_FWA.getValue());
    }
    
    String basedir = System.getProperty("pxe.home");
    String testdir = System.getProperty("pxe.spex.home");
    String[] testArgs = TESTS_MA.getValues();
    
    if (testArgs != null) {
      for(int i = 0; i < testArgs.length; ++i) {
        spex.addTest(new File(testArgs[i]));
      }
    } else if (testdir != null) {
      spex.addTest(new File(testdir));
    } else if (basedir != null) {
      spex.addTest(new File(new File(basedir),TEST_DIR));
    }

    try {
      spex.execute(new ClineCommandContext(LogFactory.getLog(SpexerciserCline.class)));
    } catch (ExecutionException ee) {
      consoleErr(ee.getMessage());
      System.exit(-1);
    }
    
    System.exit(0);
  }
}
