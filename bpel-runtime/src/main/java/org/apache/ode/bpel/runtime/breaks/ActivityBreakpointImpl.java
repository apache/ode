/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
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
