/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modrmi;

import com.fs.pxe.kernel.PxeKernelModException;
import com.fs.utils.jmx.SimpleMBean;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FiveSight Naming PXE Kernel Module - A simple implementation of JNDI for use
 * in circumstances where server-provided JNDI is not available.
 */
public class ModRmiRegistry extends SimpleMBean implements ModRmiRegistryMBean {
  private static final Log __log = LogFactory.getLog(ModRmiRegistry.class);

  private int _port = -1;

  public ModRmiRegistry() throws NotCompliantMBeanException {
    super(ModRmiRegistryMBean.class);
  }

  public int getPort(){
    return _port;
  }

  /**
   * because this Module will always be started before the SFWK Module there
   * is a chicken and the egg problem where we need to set the RMI registry port
   * here
   */
  public void setPort(int port){
    _port = port;
  }

  public void start() throws PxeKernelModException {
    if (_port == -1)
      throw new PxeKernelModException("Must specify RMI registry port!");
    
    try {
      LocateRegistry.createRegistry(_port);
    } catch (RemoteException e1) {
      __log.error("Unable to create rmi registry", e1);
    }
    __log.info("RMIRegistry started on port " + _port);  
  }

  public void stop() {
    // Not much to do here. 
  }

	/**
	 * @see com.fs.utils.jmx.SimpleMBean#createObjectName()
	 */
	protected ObjectName createObjectName() {
		return null;
	}

}
