/*
 * File:      $Id$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.integrations.as9;

import java.net.URL;

import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.LifecycleEventContext;

import com.fs.pxe.kernel.PxeKernel;
import com.fs.utils.fs.TempFileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.Properties;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Supports the logical role that the PXE Standalone runtime context provides
 * with the fivesight.bootstrap.BootLoader and com.fs.pxe.server.Main
 *
 * see https://glassfish.dev.java.net/javaee5/docs/DG/beamc.html
 */
public class PxeSunAS9LifecycleListener extends Object
  implements LifecycleListener {

  private static Log __log = LogFactory.getLog(PxeSunAS9LifecycleListener.class);
  
  /**
   * Required init property of the lifecycle module configuration used to configure
   * the Pxe Kernel, there is no default.
   */
  public static final String CONFIG_FILE_PROP = "pxe.config.file";
  
  /**
   * Optional init property of the lifecycle module configuration used to
   * establish system properties which may be used as PXE defaults or referenced
   * in the Pxe Kernel configuration file.  See pxe.home/etc/pxe.properties for
   * an example of appropriate file.
   */
  public static final String SYS_PROPERTIES_FILE_PROP = "pxe.sys.properties.file";
  
  private static final String PXE_HOME_PROP = "pxe.home";
  private static final String PXE_KERNEL_THREAD_NAME = "PXE Kernel";
  
  private SunAS9LifecycleContext runtimeContext;
  private boolean enabled = false;
  
  public PxeSunAS9LifecycleListener() {
      __log.info("Listener Created.");    
  }
  
  private boolean isEnabled() {
    return getRuntimeContext() != null && getRuntimeContext().getKernel() != null;
  }
  
  private SunAS9LifecycleContext getRuntimeContext() {
    return runtimeContext;
  }
  
  private void setRuntimeContext(SunAS9LifecycleContext runtimeContext) {
    this.runtimeContext = runtimeContext;
  }
  
  public void handleEvent(LifecycleEvent event) throws ServerLifecycleException {
    try {
      switch(event.getEventType()) {
        case LifecycleEvent.INIT_EVENT:
          init(event.getLifecycleEventContext(),(Properties)event.getData());
          break;

        case LifecycleEvent.STARTUP_EVENT:
          start(event.getLifecycleEventContext());
          break;

        case LifecycleEvent.READY_EVENT:
          ready(event.getLifecycleEventContext());
          break;

        case LifecycleEvent.SHUTDOWN_EVENT:
          shutdown(event.getLifecycleEventContext());
          break;

        case LifecycleEvent.TERMINATION_EVENT:
          terminate(event.getLifecycleEventContext());
          break;
      }
    } catch(Throwable t) {
      if(t instanceof ServerLifecycleException)
        throw (ServerLifecycleException)t;
      else {
        throw new ServerLifecycleException("Exception reached in handleEvent event=" + event,t);
      }
    }
  }
  
  private void init(LifecycleEventContext context, Properties props)
    throws SecurityException, MalformedURLException {
    __log.info("init begin" + isEnabled());
    
    //
    // validate pxe.home system property
    //
    String pxeHomeFileName = System.getProperty(PXE_HOME_PROP);
    if(pxeHomeFileName != null && pxeHomeFileName.trim().length()>0) {
      __log.info("init " + PXE_HOME_PROP + "=" + pxeHomeFileName);
    } else {
      __log.warn("init " + PXE_HOME_PROP + " system property not set checking init parameters");
      throw new IllegalArgumentException("init " + PXE_HOME_PROP +
        " system property is not defined");
    }
    File pxeHome = new File(pxeHomeFileName);
    if(!pxeHome.exists() || !pxeHome.isDirectory()) {
      __log.error("init " + PXE_HOME_PROP + "=" + pxeHomeFileName + " does not exist");
      throw new IllegalArgumentException("init invalid value for " +
        PXE_HOME_PROP + " " + pxeHomeFileName + " does not exist or is not a directory");
    }
    
    //
    // validate optional SYS_PROPERTIES_FILE_PROP init parameter
    //
    String sysPropsFileName = props.getProperty(SYS_PROPERTIES_FILE_PROP);
    if(sysPropsFileName != null && sysPropsFileName.trim().length()>0) {
      __log.info("init " + SYS_PROPERTIES_FILE_PROP + "=" + sysPropsFileName);
      File sysPropsFile = new File(sysPropsFileName);
      if(!sysPropsFile.exists()) {
        __log.error("init " + SYS_PROPERTIES_FILE_PROP + "=" + sysPropsFileName + " does not exist");
        throw new IllegalArgumentException("init invalid value for optional init property " +
          SYS_PROPERTIES_FILE_PROP + " " + sysPropsFileName + " does not exist");
      }
      if(!sysPropsFile.canRead()) {
        __log.error("init " + SYS_PROPERTIES_FILE_PROP + "=" + sysPropsFileName + " cannot be read");
        throw new IllegalArgumentException("init invalid value for optional init property " +
          SYS_PROPERTIES_FILE_PROP + " " + sysPropsFileName + " cannot be read");
      }
      Properties sysProps = new Properties();
      try {
        FileInputStream fis = new FileInputStream(sysPropsFile);
        try {
          sysProps.load(fis);
        } finally {
          fis.close();
        }
        // NOTE: we promote only those properties in the property file that
        // have not been overriden on the domain JVM configuration.
        sysProps.keySet().removeAll(System.getProperties().keySet());
        System.getProperties().putAll(sysProps);
        
      } catch(IOException ioe) {
        throw new IllegalArgumentException("init invalid value for optional init property " +
          SYS_PROPERTIES_FILE_PROP + " loading " + sysPropsFileName + " realized exception ",ioe);
      }
    
    }

      
    //
    // validate CONFIG_FILE_PROP init parameter
    //
    String configFileName = props.getProperty(CONFIG_FILE_PROP);
    if(configFileName != null && configFileName.trim().length()>0) {
      __log.info("init " + CONFIG_FILE_PROP + "=" + configFileName);
      File configFile = new File(configFileName);
      if(!configFile.exists()) {
        __log.error("init " + CONFIG_FILE_PROP + "=" + configFileName + " does not exist");
        throw new IllegalArgumentException("init invalid value for required init property " +
          CONFIG_FILE_PROP + " " + configFileName + " does not exist");
      }
      if(!configFile.canRead()) {
        __log.error("init " + CONFIG_FILE_PROP + "=" + configFileName + " cannot be read");
        throw new IllegalArgumentException("init invalid value for required init property " +
          CONFIG_FILE_PROP + " " + configFileName + " cannot be read");
      }
      setRuntimeContext(new SunAS9LifecycleContext(configFile.toURL()));
    } else {
      throw new IllegalArgumentException("init cannot find required init property " +
        CONFIG_FILE_PROP);
    }
    
    initWorkarounds(context, props);
    
    __log.info("init end enabled=" + isEnabled());
  }

  private void start(LifecycleEventContext context) {
    __log.info("start enabled=" + isEnabled());
    if(!isEnabled())
      return;

    workaroundStart(context);
  }

  private void ready(LifecycleEventContext context) {
    __log.info("ready enabled=" + isEnabled());
    if(!isEnabled())
      return;
    createKernelThread().start();
  }

  private void shutdown(LifecycleEventContext context) {
    __log.info("shutdown enabled=" + isEnabled());
    if(!isEnabled())
      return;
    getRuntimeContext().handleShutdown();
  }

  private void terminate(LifecycleEventContext context) {
    __log.info("terminate enabled=" + isEnabled());
    if(!isEnabled())
      return;
    TempFileManager.cleanup();
  }
  
  //////////////////////////////////////////////////////////////////////////////
  ///////////////////// PxeKernel.RuntimeContext Impl //////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
  static class SunAS9LifecycleContext extends Object
    implements PxeKernel.RuntimeContext, Runnable {
    
    private URL url;
    private PxeKernel kernel;
    
    SunAS9LifecycleContext(URL url) {
      this.url = url;
      __log.info("Config URL is " + getConfigUrl());    
      try {
        setKernel(new PxeKernel(this));
      } catch(Exception e) {
        __log.error("Exception reached creating PxeKernel.RuntimeContext", e);
      }
    }
    
    /**
     * when we actually get around to starting PxeKernel we want to be in
     * a new thread, not in the AS main thread
     */
    public void run() {
      if(null == getKernel())
        return;
      try {
        __log.info("Registering PxeKernel with Platform MBeanServer as " +
          getKernel().getObjectName());
        ManagementFactory.getPlatformMBeanServer().registerMBean(
          getKernel(), getKernel().getObjectName());
        __log.info("Starting PxeKernel");
        getKernel().start();
        __log.info("PxeKernel Started");
      } catch(Exception e) {
        __log.error("Exception reached starting PXEKernel", e);
      }
    }
    
    public boolean handleShutdown() {
      __log.info("handleShutdown called");
      if(null == getKernel()) {
        __log.info("PxeKernel does not exist");
        return false;
      }
      try {
        __log.info("Calling PxeKernel.stop()");
        getKernel().stop();
        __log.info("Unregistering PxeKernel MBean ObjectName=" + getKernel().getObjectName());
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(
          getKernel().getObjectName());
        setKernel(null);
      } catch(Exception e) {
        __log.error("Problem shutting down PXE Kernel", e);
        return false;
      }
      return true;
    }
    
    private void setKernel(PxeKernel kernel) {
      this.kernel = kernel;
    }

    PxeKernel getKernel() {
      return kernel;
    }
    
    public URL getConfigUrl() {
      return url;
    }
  };
  
  
  //////////////////////////////////////////////////////////////////////////////
  ////////////////////////////// Workarounds ///////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
  /**
   * flag to workaround issue 6378375
   */
  private static final String PRIME_DATASOURCE_PROP = "pxe.prime.datasource";
  private static final String DATASOURCE_PROP = "pxe.datasource";
  private static final boolean PRIME_DATASOURCE_DEFAULT = true;
  private static final String DATASOURCENAME_DEFAULT = "jdbc/pxe-ds__pm";
  
  /**
   * if true then setContextClassloader is traced
   */
  private static final String TRACE_KERNEL_THREAD_PROP = "pxe.trace.kernel.thread";
  private static final boolean TRACE_KERNEL_THREAD_DEFAULT = false;
  
  private boolean traceKernelThread = TRACE_KERNEL_THREAD_DEFAULT;
  private boolean primeDataSource = PRIME_DATASOURCE_DEFAULT;
  private String dataSourceName = DATASOURCENAME_DEFAULT;
  
  private String getDataSourceName() {
    return dataSourceName;
  }
  
  private void setDataSourceName(String dataSourceName) {
    this.dataSourceName = dataSourceName;
  }
  
  private boolean isPrimeDataSource() {
    return primeDataSource;
  }
  
  private void setPrimeDataSource(boolean flag) {
    primeDataSource = flag;
  }
  
  private boolean isTraceKernelThread() {
    return traceKernelThread;
  }
  
  private void setTraceKernelThread(boolean flag) {
    traceKernelThread = flag;
  }
  
  private void initWorkarounds(LifecycleEventContext context, Properties props) {
    __log.info("initWorkarounds");
    
    String primeDataSourceValue = props.getProperty(PRIME_DATASOURCE_PROP);
    if(primeDataSourceValue != null && primeDataSourceValue.trim().length()>0) {
      __log.info("initWorkarounds " + PRIME_DATASOURCE_PROP + "=" + primeDataSourceValue);
      setPrimeDataSource(Boolean.parseBoolean(primeDataSourceValue));
    }
    
    String dataSourceValue = props.getProperty(DATASOURCE_PROP);
    if(dataSourceValue != null && dataSourceValue.trim().length()>0) {
      __log.info("initWorkarounds " + DATASOURCE_PROP + "=" + dataSourceValue);
      setDataSourceName(dataSourceValue);
    }
    
    String traceKernelThreadValue = props.getProperty(TRACE_KERNEL_THREAD_PROP);
    if(traceKernelThreadValue != null && traceKernelThreadValue.trim().length()>0) {
      __log.info("initWorkarounds " + TRACE_KERNEL_THREAD_PROP + "=" + traceKernelThreadValue);
      setTraceKernelThread(Boolean.parseBoolean(traceKernelThreadValue));
    }
    __log.info("initWorkarounds isPrimeDataSource()=" + isPrimeDataSource());
    __log.info("initWorkarounds getDataSourceName()=" + getDataSourceName());
    __log.info("initWorkarounds isTraceKernelThread()=" + isTraceKernelThread());
  }
  
  private void workaroundStart(LifecycleEventContext context) {
    __log.info("workaroundStart");
    
    if(!isPrimeDataSource() || !isEnabled())
      return;

    workaroundPrimeDataSource(context);
  }

  private Thread createKernelThread() {
    __log.info("createKernelThread trace=" + isTraceKernelThread());
    if(isTraceKernelThread())
      return new DebugThread(getRuntimeContext(),PXE_KERNEL_THREAD_NAME);
    else
      return new Thread(getRuntimeContext(),PXE_KERNEL_THREAD_NAME);
  }

  
  private void workaroundPrimeDataSource(LifecycleEventContext context) {
    __log.warn("workaroundPrimeDataSource");
    //
    // Sun Bug - 6378375
    //
    // The Hibernate DAO module does a JNDI lookup during Kernel start, this causes
    // the context classloader of the the PXE Kernel thread to be changed and PXE
    // or PXE dependent classes cannot be loaded after this point.
    //
    // The workaround is to avoid that codepath through the AS during the Kernel
    // start by priming the resource here.
    //
    try {
      __log.warn("Working Around Classloader Bug -> prefetching" +
        getDataSourceName());
      context.getInitialContext().lookup(getDataSourceName());
    } catch(Exception e) {
      __log.error("Exception reached looking up " + getDataSourceName());
    }

  }
  
  static class DebugThread extends Thread {
    public DebugThread(Runnable runner, String name) {
      super(runner,name);
    }
    
    public void setContextClassLoader(ClassLoader cl) {
      __log.warn("************* DebugThread.setContextClassLoader classloader=" +
        cl.toString() + " type=" + cl.getClass().getName() +
        " parent=" + cl.getParent().toString() + " type=" + 
        cl.getParent().getClass().getName());
      __log.error("Cluprit Stacktrace", new Exception());
      super.setContextClassLoader(cl);
    }
  }  
}
