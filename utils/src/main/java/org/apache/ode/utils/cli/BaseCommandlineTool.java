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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.apache.ode.utils.Version;
import org.apache.ode.utils.fs.TempFileManager;
import org.apache.ode.utils.msg.MessageBundle;

import java.io.IOException;
import java.io.PrintWriter;

public abstract class BaseCommandlineTool {

  public static final int TERSE = 0;

  public static final int VERBOSE = 1;

  public static final int EFFUSIVE = 2;

  private static boolean _tempFileManagerRegistered = false;

  private static Class CLAZZ = BaseCommandlineTool.class;

  private static final String LOGGING_PATTERN = "%p - %d{ISO8601} - <%c> %m%n";

  protected static final Flag HELP_FLAG = new Flag("h",
      "print usage and help to the console and exit.", false);

  protected static final Fragments HELP = new Fragments(new CommandlineFragment[]{HELP_FLAG});

  protected static final Flag QUIET_F = new Flag("q",
      "only produce error output in the event of an error.", false);
  protected static final Flag VERBOSE_F = new Flag("v",
      "produce verbose (INFO-level) logging output.", false);
  protected static final Flag VERYVERBOSE_F = new Flag("vv",
      "product effusive (DEBUG-level) logging output", false);

  protected static final XorGroup LOGGING = new XorGroup(
      "set logging output verbosity from quiet (-q), to verbose (-v), to effusive (-vv).",
      true);
  static {
    LOGGING.addFragment(QUIET_F);
    LOGGING.addFragment(VERBOSE_F);
    LOGGING.addFragment(VERYVERBOSE_F);
  }

  private static final CommandLineMessages __msgs = MessageBundle.getMessages(CommandLineMessages.class);

  /**
   * Print program banner.
   */
  public static void outputHeader() {
    if (QUIET_F.isSet()) {
      return;
    }
    System.out.println(__msgs.msgCliHeader(getProgramName(), Version.getVersionName(), Version
        .getBuildDate()));
  }

  /**
   * Initialize logging appropriate for command-line utilities. The logging will
   * be limited to error messages on standard error, unless user-specified
   * logging options are present. Among other things, this method looks for the
   * <code>-v</code> option and configures logging verbosity appropriately
   *
   * @param level
   */
  protected static void initLogging(int level) {
    ConsoleAppender appender = new ConsoleAppender(new SimpleLayout());
    appender.setName("stderr appender");
    appender.setWriter(new PrintWriter(System.err));
    appender.setLayout(new PatternLayout(LOGGING_PATTERN));
    initialize(appender, level);
    BasicConfigurator.configure(appender);
    Logger.getRootLogger().addAppender(appender);
  }

  protected static void initLogFile() throws IOException {
    initLogFile(getLevel());
  }

  protected static void initLogFile(int level) throws IOException {
    FileAppender appender = new FileAppender(new PatternLayout(LOGGING_PATTERN),
        System.getProperty("ode.home") + "/logs/ode.log");
    appender.setName("file appender");
    initialize(appender, level);
    BasicConfigurator.configure(appender);
    Logger.getRootLogger().addAppender(appender);
  }

  private static void initialize(AppenderSkeleton appender, int level) {
    switch (level) {
      case EFFUSIVE :
        appender.setThreshold(Level.DEBUG);
        break;
      case VERBOSE :
        appender.setThreshold(Level.INFO);
        break;
      default :
        appender.setThreshold(Level.WARN);
    }
  }

  protected static void setClazz(Class c) {
    CLAZZ = c;
  }

  protected static String getProgramName() {
    return "java " + CLAZZ.getName();
  }

  protected static void initLogging() {
    initLogging(getLevel());
  }

  private static int getLevel() {
    if (QUIET_F.isSet()) {
      return TERSE;
    }
    else if (VERBOSE_F.isSet()) {
      return VERBOSE;
    }
    else if (VERYVERBOSE_F.isSet()) {
      return EFFUSIVE;
    }
    else {
      // none of the above.
      return -1;
    }
  }

  protected synchronized static final void registerTempFileManager() {
    if (!_tempFileManagerRegistered) {
      Runtime.getRuntime().addShutdownHook(new Thread() {

        public void run() {
          TempFileManager.cleanup();
        }
      });
    }
  }

  protected static void consoleErr(String errMsg) {
    String progName = getProgramName();
    System.err.println(progName + ": " + errMsg);
  }
}
