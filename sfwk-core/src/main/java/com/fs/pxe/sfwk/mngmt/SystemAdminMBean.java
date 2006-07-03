/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.mngmt;

import javax.management.ObjectName;

/**
 * Represents a system in a domain (i.e. not a system instance).
 */
public interface SystemAdminMBean extends AdminMBean {

  String getName();

  boolean isEnabled();

  String getDeploymentDescriptor();

  void enable();

  void disable();

  void undeploy();

  ObjectName[] getServices();
}
