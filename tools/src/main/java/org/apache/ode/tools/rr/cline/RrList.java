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
import org.apache.ode.utils.rr.ResourceRepositoryException;
import org.apache.ode.utils.rr.URLResourceRepository;

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
