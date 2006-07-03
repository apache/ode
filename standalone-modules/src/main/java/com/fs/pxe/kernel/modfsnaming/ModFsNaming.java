/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modfsnaming;

import com.fs.naming.mem.InMemoryContextFactory;
import com.fs.pxe.kernel.PxeKernelModException;
import com.fs.utils.jmx.SimpleMBean;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FiveSight Naming PXE Kernel Module - A simple implementation of JNDI for use
 * in circumstances where server-provided JNDI is not available.
 */
public class ModFsNaming extends SimpleMBean implements ModFsNamingMBean {
  static final Log __log = LogFactory.getLog(ModFsNaming.class);

  private String _providerURL = "pxe";

  public ModFsNaming() throws NotCompliantMBeanException {
    super(ModFsNamingMBean.class);
  }

  public String getProviderURL() {
    return _providerURL;
  }

  public void setProviderURL(String providerURL) {
    _providerURL = providerURL;
  }

  protected ObjectName createObjectName() {
    return null;
  }

  public void start() throws PxeKernelModException {
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
            InMemoryContextFactory.class.getName());
    System.setProperty(Context.PROVIDER_URL, _providerURL);
    
    // Make sure we are able to get an initial context.
    try {
      new InitialContext();
    } catch (Exception ex) {
      __log.error("Error creating initial JNDI context.",ex);
      throw new PxeKernelModException("Failed to start ModFsNaming!",ex);
    }
    __log.info("Successfully started JNDI module");
  }

  public void stop() {
    // Not much to do here. 
  }

}
