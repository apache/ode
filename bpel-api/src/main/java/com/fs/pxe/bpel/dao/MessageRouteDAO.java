/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.dao;

/**
 * Data access object representing a message consumer. A message consumer
 * represents an unsatisfied BPEL <code>pick</code> or <code>receive</code>
 * activity.
 */
public interface MessageRouteDAO  {

  /**
   * Get the BPEL process instance to which this consumer belongs.
   *
   * @return the process instance to which this consumer belongs
   */
  ProcessInstanceDAO getTargetInstance();

  String getGroupId();

  int getIndex();
}
