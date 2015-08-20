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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.ode.utils.Version;
import org.apache.ode.utils.fs.TempFileManager;
import org.apache.ode.utils.msg.MessageBundle;

import java.io.IOException;
import java.nio.charset.Charset;

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
      final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      final Configuration config = ctx.getConfiguration();
      LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
      Layout layout = PatternLayout.createLayout(LOGGING_PATTERN,config,null,Charset.forName("UTF-8"),true,false,null,null);
      Appender appender = ConsoleAppender.createAppender(layout, null, "SYSTEM_OUT", "stderr appender", "false", "true");
      AppenderRef ref = initialize("stderr appender", level);
      AppenderRef[] refs = new AppenderRef[] {ref};
      loggerConfig.addAppender(appender, ref.getLevel(), null);
      loggerConfig.setLevel(ref.getLevel());
      ctx.updateLoggers();
  }

  protected static void initLogFile() throws IOException {
    initLogFile(getLevel());
  }

  protected static void initLogFile(int level) throws IOException {
      final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      final Configuration config = ctx.getConfiguration();
      LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
      Layout layout = PatternLayout.createLayout(LOGGING_PATTERN,config,null,Charset.forName("UTF-8"),true,false,null,null);
      Appender appender = FileAppender.createAppender(System.getProperty("ode.home") + "/logs/ode.log", "false", "false", "File", "false",
          "true", "true", "8192", layout, null, "false", null, config);
      AppenderRef ref = initialize("File", level);
      AppenderRef[] refs = new AppenderRef[] {ref};
      loggerConfig.addAppender(appender, ref.getLevel(), null);
      loggerConfig.setLevel(ref.getLevel());
      ctx.updateLoggers();
  }

  private static AppenderRef initialize(String name, int level) {
      Level appenderLevel = null;
      switch (level) {
          case EFFUSIVE :
              appenderLevel = Level.DEBUG;
          break;
          case VERBOSE :
              appenderLevel = Level.INFO;
          break;
          default :
              appenderLevel = Level.WARN;
          break;
    }
    return AppenderRef.createAppenderRef(name, appenderLevel, null);
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
