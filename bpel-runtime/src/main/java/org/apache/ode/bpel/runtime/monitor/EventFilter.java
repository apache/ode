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
package org.apache.ode.bpel.runtime.monitor;

import java.io.Serializable;


/**
 * Specifies event filter associated with a process instance.
 */
public class EventFilter implements Serializable {
  private static final long serialVersionUID = 8730444480896696770L;
  private Serializable _processInstance;
  private int _maxEvents = 20;
  private int _skip = 0;

  /**
   * Constructor.
   * @param processInstance process instance with which to associate this event filter
   */
  public EventFilter(Serializable processInstance) {
    _processInstance = processInstance;
  }

  /**
   * Constructor.
   * @param processInstance process instance with which to associate this event filter
   * @param skip
   */
  public EventFilter(Serializable processInstance, int skip) {
    this(processInstance);
    _skip = skip;
  }

  /**
   * Set the maximum number of events to return.
   *
   * @param eventNo the maximum number of events to return
   */
  public void setMaxReturn(int eventNo) {
    _maxEvents = eventNo;
  }

  /**
   * Get the maximum number of events to return.
   *
   * @return maximum number of events to return
   */
  public int getMaxReturn() {
    return _maxEvents;
  }

  /**
   * Unique identifier of the process instance.
   *
   * @return process instance identifier.
   */
  public Serializable getProcessInstance() {
    return _processInstance;
  }

  /**
   * The number of events to skip.  Useful for paging for event iteration.
   *
   * @param skip Events to skip.
   */
  public void setSkip(int skip) {
    _skip = skip;
  }

  /**
   * The number of events to skip.  Useful for paging for event iteration.
   *
   * @return Returns events to skip.
   */
  public int getSkip() {
    return _skip;
  }
}
