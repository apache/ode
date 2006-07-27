/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.evt;

/**
 * Event indicating the start (post creation) of a new process instance.
 */
public class ProcessInstanceStateChangeEvent extends ProcessInstanceEvent {
  private static final long serialVersionUID = 5145501369806670539L;
  private short _oldState;
  private short _newState;

  public ProcessInstanceStateChangeEvent() {
    super();
  }
  
  public short getOldState() {
    return _oldState;
  }

  public void setOldState(short state) {
    _oldState = state;
  }

  public short getNewState() {
    return _newState;
  }

  public void setNewState(short state) {
    _newState = state;
  }

}
