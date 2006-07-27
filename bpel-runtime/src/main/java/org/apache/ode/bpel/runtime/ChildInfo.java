/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import java.io.Serializable;

/**
 * Infomration about a child.
 */
class ChildInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	/** Has it been completed? */
  boolean completed;

  /** The child agent. */
  ActivityInfo activity;

  /** a terminate message has been sent to child */
  boolean terminated;

  ChildInfo(ActivityInfo activity) {
    this.activity = activity;
  }

}
