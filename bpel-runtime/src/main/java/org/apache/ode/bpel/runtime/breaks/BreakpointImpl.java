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
package org.apache.ode.bpel.runtime.breaks;

import org.apache.ode.bpel.bdi.breaks.Breakpoint;
import org.apache.ode.bpel.evt.BpelEvent;

import java.io.Serializable;

public abstract class BreakpointImpl implements Breakpoint, Serializable {

  private String _uuid;
  private boolean _enabled = true;

    public BreakpointImpl(String uuid) {
        _uuid = uuid;
    }

  /**
     * @see org.apache.ode.bpel.bdi.breaks.Breakpoint#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

  /**
     * @see org.apache.ode.bpel.bdi.breaks.Breakpoint#isEnabled()
     */
    public boolean isEnabled() {
        return _enabled;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return obj instanceof BreakpointImpl
      && _uuid.equals(((BreakpointImpl)obj)._uuid);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return _uuid.hashCode();
    }

  public abstract boolean checkBreak(BpelEvent event);
}

