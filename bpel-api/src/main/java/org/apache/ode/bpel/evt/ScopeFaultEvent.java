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
 * Event indicating that an activity completed with a fault.
 */
public class ScopeFaultEvent
  extends ScopeEvent {

    private static final long serialVersionUID = 1L;
    private QName _faultType;
  private int _faultLineNo = -1;
  private String _explanation;

  public ScopeFaultEvent() {
    super();
  }

    public int getFaultLineNo() {
        return _faultLineNo;
    }
    public void setFaultLineNo(int faultLineNo) {
        _faultLineNo = faultLineNo;
    }
  public ScopeFaultEvent(QName faultType, int lineNo, String explanation) {
    _faultType = faultType;
    _faultLineNo = lineNo;
    _explanation = explanation;
  }


  /**
     * Get the fault type.
     * @return the fault type
     */
    public QName getFaultType() {
    return _faultType;
  }

  public void setFaultType(QName faultType) {
    _faultType = faultType;
  }

    public String getExplanation() {
        return _explanation;
    }

    public void setExplanation(String explanation) {
        _explanation = explanation;
    }
}
