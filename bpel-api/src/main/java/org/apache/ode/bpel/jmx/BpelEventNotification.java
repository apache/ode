/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.jmx;

import org.apache.ode.bpel.evt.BpelEvent;

import javax.management.Notification;

/**
 * JMX notification used to deliver {@link org.apache.ode.bpel.evt.BpelEvent}s to JMX
 * {@link javax.management.NotificationListener}s.
 */
public class BpelEventNotification extends Notification {
  /**
   * Constructor. Creates a JMX notification with a type matching the
   * <em>class name</em> of the passed-in {@link BpelEvent} object.
   * @param source originating object/{@link javax.management.ObjectName}
   * @param sequence event sequence
   * @param bpelEvent {@link BpelEvent} payload
   */
  public BpelEventNotification(Object source, long sequence, BpelEvent bpelEvent) {
    super(bpelEvent.getClass().getName(), source, sequence);
    setUserData(bpelEvent);
  }

  /**
   * Get the {@link BpelEvent} payload.
   * @return {@link BpelEvent} payload.
   */
  public BpelEvent getBpelEvent() {
    return (BpelEvent) getUserData();
  }

}
