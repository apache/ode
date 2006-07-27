/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.tools.bpelc.cline;

import org.apache.ode.tools.ClineCommandContext;
import org.apache.ode.tools.ExecutionException;
import org.apache.ode.tools.bpelc.BpelCompileCommand;
import org.apache.ode.utils.cli.*;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BpelC extends BaseCommandlineTool {
  private static final Log __log = LogFactory.getLog(BpelC.class);
    
  private static FlagWithArgument RR_FILE = new FlagWithArgument("rr","rr-dir",
      "resource repository directory; " +
      "  if not supplied, URIs will be resolved as URLs",
      true);
  
  private static FlagWithArgument OUTPUT_DIR = new FlagWithArgument("od","directory",
      "output directory",true);
  
  private static FlagWithArgument ROOT_WSDL = new FlagWithArgument("wsdl","uri",
      "URI of the WSDL for the process (used with BPEL4WS 1.1 " + 
      "processes, for WS-BPEL 2.0 processes, use <import>).",true);

  private static MultiArgument BPEL_URLS = new MultiArgument("bpelurl",
      "the URLs of BPEL processes to compile.",false);

  private static Fragments DEFAULT = new Fragments(new CommandlineFragment[] {
      LOGGING, RR_FILE, ROOT_WSDL, OUTPUT_DIR, BPEL_URLS
  });
  
  private static final String SYNOPSIS = "compile one or more BPEL processes";
  
  public static void main(String[] args) {
    setClazz(BpelC.class);
    if (args.length ==0 || HELP.matches(args)) {
      ConsoleFormatter.printSynopsis(
          getProgramName(),SYNOPSIS,
          new Fragments[] {DEFAULT, HELP});
      System.exit(0);
    }
    if (DEFAULT.matches(args)) {
      // Do nothing; we just want the format enforced.
    } else {
      consoleErr("INVALID COMMANDLINE: Try \"" + getProgramName() + " -h\" for help.");
      System.exit(-1);
    }
    
    // We don't want the normal logging crap; just the coiler messages.
    initLogging();
    
    
    BpelCompileCommand bcc = new BpelCompileCommand();


    if (OUTPUT_DIR.isSet()) {
      String outputDir = OUTPUT_DIR.getValue();
      File od = new File(outputDir);
      if (!od.exists() || !od.isDirectory()) {
        consoleErr(outputDir + ": no such directory or not writable.");
        System.exit(-1);
      }
      bcc.setOuputDirectory(od);
    }
    
    if (RR_FILE.isSet()) {
      bcc.setResourceRepository(new File(RR_FILE.getValue()));
    }
    
    if (ROOT_WSDL.isSet()) {
      bcc.setWsdlImportUri(ROOT_WSDL.getValue());
    } 
    
    String[] b = BPEL_URLS.getValues();
    if (b == null || b.length == 0) {
      consoleErr("At least one process must be specified.");
      System.exit(-1);
    }
    for (int i=0; i < b.length; ++i) {
      bcc.addBpelProcessUrl(b[i]);
    }
    try {
      bcc.execute(new ClineCommandContext(__log));
    } catch (ExecutionException ee) {
      consoleErr(ee.getMessage());
      System.exit(-1);
    } catch (Throwable t) {
      // This is really quite unexpected, so we should
      // print the stack trace to stderr.
      consoleErr(t.getMessage());
      t.printStackTrace();
      System.exit(-2);
    }
    System.exit(0);
  }
}
