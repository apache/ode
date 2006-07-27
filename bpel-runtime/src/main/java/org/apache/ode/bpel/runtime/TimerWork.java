/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.utils.ObjectPrinter;

import java.io.Serializable;

/**
 * Work object representating a timer event.
 */
class TimerWork implements Serializable {
  static final long serialVersionUID = 1;

  private Long _pid;
  private String _timerChannel;

  /** Constructor.	 */
	TimerWork(Long pid, String timerChannel) {
    _pid = pid;
		_timerChannel = timerChannel;
	}

  /** Get the Process Instance ID (PIID). */
  public Long getPID() {
    return _pid;
  }

  /** Get the exported for of the timer response channel. */
  public String getTimerChannel(){
  	return _timerChannel;
  }

  public String toString() {
    return ObjectPrinter.toString(this, new Object[] {
      "pid", _pid,
      "timerChannel", _timerChannel
    });
  }

}
