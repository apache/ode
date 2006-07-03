/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime.breaks;

import com.fs.pxe.bpel.bdi.breaks.Breakpoint;
import com.fs.pxe.bpel.evt.BpelEvent;

import java.io.Serializable;

public abstract class BreakpointImpl implements Breakpoint, Serializable {
	
  private String _uuid;
  private boolean _enabled = true;

	public BreakpointImpl(String uuid) {
		_uuid = uuid;
	}

  /**
	 * @see com.fs.pxe.bpel.bdi.breaks.Breakpoint#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}

  /**
	 * @see com.fs.pxe.bpel.bdi.breaks.Breakpoint#isEnabled()
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

