/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.rr.cline;

import com.fs.pxe.sfwk.rr.ResourceRepositoryBuilder;
import com.fs.utils.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Command-line tool for adding aliases to a resource repository.
 */
public class RrAlias extends BaseCommandlineTool {

  private static final Argument ARG_RRDIR = new LastArgument("rr-dir",
      "destination resource repository",false);
  private static final Argument EXISTING_A = new Argument("resource",
      "URI of an existing repository resource",false);
  private static final Argument NEW_A = new Argument("alias",
      "new alias URI",false);
  
  private static final Fragments CLINE = new Fragments(new CommandlineFragment[] {
    LOGGING, ARG_RRDIR, EXISTING_A, NEW_A
  });
  
  private static final String SYNOPSIS = "add an alias from an existing URI to a new URI.";
  
  public static void main(String[] args) {
    registerTempFileManager();
    
    if (args.length == 0 || HELP.matches(args)) {
      ConsoleFormatter.printSynopsis(getProgramName(),SYNOPSIS, new Fragments[] {
        CLINE, HELP
      });
      System.exit(0);
    } else if (!CLINE.matches(args)) {
      consoleErr("INVALID COMMANDLINE: Try \"" + getProgramName() + " -h\" for help.");
      System.exit(-1);
    }

    
    initLogging();

    URI resource;
    URI alias;
    try {
      resource = new URI(EXISTING_A.getValue());
    } catch (URISyntaxException e) {
      consoleErr("Malformed URI " + EXISTING_A.getValue());
      System.exit(-2);
      return;
    }

    try {
      alias = new URI(NEW_A.getValue());
    } catch (URISyntaxException e) {
      consoleErr("Malformed URI " + EXISTING_A.getValue());
      System.exit(-2);
      return;
    }

    ResourceRepositoryBuilder rr = null;
    File rrdir = new File(ARG_RRDIR.getValue());
    rrdir.mkdirs();
    try {
      rr = new ResourceRepositoryBuilder(rrdir);
    } catch (FileNotFoundException fnf) {
      consoleErr("Resource repository not found.");
      System.exit(-2);
    } catch (IOException e) {
      consoleErr("Error reading resource repository.");
      System.exit(-2);
    }

    if (!rr.containsResource(resource)) {
      consoleErr("The resource " + resource + " is not in the repository.");
      System.exit(-3);
    }

    if (rr.containsResource(alias) && !QUIET_F.isSet()) {
      consoleErr("The resource " + NEW_A.getValue() +
          " is already bound in the repository and will be overwritten");
    }

    try {
      rr.addAlias(alias, resource);
    } catch (IOException ex) {
      consoleErr("The resource " + resource + " is not in the repository.");
    }
    System.exit(0);
  }
}
