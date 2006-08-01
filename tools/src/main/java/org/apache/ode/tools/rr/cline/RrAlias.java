/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.     
 */
package org.apache.ode.tools.rr.cline;

import org.apache.ode.utils.cli.*;
import org.apache.ode.utils.rr.ResourceRepositoryBuilder;

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
