/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.rr.cline;

import com.fs.pxe.sfwk.rr.ResourceRepositoryBuilder;
import com.fs.utils.cli.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Command line tool for removing resources from a resource repository.
 */
public class RrDrop extends BaseCommandlineTool {

  private static final Argument ZIPRR_LA = new Argument("rr-dir",
      "resource repository directory",false); 
  
  private static final MultiArgument RESOURCES_MA = new MultiArgument("uri",
      "URI(s) of resource(s) to remove",false);
  
  private static final Fragments CLINE = new Fragments(new CommandlineFragment[] {
      LOGGING, ZIPRR_LA, RESOURCES_MA
  });
  
  private static final String SYNOPSIS =
    "remove one or more URI references from a resource repository.  ";
  
  public static void main(String[] args) {
    registerTempFileManager();
    if (args.length == 0 || HELP.matches(args)) {
      ConsoleFormatter.printSynopsis(getProgramName(),SYNOPSIS,new Fragments[] {
        CLINE, HELP
      });
      System.exit(-1);
    }

    initLogging();

    URI[] uris = new URI[RESOURCES_MA.getValues().length];
    for (int i = 0; i < uris.length; ++i)
    	try {
    		uris[i] = new URI(RESOURCES_MA.getValues()[i]);
    	} catch (URISyntaxException use) {
        consoleErr("Malformed URI " + RESOURCES_MA.getValues()[i]);
    		System.exit(-2);
    	}

    boolean quiet = QUIET_F.isSet();
    File rrdir = new File(ZIPRR_LA.getValue());
    if (!rrdir.exists()) {
      consoleErr("The resource repository " + rrdir + " does not exist.");
      System.exit(-3);
    }
    
    ResourceRepositoryBuilder rr;
    try {
      rr = new ResourceRepositoryBuilder(rrdir);
    } catch (IOException zre) {
      consoleErr("Error reading resource repository " + rrdir);
      System.exit(-2);
      return;
    }
    
    for (int i=0; i<uris.length; ++i) {
      if (!rr.containsResource(uris[i])) {
        if (!quiet) {
          consoleErr(uris[i] + " is not in the repository.");
        }
      } else {
      	try {
          rr.removeURI(uris[i]);
      	} catch (IOException ioex) {
          consoleErr("Error removing URI " + uris[i] + " from resource repository.");
          System.exit(-2);
          return;
      		
      	}
      }
    }
    System.exit(0);
  }
}
