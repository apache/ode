/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

/**
 * Interface implemented by nodes (such as {@link Activity} and {@link Process}
 * that can suppress join failure.
 */
public interface JoinFailureSuppressor extends BpelObject {
  /** Model element does not specify a <code>suppressJoinFailure</code> override. */
  short SUPJOINFAILURE_NOTSET = 0;

  /** Model element overrides <code>suppressJoinFailure</code> to <code>no</code>. */
  short SUPJOINFAILURE_NO = -1;

  /** Model element overrides <code>suppressJoinFailure</code> to <code>no</code>. */
  short SUPJOINFAILURE_YES = 1;

  /**
   * Set the suppress join failure flag.
   *
   * @param suppressJoinFailure suppress join failure flag code
   */
  void setSuppressJoinFailure(short suppressJoinFailure);

  /**
   * Get the suppress join failure flag.
   *
   * @return suppress join failure flag code
   */
  short getSuppressJoinFailure();

}
