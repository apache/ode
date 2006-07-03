/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime.breaks;

import com.fs.pxe.bpel.bdi.breaks.ActivityBreakpoint;
import com.fs.pxe.bpel.evt.ActivityExecStartEvent;
import com.fs.pxe.bpel.evt.BpelEvent;
import com.fs.pxe.bpel.evt.ScopeCompletionEvent;

public class ActivityBreakpointImpl extends BreakpointImpl implements ActivityBreakpoint {
    private static final long serialVersionUID = -8717519287041871427L;
    private String _activityName;

	public ActivityBreakpointImpl(String uuid, String activityName) {
		super(uuid);
    _activityName = activityName;
	}

  /**
	 * @see com.fs.pxe.bpel.bdi.breaks.ActivityBreakpoint#activityName()
	 */
	public String activityName() {
		return _activityName;
	}

  /**
	 * @see com.fs.pxe.bpel.runtime.breaks.BreakpointImpl#checkBreak(com.fs.pxe.bpel.evt.BpelEvent)
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
