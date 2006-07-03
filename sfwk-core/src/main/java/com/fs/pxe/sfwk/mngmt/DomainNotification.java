/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.mngmt;

import javax.management.Notification;
import javax.management.ObjectName;


/**
 * JMX {@link Notification} representing PXE domain events such as system
 * deployment and undeployment.
 */
public class DomainNotification extends Notification {
  /** System was deployed (first) */
  public static final String SYSTEM_DEPLOYMENT = "pxe.SystemDeployed";

  /** System was loaded (second) */
  public static final String SYSTEM_LOAD = "pxe.SystemLoaded";

  /** System was registered (third) */
  public static final String SYSTEM_REGISTRATION = "pxe.SystemRegistered";
  
  /** System was activated (last) */
  public static final String SYSTEM_ACTIVATION = "pxe.SystemActivated";
  
  /** System was deactivated (first) */
  public static final String SYSTEM_DEACTIVATION = "pxe.SystemDeactivated";
  
  /** System was registered (second) */
  public static final String SYSTEM_UNREGISTRATION = "pxe.SystemUnregistered";
  
  /** System was unloaded (third) */
  public static final String SYSTEM_UNLOAD = "pxe.SystemUnloaded";

  /** System was undeployed (fourth) */
  public static final String SYSTEM_UNDEPLOYMENT = "pxe.SystemUndeployed";
  
  /** System was undeployed (last) */
  public static final String SYSTEM_PURGE = "pxe.SystemPurged";
  
  public static final String[] TYPES = new String[] {
    SYSTEM_DEPLOYMENT,
    SYSTEM_LOAD,
    SYSTEM_REGISTRATION,
    SYSTEM_ACTIVATION,
    SYSTEM_DEACTIVATION,
    SYSTEM_UNREGISTRATION,
    SYSTEM_UNLOAD,
    SYSTEM_UNDEPLOYMENT
  };

  private String _apropos;

  /**
   * Constructor.
   * @param type event type
   * @param source event source
   * @param seq event sequence
   * @param systemName apropos system
   */
  public DomainNotification(String type, ObjectName source, long seq, String systemName) {
    super(type, source, seq);
    _apropos = systemName;
  }

  /**
   * Get the name of the system to which this event applies.
   * @return name of the associated system
   */
  public String getSystemName() {
    return _apropos;
  }

}
