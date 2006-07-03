/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.o.OActivity;
import com.fs.pxe.bpel.runtime.channels.ParentScopeChannel;
import com.fs.pxe.bpel.runtime.channels.TerminationChannel;

import java.io.Serializable;

class ActivityInfo implements Serializable {

	private static final long serialVersionUID = 1L;
  /** Activity instance identifier */
  long aId;
  
  /** Activity definition. */
	OActivity o;
  TerminationChannel self;
  ParentScopeChannel parent;
  
  ActivityInfo(long aid, OActivity o, TerminationChannel self, ParentScopeChannel parent) {
    assert o != null;
    assert self != null;
    assert parent != null;
    
    this.o = o;
    this.self = self;
    this.parent = parent;
    this.aId = aid;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer("(");
    buf.append(o);
    buf.append(',');
    buf.append(self);
    buf.append(',');
    buf.append(parent);
    buf.append(')');
    return buf.toString();
  }
  
  public int hashCode() {
    return (int)aId;
  }
}
