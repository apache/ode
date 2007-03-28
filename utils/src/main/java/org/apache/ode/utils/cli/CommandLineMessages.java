/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.utils.cli;

import org.apache.ode.utils.SystemUtils;
import org.apache.ode.utils.msg.MessageBundle;

/**
 * Message bundle for {@link org.apache.ode.utils.cli.CliHelper}.
 */
public class CommandLineMessages extends MessageBundle {

  /**
   * Format a header for display by a commandline tool at startup.
   * 
   * @param cmd
   *          the name of the command or class with <code>main(...)</code>
   * @param version
   *          the full version name, e.g., <code>2.0B101</code>
   * @param date
   *          the build date
   * 
   * {0} - FiveSight ODE v{1} ({2})\nCopyright (c) 2003-2005 FiveSight
   * Technologies, Inc.
   */
  public String msgCliHeader(String cmd, String version, String date) {
    return this.format("{0} - Apache ODE v{1} ({2})" + SystemUtils.lineSeparator()
        + "Copyright (c) 2006-2007 Apache ODE.", cmd, version, date);
  }

  /**
   * Format the description of the commandline help option.
   * 
   * print detailed help information to the console
   */
  public String msgHelp() {
    return this.format("print detailed help information to the console");
  }

  /**
   * Format the description of the verbose option.
   * 
   * provide verbose logging information to the console (on standard error)
   */
  public String msgVerbose() {
    return this.format("provide verbose logging information to the console"
        + " (on standard error)");
  }

  /**
   * Format the description of the very verbose option.
   * 
   */
  public String msgVeryVerbose() {
    return this.format("provide very verbose logging information to the console"
        + " (on standard error)");
  }

  /**
   * Format the description of the version information option.
   * 
   * display version information and exit
   */
  public String msgVersion() {
    return this.format("display version information and exit");
  }

  /**
   * Format the description of the quiet option.
   * 
   * do not output the program name and version header to standard out at
   * startup
   */
  public String msgQuiet() {
    return this.format("do not output the program name and version header to"
        + "standard out at startup");
  }

  /**
   * Format an error message about an option missing a required argument.
   * 
   * @param opt
   *          the name of the option missing the argument
   * 
   * the option {0} requires an agument.
   */
  public String msgMissingArgument(String opt) {
    return this.format("the option {0} requires an agument.", opt);
  }

  /**
   * Format an error message about a missing required option.
   * 
   * @param opt
   *          the name of the the option
   * 
   * the option {0} is required.
   */
  public String msgMissingOption(String opt) {
    return this.format("the option {0} is required.", opt);
  }

  /**
   * Format an error message about an unrecognized commandline option.
   * 
   * @param opt
   *          the unrecognized option
   * 
   * the option {0} is not recognized.
   */
  public String msgUnrecognizedOption(String opt) {
    return this.format("the option {0} is not recognized.", opt);
  }

  /**
   * Format an error message about a repeated option.
   * 
   * @param opt
   *          the name of the option
   * 
   * the option {0} was already specified.
   */
  public String msgAlreadySelectedOption(String opt) {
    return this.format("the option {0} was already specified.", opt);
  }

  /**
   * Format a generic error about an unparseable commandline.
   * 
   * unable to parse commandline.
   */
  public String msgCommandLineError() {
    return this.format("unable to parse command line.");
  }

}
