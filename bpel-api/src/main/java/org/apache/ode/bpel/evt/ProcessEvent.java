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
package org.apache.ode.bpel.evt;

import javax.xml.namespace.QName;

/**
 * Base class for process events.
 */
public abstract class ProcessEvent extends BpelEvent {

  private QName _processId;
  private QName _processname;

  public ProcessEvent() {
  }


  public void setProcessId(QName processId) {
    _processId = processId;
  }

  public QName getProcessId() {
    return _processId;
  }

  public void setProcessName(QName processName) {
    _processname = processName;
  }

  /**
   * Gets process name.
   * 
   * @return the process name
   */
  public QName getProcessName() {
    return _processname;
  }

}
