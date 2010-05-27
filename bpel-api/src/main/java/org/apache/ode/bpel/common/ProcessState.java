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
package org.apache.ode.bpel.common;

/**
 * Convenience class for working with process states.
 */
public class ProcessState {

    /** <em>NEW</em> state: instance has not been reduced. */
  public static final short STATE_NEW = 0;

  /**
   * <em>READY</em> state: instance is waiting for a <code>&lt;receive&gt;</code>
   * or <code>&lt;pick&gt;</code> with <code>createInstance="true"</code>.
   */
  public static final short STATE_READY = 10;

  /**
   * <em>ACTIVE</em> state: instance has received a <code>&lt;receive&gt;</code>
   * or <code>&lt;pick&gt;</code> with <code>createInstance="true"</code>
   */
  public static final short STATE_ACTIVE = 20;

  /**
   * <em>COMPLETED</em> state: instance has reached the end of its execution
   * (normally).
   */
  public static final short STATE_COMPLETED_OK = 30;

  /**
   * <em>COMPLETED WITH FAULT</em> state: instance has reached the end of its execution
   * due to an uncaught fault.
   */
  public static final short STATE_COMPLETED_WITH_FAULT = 40;

  /**
   * <em>SUSPENDED</em> state: instance was suspended via a breakpoint or user
   * intervention.
   */
  public static final short STATE_SUSPENDED = 50;

  /**
   * <em>TERMINATED</em> state: instance was terminated, either via the
   * <code>&lt;terminate&gt;</code> or through manual intervention.
   * @see org.apache.ode.bpel.bdi.InstanceReference#terminate()
   */
  public static final short STATE_TERMINATED = 60;

  /**
   * An array containing the possible states.
   */
  public static final short[] ALL_STATES = {
    STATE_NEW,
    STATE_READY,
    STATE_ACTIVE,
    STATE_COMPLETED_OK,
    STATE_COMPLETED_WITH_FAULT,
    STATE_SUSPENDED,
    STATE_TERMINATED
  };

  /*
   * No instance for you.
   */
  private ProcessState(){}

  /**
   * Test whether a process state is one where the process can execute, i.e.,
   * not {@link #STATE_SUSPENDED} and not one of the completed or terminated states.
   * @param state the state of the process
   * @return <code>true</code> if the process can execute
   */
  public static boolean canExecute(short state){
     return state == STATE_READY
        ||  state == STATE_ACTIVE
        || state == STATE_NEW;
  }


  /**
   * Test whether a process state is one of the completed states, e.g.,
   * {@link #STATE_COMPLETED_OK} or {@link #STATE_COMPLETED_WITH_FAULT}.
   *
   * @param state the state of the process
   * @return <code>true</code> if the process is finished.
   */
  public static boolean isFinished(short state){
      return !(canExecute(state) || state == STATE_SUSPENDED);
  }
  /**
   * Change <code>short</code> state representation to human-readable form.
   * @param state the state of the process
   * @return human-readable state as a <code>String</code>
   */
  public static String stateToString(short state) {
    switch (state) {

      case STATE_NEW:
        return "New";

      case STATE_READY:
        return "Ready";

      case STATE_ACTIVE:
        return "Active";

      case STATE_COMPLETED_OK:
        return "Completed Ok";

      case STATE_COMPLETED_WITH_FAULT:
        return "Completed Fault";

      case STATE_SUSPENDED:
        return "Suspended";

      case STATE_TERMINATED:
        return "Terminated";

      default:
        throw new IllegalStateException("unknown state: " + state);
    }
  }
}
