/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.mngmt;

/**
 * Base interface for administrative MBeans.
 */
public interface AdminMBean {

  /**
   * Get the identifier of the domain to which this MBean belongs.
   * @return domain identifier
   */
  String getDomainId();

}
