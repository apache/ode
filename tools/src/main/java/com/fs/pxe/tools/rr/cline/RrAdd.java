/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.rr.cline;

import com.fs.pxe.sfwk.rr.ResourceRepositoryBuilder;
import com.fs.utils.cli.*;
import com.fs.utils.xml.capture.XmlDependencyScanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;


/**
 * Command-line tool for adding resoruces to resource repository.
 * @see {@link com.fs.pxe.sfwk.rr.ResourceRepository}
 * @see {@link com.fs.pxe.sfwk.rr.URLResourceRepository}
 * @see {@link ResourceRepositoryBuilder}
 */
public class RrAdd extends BaseCommandlineTool {

  private static final Flag ARG_RECURSE = new Flag("r","add recursively", true);
  private static final FlagWithArgument ARG_BASEDIR = new FlagWithArgument("src","src-uri", "source URI (for resolving realtive URIs) ", true);
  private static final FlagWithArgument ARG_ALIAS = new FlagWithArgument("dest", "dest-uri", "destination URI", true);
  private static final Argument ARG_RRDIR = new Argument("rr-dir", "destination resource repository directory" ,false);
  private static final MultiArgument ARG_SRC = new MultiArgument("resource", "resource URI(s)", false);
  private static final Fragments CLINE = new Fragments(new CommandlineFragment[] {
      LOGGING, ARG_RECURSE, ARG_BASEDIR, ARG_ALIAS,  ARG_RRDIR, ARG_SRC
  });
  
  private static final String SYNOPSIS = "add resources to a resource repository.";
  
  public static void main(String[] args) {
  	setClazz(RrAdd.class);
    if (args.length == 0 || HELP.matches(args)) {
      ConsoleFormatter.printSynopsis(getProgramName(),SYNOPSIS, new Fragments[] {
        CLINE, HELP
      });
      System.exit(0);
    } else if (!CLINE.matches(args)) {
      consoleErr("INVALID COMMANDLINE: Try \"" + getProgramName() + " -h\" for help.");
      System.exit(-1);
    }
    registerTempFileManager();
    initLogging();
    boolean quiet = QUIET_F.isSet();
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

    URI srcRoot,destRoot;
    try {
      srcRoot = ARG_BASEDIR.getValue() == null ? new File("./").toURI() : new URI(ARG_BASEDIR.getValue());
    } catch (URISyntaxException e) {
      consoleErr("Malformed source URI: " + ARG_BASEDIR.getValue());
      System.exit(-2);
      return;
    }

    if (!srcRoot.isAbsolute()) {
      consoleErr("Source URI must be absolute: " + srcRoot);
      System.exit(-2);
      return;
    }

    try {
      destRoot = ARG_ALIAS.getValue() == null ? null :  new URI(ARG_ALIAS.getValue());
    } catch (URISyntaxException e) {
      consoleErr("Malformed destination URI: " + ARG_ALIAS.getValue());
      System.exit(-2);
      return;
    }

    URI original[] = new URI[ARG_SRC.getValues().length];
    URI source[] = new URI[original.length];
    URI dest[] = new URI[original.length];
    URL urls[] = new URL[original.length];
    if (destRoot != null && srcRoot == null) {
      consoleErr("A source URI must be specified when a destination URI is given.");
      System.exit(-2);
      return;
    }

    for (int i = 0; i < original.length; ++i) {
      try {
        original[i] = new URI(ARG_SRC.getValues()[i]);
      } catch (URISyntaxException use) {
        consoleErr("Malformed resource URI: " + ARG_SRC.getValues()[i]);
        System.exit(-2);
        return;
      }

      URI relative = srcRoot.relativize(original[i]);
      source[i] = srcRoot.resolve(original[i]);
      dest[i] = destRoot == null ? source[i] : destRoot.resolve(relative);


      try {
        urls[i] = source[i].toURL();
      } catch (MalformedURLException e) {
        consoleErr("Unrecognized URI: " + source[i]);
        System.exit(-2);
        return;
      }
    }


    for (int i = 0; i < source.length; ++i) {
      if (ARG_RECURSE.isSet()) {
        addRecursive(rr,srcRoot,destRoot, source[i]);
      } else {

        if (!quiet && rr.containsResource(dest[i]))
          consoleErr("Overwriting existing resource for " + dest[i]);
        try {
          rr.addURI(dest[i],urls[i]);
        } catch (IOException zre) {
          consoleErr("Error writing " + dest[i]
                  + " to the repository.");
          System.exit(-6);
        }
      }
    }

    System.exit(0);
  }


  private static void addRecursive(ResourceRepositoryBuilder rr, URI base, URI alias, URI target) {
    XmlDependencyScanner scanner = new XmlDependencyScanner();
    scanner.process(target);
    if (scanner.isError()) {
      Map.Entry<URI, Exception> e = scanner.getErrors().entrySet().iterator().next();
      consoleErr("Error scanning " + e.getKey() + " : "  + e.getValue());
      System.exit(-3);
      return;
    }

    for (Iterator<URI> i = scanner.getURIs().iterator();i.hasNext(); ) {
      URI uri = i.next();
      URI relative = base.relativize(uri);
      URI aliased = alias == null ? uri : alias.resolve(relative);
      try {
        rr.addURI(aliased,uri.toURL());
      } catch (IOException ioex) {
        consoleErr("Error reading/writing resource " + target);
        System.exit(-2);
        return;
      }
    }

  }
}
