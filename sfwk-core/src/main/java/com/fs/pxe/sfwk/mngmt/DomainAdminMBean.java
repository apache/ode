/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.mngmt;

import java.net.MalformedURLException;

import javax.management.MBeanNotificationInfo;
import javax.management.ObjectName;

/**
 * MBean exposing the administration interface for a PXE domain.
 */
public interface DomainAdminMBean  {

  public static final MBeanNotificationInfo[] NOTIFICATION_INFO = new MBeanNotificationInfo[] {
    new MBeanNotificationInfo(DomainNotification.TYPES,DomainNotification.class.getName(), "Domain Notifications")
  };

  /**
   * Get the identifier for this domain.
   * @return domain identifier
   */
  String getDomainId();

  /**
   * Deploy a system archive located at a URL in the target domain.
   *
   * @param url the URL of the system archive (local to the domain)
   *
   * @return the {@link javax.management.ObjectName} of the newly deployed system
   * @throws java.net.MalformedURLException if the URL is malformed
   */
  ObjectName deploySystem(String url, boolean undeploy)
          throws MalformedURLException, ManagementException;
  /**
   * Get the systems deployed within this domain.
   * @return array of {@link ObjectName}s representing the deployed
   *         {@link SystemAdminMBean} objects
   */
  ObjectName[] getSystems();

  /**
   * Get the name of the {@link SystemAdminMBean} for a system name.
   * @param name system name
   * @return {@link ObjectName} of the associated {@link SystemAdminMBean} object
   */
  ObjectName getSystem(String name);
}
