/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import org.apache.ode.utils.ObjectPrinter;

import java.io.Serializable;


/**
 * Base class for compiled BPEL objects.
 */
public class OBase implements Serializable {
  
  static final long serialVersionUID = -1L  ;
  
  /** Our identifier, in terms of our parent. */
  private final int _id;
  private final OProcess _owner;

  public DebugInfo debugInfo;

  protected OBase(OProcess owner) {
    _owner = owner;
    if (owner == null) {
      _id = 0;
    } else {
      _id = ++_owner._childIdCounter;
      _owner._children.add(this);
    }
    assert _id == 0 || _owner != null;
  }

  public OProcess getOwner() {
    return (OProcess) (_owner == null ? this : _owner);
  }

  public int hashCode() {
    return _id;
  }

  public boolean equals(Object obj) {
    if(!(obj instanceof OBase))
      return false;
    
    OBase other = (OBase) obj;
    return (_id == 0 && other._id == 0) || _id == other._id && other._owner.equals(_owner);
  }

  public int getId() {
    return _id;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer(ObjectPrinter.getShortClassName(this));
    buf.append('#');
    buf.append(_id);
    return buf.toString();
  }
}
