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

import org.apache.ode.bpel.bdi.breaks.ActivityBreakpoint;
import org.apache.ode.bpel.evt.ActivityExecStartEvent;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.evt.ScopeCompletionEvent;

public class ActivityBreakpointImpl extends BreakpointImpl implements ActivityBreakpoint {
    private static final long serialVersionUID = -8717519287041871427L;
    private String _activityName;

    public ActivityBreakpointImpl(String uuid, String activityName) {
        super(uuid);
    _activityName = activityName;
    }

  /**
     * @see org.apache.ode.bpel.bdi.breaks.ActivityBreakpoint#activityName()
     */
    public String activityName() {
        return _activityName;
    }

  /**
     * @see org.apache.ode.bpel.runtime.breaks.BreakpointImpl#checkBreak(org.apache.ode.bpel.evt.BpelEvent)
     */
    public boolean checkBreak(BpelEvent event) {
        return isEnabled()
        && (event instanceof ActivityExecStartEvent
          && ((ActivityExecStartEvent)event).getActivityName() != null
          && ((ActivityExecStartEvent)event).getActivityName().equals(_activityName))
          ||
                    ((event instanceof ScopeCompletionEvent)
              && ((ScopeCompletionEvent)event).getScopeName() != null
                            && ((ScopeCompletionEvent)event).getScopeName().equals(_activityName));
    }

}
