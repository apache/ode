/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.mngmt.AdminMBean;

import javax.management.NotCompliantMBeanException;
import javax.management.Notification;

abstract class AdminMBeanImpl extends PxeMBean implements AdminMBean{

  private String _domainId;

  protected AdminMBeanImpl(Class intfClass, String domainId)
          throws NotCompliantMBeanException {
    super(intfClass);
    _domainId = domainId;
  }

  public String getDomainId() {
    return _domainId;
  }

  public void send(Notification notification) {
    super.send(notification);
  }

  public long nextNotificationSequence() {
    return super.nextNotificationSequence();
  }
}
