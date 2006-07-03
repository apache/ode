/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.ServiceContext;
import com.fs.pxe.sfwk.spi.ServiceEvent;

/**
 * PXE implementation of {@link com.fs.pxe.sfwk.evt.ServiceEvent}.
 */
abstract class ServiceEventImpl implements ServiceEvent {
  short _eventType;
  ServiceContext _targetService;

  protected ServiceEventImpl(short eventType) {
    _eventType = eventType;
  }

  public short getEventType() {
    return _eventType;
  }

  public ServiceContext getTargetService() {
    return _targetService;
  }

}
