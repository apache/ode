/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.mngmt;

import javax.management.ObjectName;

public interface ServiceRuntimeMBean extends RuntimeMBean {

  String getName();

  String getServiceProviderURI();

  ObjectName getServiceProvider();

}
