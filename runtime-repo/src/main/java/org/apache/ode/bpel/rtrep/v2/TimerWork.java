/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
