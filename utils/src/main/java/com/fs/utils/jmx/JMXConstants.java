/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.utils.jmx;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


/**
 * Constant related to naming of PXE objects in JMX.
 */
public final class JMXConstants {

  /** JMX domain key */
  public static final String JMX_DOMAIN = "com.fivesight.pxe";

  // type properties

  /** Value of <code>type</code> name attribute for {@link com.fs.pxe.sfwk.mngmt.DomainNodeMBean}s. */
  public static final String TYPE_DOMAINMBEAN = "DomainAdmin";

  /** Value of <code>type</code> name attribute for {@link com.fs.pxe.sfwk.mngmt.SystemRuntimeMBean}s. */
  public static final String TYPE_SYSTEMMBEAN = "SystemAdmin";

  /** Value of <code>type</code> name attribute for {@link com.fs.pxe.sfwk.mngmt.ServiceRuntimeMBean}s. */
  public static final String TYPE_SERVICEMBEAN = "service";

  /**
   * Create a JMX name for a PXE domain.
   * @param domainId domain identifier
   * @return JMX {@link ObjectName}
   */
  public static ObjectName createDomainObjectName(String domainId) throws MalformedObjectNameException {
  	Hashtable<String, String> tbl = new Hashtable<String, String>();
    tbl.put("domain", domainId);
    tbl.put("node", "node0");
    tbl.put("type", TYPE_DOMAINMBEAN);

    return new ObjectName(JMX_DOMAIN, tbl);
  }

  /**
   * Create a JMX name query to find PXE domain MBeans.
   * @return {@link ObjectName} pattern
   * @throws MalformedObjectNameException
   */ 
  public static ObjectName createDomainObjectQuery()  throws MalformedObjectNameException {
    return new ObjectName(JMX_DOMAIN + ":type=" + TYPE_DOMAINMBEAN + ",*");
  }
}
