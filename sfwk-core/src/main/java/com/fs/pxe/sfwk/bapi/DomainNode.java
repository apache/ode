/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.bapi;

import com.fs.pxe.sfwk.core.PxeSystemException;
import com.fs.pxe.sfwk.mngmt.DomainAdminMBean;
import com.fs.pxe.sfwk.spi.PxeException;
import com.fs.pxe.sfwk.spi.ServiceProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>
 * Abstract base class for the PXE container implementation. Conceptually,
 * objects of this class represent a processing node of a given domain (i.e.
 * unqiue container instance): that is, one domain / container instance may
 * consist of multiple nodes.This class is used by binding implementations to
 * manipulate the state of the domain.
 * </p>
 */
public abstract class DomainNode {
  /** Class-level logger. */
  private static final Log __log = LogFactory.getLog(DomainNode.class);

  /**
   * System property containing the name of the {@link DomainNode}
   * implementation.
   */
  public static final String PROPERTY_DOMAINIMPLCLASS = "pxe.domainImplClass";

  /** Default implementation of the {@link DomainNode}. */
  public static final String DEFAULT_DOMAINIMPLCLASS = "com.fs.pxe.sfwk.impl.DomainNodeImpl";

  /**
   * <p>
   * Factory method for creating {@link DomainNode} objects. This method
   * decouples the PXE container implementation from the PXE container
   * interfaces. While it is possible to control which class gets created by
   * this method (by specifying a class name in the system property
   * identified by the {@link DomainNode#PROPERTY_DOMAINIMPLCLASS} field),
   * there should be no reason to do this, and by default the class
   * identified by the {@link DomainNode#DEFAULT_DOMAINIMPLCLASS} field will
   * be created.
   * </p>
   * 
   * <p>
   * <em>Note: in this class, the dependency on the implementation class must
   * be limited to a the implementation class name (not the actual
   * implementation class): otherwise a circular dependency would exist.
   * </em>
   * </p>
   *
   * @param config domain configuration
   *
   * @return new {@link DomainNode} instance
   *
   * @throws IllegalArgumentException DOCUMENTME
   * @throws RuntimeException DOCUMENTME
   */
  public static DomainNode createDomainNode(DomainConfig config) {
    if (config == null) {
      throw new IllegalArgumentException("config parameter may not be null!");
    }

    String domainNodeImplClassName = System.getProperty(PROPERTY_DOMAINIMPLCLASS);

    if (domainNodeImplClassName == null) {
      domainNodeImplClassName = DEFAULT_DOMAINIMPLCLASS;
    }

    try {
      Class domainNodeImplClass = DomainNode.class.getClassLoader()
                                                  .loadClass(domainNodeImplClassName);
      Constructor ctor = domainNodeImplClass.getConstructor(new Class[] {
                                                              DomainConfig.class,
                                                            });
      DomainNode domainNode = (DomainNode)ctor.newInstance(new Object[] {
                                                             config
                                                           });

      __log.debug("domain node created: " + domainNode);

      return domainNode;
    } catch (Exception ex) {
      String msg = "PXE: Unable to instantiate domain implementation: "
                   + domainNodeImplClassName;
      __log.fatal(msg, ex);
      throw new RuntimeException(msg);
    }
  }

  /**
   * Get the unique identifier for this domain (container instance).
   *
   * @return globally unique identifier for the domain
   */
  public abstract String getDomainId();

  public abstract void registerServiceProvider(String uri, ServiceProvider provider)
  	throws ServiceProviderInstallationException;
  
  /**
   * Permits local resolution of PXE MBean objects.
   * @param objectName
   * @param cls
   * @return
   * @throws PxeException
   */
  public abstract Object resolve(ObjectName objectName, Class cls) throws PxeException;

  public abstract Object onServiceProviderInvoke(String serviceProviderUri, Object sessionId, String name, Object[] args)
                                          throws PxeException, InvocationTargetException ;

  public abstract void closeServiceProviderSession(String serviceProviderUri, Object sessionId);

  public abstract Object createServiceProviderSession(String serviceProviderUri, Class interactionClass);

  public abstract void initialize(boolean disableAll) throws PxeSystemException;
  
  public abstract void start() throws PxeSystemException;

  public abstract DomainAdminMBean getDomainAdminMBean();

  public abstract void shutdown() throws PxeSystemException ;
}
