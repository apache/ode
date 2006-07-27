/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

import java.util.Set;

/**
 * Reperesentation of a BPEL <code>&lt;pick&gt;</code> activity.
 */
public interface PickActivity extends CreateInstanceActivity {
  /**
   * Set the "create instance" flag.
   *
   * @param createInstance value of the "create instance" flag.
   */
  void setCreateInstance(boolean createInstance);

  /**
   * Check if the "create instance" flag is set.
   *
   * @return value of the "create instance" flag.
   */
  boolean isCreateInstance();

  /**
   * Add an 'onMessage' waiter.
   *
   * @param onMsg
   */
  void addOnMessage(OnMessage onMsg);

  /**
   * Return all 'onMessage' handlers.
   *
   * @return
   */
  Set<OnMessage> getOnMessages();

  /**
   * Adds an alarm for the pick.
   *
   * @param onAlarm
   */
  void addOnAlarm(OnAlarm onAlarm);

  /**
   * Gets the alarm for the pick; may be <code>null</code>.
   *
   * @return
   */
  Set<OnAlarm> getOnAlarms();
}
