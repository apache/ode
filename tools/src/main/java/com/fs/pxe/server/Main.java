/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.server;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fs.pxe.kernel.PxeKernel;
import com.fs.utils.cli.BaseCommandlineTool;
import com.fs.utils.cli.CommandlineFragment;
import com.fs.utils.cli.ConsoleFormatter;
import com.fs.utils.cli.Flag;
import com.fs.utils.cli.FlagWithArgument;
import com.fs.utils.cli.Fragments;
import com.fs.utils.fs.TempFileManager;


public class Main extends BaseCommandlineTool {
  
  private static final Log __log = LogFactory.getLog(Main.class);
  
  private static final Flag CONSOLE_F = new Flag("console",
      "direct logging output to the console (instead of the log file)",true);
  
  private static final FlagWithArgument CONFIG_URL_A = new FlagWithArgument("config",
      "url","URL of the PXE kernel configuration to run.",true);

  private static final Fragments CLINE = new Fragments(new CommandlineFragment[] {
      LOGGING, CONSOLE_F, CONFIG_URL_A
  });
  
  private static final Fragments[] FRAGS = new Fragments[] { CLINE, HELP };
  
  private static final String SYNOPSIS = "launch a configuration of the PXE kernel.";

  private static final ShutdownHook HOOK = new ShutdownHook();

  public static void main(String[] argv) throws Exception {
		if (HELP.matches(argv)) {
      ConsoleFormatter.printSynopsis(getProgramName(),SYNOPSIS,FRAGS);
      System.exit(0);
    } else if (argv.length != 0 && !CLINE.matches(argv)) {
      consoleErr("INVALID COMMANDLINE: Try \"" + getProgramName() + " -h\" for help.");
      System.exit(-1);
    }
		
    if (!QUIET_F.isSet()) {
      BaseCommandlineTool.outputHeader();
    }
    
    if (CONSOLE_F.isSet()) {
      initLogging();
    } else {
      initLogFile();
    }

    // register shutdown hook 'container'
    try {
      HOOK.addAction(Main.getTempFilemanagerShutdownAction());
      Runtime.getRuntime().addShutdownHook(HOOK);
    } catch (Throwable t) {
      __log.error("Unable to register shutdown hook: ", t);
    }
    
    String config;
    if (CONFIG_URL_A.isSet()) {
      config = CONFIG_URL_A.getValue();
    } else {
      File f = new File(System.getProperty("fivesight.bootstrap.BootLoader.baseDir"), "etc/pxe-config.xml");
      config = f.toURI().toURL().toExternalForm();
    }

    try {
      __log.info("Creating PXE Kernel Runtime Context");
      PxeKernelRuntimeContext kernelRuntimeContext =
        new PxeKernelRuntimeContext(
          ManagementFactory.getPlatformMBeanServer(),
          new URL(config));
      HOOK.addAction(kernelRuntimeContext);
      if(!kernelRuntimeContext.start()) {
        __log.fatal("Failure starting PxeKernelRuntimeContext...Shutting Down");
        System.exit(-1); // will call shutdown hook
      } 
    } catch (MalformedURLException mue) {
      consoleErr("The configuration URL " + config + " is malformed.");
      System.exit(-1);
    }

	}

  private static Runnable getTempFilemanagerShutdownAction() {
    return new Runnable() {
      public void run() {
        TempFileManager.cleanup();
      }
    };
  }

  private static class ShutdownHook extends Thread {
    private List<Runnable> _actions = new ArrayList<Runnable>();

    public ShutdownHook() {
      super("PXE VM shutdown hook");
    }

    public synchronized void addAction(Runnable r) {
      _actions.add(r);
    }

    public void run() {
      synchronized(_actions) {
        // perform actions in reverse(!) order of registration
        for (int i = _actions.size(); --i >= 0;) {
          _actions.get(i).run();
        }
      }
    }
  }
  
  private static class PxeKernelRuntimeContext
    implements PxeKernel.RuntimeContext, Runnable {
    private MBeanServer mbeanServer;
    private PxeKernel kernel;
    private URL kernelConfig;
    
    PxeKernelRuntimeContext(MBeanServer mbeanServer, URL kernelConfig) {
      this.kernelConfig = kernelConfig;
      this.mbeanServer = mbeanServer;
      try {
        kernel = new PxeKernel(this);
      } catch(Exception e) {
        __log.error("Problem configuring PXE Kernel Runtime Context", e);
      }
    }
    
    public URL getConfigUrl() {
      return kernelConfig;
    }
    
    private PxeKernel getKernel() {
      return kernel;
    }
    
    public void run() {
      if(null == getKernel())
        return;
      try {
        __log.info("Stopping PxeKernel MBean");
        getKernel().stop();
        __log.info("Unregistering PxeKernel MBean");
        mbeanServer.unregisterMBean(getKernel().getObjectName()); // TODO: necessary?
      } catch(Exception e) {
        __log.error("Problem stopping PXE Kernel", e);
      }
    }
    
    public boolean start() {
      if(null == getKernel())
        return false;
      try {
        __log.info("Registering PxeKernel MBean");
        mbeanServer.registerMBean(getKernel(), getKernel().getObjectName());
        __log.info("Starting PxeKernel MBean");
        getKernel().start();
      } catch(Exception e) {
        __log.error("Problem starting PXE Kernel", e);
        return false;
      }
      return true;
    }
    
    /**
     * exits the VM realizing the shutdown hooks
     */
    public boolean handleShutdown() {
      Thread exitDispatch = new Thread(new Runnable() {
        public void run() {
          try {
            final int waitTime = 200;
            __log.warn("Sleeping for " + waitTime + "ms before exiting");
            Thread.sleep(waitTime);
          } catch(Exception e) {
            __log.error("Unexpected exception reached", e);
          }
          __log.warn("Exiting virtual machine");
          System.exit(0);
        }
      });
      __log.info("Setting exit threads priority to MIN_PRIORITY");
      exitDispatch.setPriority(Thread.MIN_PRIORITY);
      __log.info("Dispatching exit thread");
      exitDispatch.start();
      return true;
    }
  }
}

