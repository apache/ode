/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.mngmt;

import javax.management.ObjectName;

/**
 * Base interface for all PXE managed runtime objects.
 */
public interface RuntimeMBean  {

  /** Get the name of the MBean. */
  ObjectName getObjectName();

}
