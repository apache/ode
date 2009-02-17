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
 * Activity completion event.
 */
public class ScopeCompletionEvent extends ScopeEvent {
  private static final long serialVersionUID = 1L;

  private boolean _success;
  private QName _fault;
  
  public ScopeCompletionEvent(boolean success, QName fault) {
    _success = success;
    _fault = fault;
  }

    /**
     * @param fault The fault to set.
     */
    public void setFault(QName fault) {
        _fault = fault;
    }

    /**
     * @return Returns the fault.
     */
    public QName getFault() {
        return _fault;
    }
  
    public boolean isSuccess() {
        return _success;
    }
    public void setSuccess(boolean success) {
        _success = success;
    }
}
