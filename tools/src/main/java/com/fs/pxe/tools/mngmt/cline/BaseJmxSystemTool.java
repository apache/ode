/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt.cline;

import com.fs.pxe.tools.ClineCommandContext;
import com.fs.pxe.tools.ExecutionException;
import com.fs.pxe.tools.mngmt.AllSystemsCommand;
import com.fs.pxe.tools.mngmt.SystemCommand;
import com.fs.utils.cli.*;

import org.apache.commons.logging.Log;


abstract class BaseJmxSystemTool extends BaseJmxTool {

  protected static MultiArgument SYSTEM_A = new MultiArgument("system",
      "the names of systems to apply the command to.",false);
  protected static Flag ALL_F = new Flag("all",
      "apply the command to all systems in the domain.",false);
  
  protected static final Fragments SPECIFIC_CL = 
    new Fragments(new CommandlineFragment[] {
        LOGGING, JMX_URL_F, JMX_USERNAME_F, JMX_PASSWORD_F, DOMAIN_F, SYSTEM_A
    });
  
  protected static final Fragments ALL_SYS_CL = 
    new Fragments(new CommandlineFragment[] {
       LOGGING, JMX_URL_F, JMX_USERNAME_F, JMX_PASSWORD_F, DOMAIN_F, ALL_F 
    });  
  
  private Log _log;
  private CommandFactory _f;
  private String _synopsis;

  
  protected BaseJmxSystemTool(String synopsis, Class c, CommandFactory f, Log l) {
    setClazz(c);
    _synopsis = synopsis;
    _log = l;
    _f = f;
  }
  
  private void executeSome() throws ExecutionException {
    String[] s = SYSTEM_A.getValues();
    for (int i=0; i < s.length; ++i) {
      SystemCommand ss = _f.newCommand();
      processDomain(ss);
      processJmxUrl(ss);
      processJmxUsername(ss);
      processJmxPassword(ss);
      ss.setSystemName(s[i]);
      ss.execute(new ClineCommandContext(_log));
    }
  }
  
  private void executeAll() throws ExecutionException {
    AllSystemsCommand c = _f.newAllCommand();
    processDomain(c);
    processJmxUrl(c);
    processJmxUsername(c);
    processJmxPassword(c);
    c.execute(new ClineCommandContext(_log));
  }
  
  protected int run(String[] argv) {
    if (argv.length ==0 || HELP.matches(argv)) {
      ConsoleFormatter.printSynopsis(
          getProgramName(),_synopsis,
          new Fragments[] {SPECIFIC_CL, ALL_SYS_CL, HELP});
      return 0;
    }    
    
    registerTempFileManager();

    boolean some;
    if (SPECIFIC_CL.matches(argv)) {
      some = true;
    } else if (ALL_SYS_CL.matches(argv)) {
        some = false;
    } else {
      consoleErr("INVALID COMMANDLINE: Unable to match any possible argument formats.");
      return -1;
    }
    initLogging();
    try {
      if (some) {
        executeSome();
      } else {
        executeAll();
      }
    } catch (ExecutionException ee) {
      consoleErr(ee.getMessage());
      return -1;
    }
    return 0;
  }
    
  protected interface CommandFactory {
    SystemCommand newCommand();
    AllSystemsCommand newAllCommand();
  }
}
