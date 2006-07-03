/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel;

import com.fs.utils.jmx.JMXConstants;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.management.*;
import javax.management.loading.MLet;
import javax.management.loading.PrivateMLet;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * PXE JMX Micro-Kernel.
 */
public class PxeKernel extends StandardMBean implements PxeKernelMBean, MBeanRegistration {

  private static Log __log = LogFactory.getLog(PxeKernel.class);

  private MBeanServer _mbeanServer;
  private MLet _mletLoader;
  private ObjectName _kernelObjectName;
  private ObjectName _mletLoaderName;
  private RuntimeContext runtimeContext;
  private boolean shuttingDown = false;
  private final List<ObjectName> _startedModules = new ArrayList<ObjectName>();
  
  public PxeKernel() throws NotCompliantMBeanException {
    super(PxeKernelMBean.class);
  }

  public PxeKernel(RuntimeContext runtimeContext) throws NotCompliantMBeanException {
    super(PxeKernelMBean.class);
    if(null == runtimeContext)
      throw new NullPointerException("PxeKernel RuntimeContext is null");
    
    this.runtimeContext = runtimeContext;
    
    if(null == getRuntimeContext().getConfigUrl())
      throw new NullPointerException("PxeKernel RuntimeContext Config URL is null");
    
    try {
      _kernelObjectName = new ObjectName(JMXConstants.JMX_DOMAIN + ":type=Kernel");
      _mletLoaderName = new ObjectName(JMXConstants.JMX_DOMAIN + ":type=PxeMLetLoader");
    }
    catch (MalformedObjectNameException e) {
      // unlikely to happen
      throw new RuntimeException(e);
    }
  }

  private RuntimeContext getRuntimeContext() {
    return runtimeContext;
  }

  public ObjectName getObjectName() {
    return _kernelObjectName;
  }

  public String getConfigURL() {
    return (null != getRuntimeContext()) ?
      getRuntimeContext().getConfigUrl().toString() : null;
  }

  public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
    _mbeanServer = mBeanServer;
    return objectName;
  }

  public void postRegister(Boolean aBoolean) {
    __log.debug("PxeKernel registed in JMX.");
  }

  public void shutdown() {
    if(!shuttingDown)
      shuttingDown = true;
    else
      return;
    __log.info("Att shutting down.");
    shuttingDown = getRuntimeContext().handleShutdown();
    if(!shuttingDown)
    __log.info("PxeKernel RuntimeContext[" +
      getRuntimeContext().getClass().getName() + "] failed to shutdown.");      
  }

  /**
   * @see com.fs.pxe.kernel.PxeKernelMBean#start()
   */
  public void start() throws PxeKernelModException {
    if (null == _mbeanServer)
      throw new PxeKernelModException(
        "PxeKernel MBean has not been registered: local MBeanServer reference is null");
    if(null != _mletLoader || shuttingDown)
      return;
    try {
      __log.info("PxeKernel starting.");
      _mletLoader = new PrivateMLet(new URL[0],
        Thread.currentThread().getContextClassLoader(),true);
      _mbeanServer.registerMBean(_mletLoader, _mletLoaderName);
      Thread.currentThread().setContextClassLoader(_mletLoader);
      PxeConfigProcessor pxeConfig = new PxeConfigProcessor(_mbeanServer, _mletLoader);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      Document doc = dbf.newDocumentBuilder().parse(
        new InputSource(getRuntimeContext().getConfigUrl().openStream()));
      pxeConfig.evaluate(null, doc.getDocumentElement());
      
      _startedModules.addAll(pxeConfig.getModules());
    }
    catch (PxeConfigException ex) {
      ex.printStackTrace(System.out);
      if (ex.getCause() instanceof PxeKernelModException)
        throw (PxeKernelModException)ex.getCause();
      else
        throw new PxeKernelModException(ex);
    }
    catch (Throwable ex) {
      ex.printStackTrace(System.out);
      throw new PxeKernelModException(ex);
    }
  }
  
  @SuppressWarnings("unchecked")
  public void stop() {
    __log.info("PxeKernel stopping");
    
    if(null == _mletLoader || shuttingDown)
      return;
    
    try {
      Collections.reverse(_startedModules);
      for(ObjectName mbeanName : _startedModules) {
        _mbeanServer.invoke(mbeanName, "stop", null, null);
        _mbeanServer.unregisterMBean(mbeanName);
      }
      _startedModules.clear();
      
      /*
      // stop & unregister ServiceProvider modules
      // TODO get rid of "PXE" name in code (with better properties on mbeans)
      List<ObjectName> serviceNames = new ArrayList<ObjectName>();
      serviceNames.add(new ObjectName(JMXConstants.JMX_DOMAIN + ":mod=PXE"));
      
      ObjectName serviceQuery = new ObjectName(JMXConstants.JMX_DOMAIN + ":*,mod=ServiceProvider");
      serviceNames.addAll(_mbeanServer.queryNames(serviceQuery, null));

      for (ObjectName mbeanName : serviceNames) {
        _mbeanServer.invoke(mbeanName, "stop", null, null);
        _mbeanServer.unregisterMBean(mbeanName);
      }
      */
      // unregister mlet
      _mbeanServer.unregisterMBean(_mletLoaderName);
      _mletLoader = null;
      
    }
    catch (Throwable t) {
      __log.error(t);
    }
  }  

  /**
   * @see javax.management.MBeanRegistration#preDeregister()
   */
  public void preDeregister() throws Exception {
    __log.debug("PxeKernel will be removed from JMX.");
  }

  /**
   * @see javax.management.MBeanRegistration#postDeregister()
   */
  public void postDeregister() {
    __log.debug("PxeKernel removed from JMX.");
    _mbeanServer = null;
  }

  public interface RuntimeContext {
    boolean handleShutdown();
    URL getConfigUrl();
  }

}
