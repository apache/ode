/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.dd.cline;

import com.fs.pxe.sfwk.deployment.som.sax.SystemDescriptorFactory;
import com.fs.pxe.sfwk.rr.ResourceRepository;
import com.fs.pxe.sfwk.rr.URLResourceRepository;
import com.fs.pxe.tools.ClineCommandContext;
import com.fs.pxe.tools.CommandContextErrorHandler;
import com.fs.utils.cli.*;
import com.fs.utils.sax.FailOnErrorErrorHandler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public class DdValidate extends BaseCommandlineTool {

  private static final Log __log = LogFactory.getLog(DdValidate.class);
  
  private static final Argument DESCRIPTOR_FILE = new Argument("descriptor",
      "the path to the XML system descriptor.",false);
  
  private static final Argument RR_FILE = new Argument("rrdir",
      "the system resource repository.",true);
  
  private static Fragments DEFAULT = new Fragments(new CommandlineFragment[] {
      LOGGING,DESCRIPTOR_FILE,RR_FILE
  });
  
  private static final String SYNOPSIS = "validate a PXE system deployment descriptor " +
    "for schema correctness and (optionally) for WSDL portType references.";
  
  public static void main(String[] args) {
    setClazz(DdValidate.class);    
    if (HELP.matches(args)) {
      ConsoleFormatter.printSynopsis(getProgramName(),SYNOPSIS,new Fragments[] {
          DEFAULT,HELP});
      System.exit(0);
    }
    if (!DEFAULT.matches(args)) {
      System.err.println("INVALID COMMANDLINE: " + DEFAULT.getReason().getMessage());
      System.exit(-1);
    }
    
    initLogging();
    registerTempFileManager();
    boolean quiet = QUIET_F.isSet();
    String sysd = DESCRIPTOR_FILE.getValue();
    
    ResourceRepository rr = null;
    if (RR_FILE.isSet()) {
      String rrfile = RR_FILE.getValue();
      File rrf = new File(rrfile);
      if (!rrf.exists()) {
        consoleErr(rrfile + " does not exist.");
        System.exit(-1);
      } else if (!rrf.isDirectory()) {
        consoleErr(rrfile + " is not a directory.");
        System.exit(-1);
      }
      try {
        rr = new URLResourceRepository(rrf.toURI());
      } catch (Exception ioe) {
        consoleErr(rrfile + " does not appear to be a resource repository: " + ioe.getMessage());
        System.exit(-1);
      }
    }
    
    ErrorHandler eh = quiet?
        ((ErrorHandler) new FailOnErrorErrorHandler()):
          ((ErrorHandler) new CommandContextErrorHandler(
              new ClineCommandContext(__log)));
    
    try {
      SystemDescriptorFactory.parseDescriptor(new File(sysd).toURI().toURL(),
          eh,rr,
          true);
    } catch (SAXException se) {
      consoleErr("The descriptor is not valid: " + se.getMessage());      
      System.exit(-1);
    } catch (IOException ioe) {
      consoleErr(
          "An error occurred while reading a resource: " + ioe.getMessage());
      System.exit(-1);
    }
    if (eh instanceof CommandContextErrorHandler) {
      if (((CommandContextErrorHandler)eh).hadError()) {
        consoleErr(
            "The descriptor is not valid; additional information should have been " +
            "provided.");
        System.exit(-1);
      }
    }
    if (!quiet) {
      if (rr == null) {
        consoleErr("The descriptor is schema valid; portType references were not checked.");
      } else {
        consoleErr("The descriptor is schema valid, and the portType references were checked.");
      }
    }
    System.exit(0);    
  }
}
