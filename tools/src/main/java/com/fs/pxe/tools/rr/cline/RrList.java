/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.rr.cline;

import com.fs.pxe.sfwk.rr.ResourceRepositoryException;
import com.fs.pxe.sfwk.rr.URLResourceRepository;
import com.fs.utils.cli.*;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;


/**
 * Command-line utility for listing resources in a resource repository
 * directory.
 */
public class RrList extends BaseCommandlineTool {

  private static final Argument ARG_RR = new Argument(
      "rr-dir","resource repository directory",false);
  
  private static final Fragments CLINE = new Fragments(new CommandlineFragment[] {
     LOGGING, ARG_RR 
  });
  
  private static final String SYNOPSIS = "enumerate the contents of a ZIPRR.";
  
  public static void main(String[] args) {
    registerTempFileManager();
    if (args.length == 0 || HELP.matches(args)) {
      ConsoleFormatter.printSynopsis(getProgramName(),SYNOPSIS,new Fragments[] {
        CLINE, HELP
      });
      System.exit(0);
    } else if (!CLINE.matches(args)) {
      consoleErr("INVALID COMMANDLINE: Try \"" + getProgramName() + " -h\" for help.");
      System.exit(-1);
    }

    String rrf = ARG_RR.getValue();
    initLogging();
    URLResourceRepository rr;
    try {
        rr = new URLResourceRepository(new File(rrf).toURI());
    } catch (ResourceRepositoryException zre) {
      consoleErr(zre.getMessage());
      System.exit(-2);
      throw new IllegalStateException();
    }
    Map<URI, String> resources = rr.getTableOfContents();
    System.out.println( resources.size()+ " resource" + (resources.size()==1?"":"s") +
    		" in " + rr.getBaseURL());
    for (Iterator<Map.Entry<URI,String>> i = resources.entrySet().iterator();i.hasNext();) {
    	Map.Entry<URI,String> me = i.next();
    	System.out.print("  " + me.getKey() + " --> ");
    	System.out.println(me.getValue());
    }
    System.exit(0);
  }

}
