/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;


/**
 * Base class for process instances events.
 */
public abstract class ProcessInstanceEvent extends ProcessEvent {
  
  private Long _pid;

  public ProcessInstanceEvent() {
    super();
  }

  public ProcessInstanceEvent(Long processInstanceId) {
    _pid = processInstanceId;
  }


  /**
   * Get the process instance identifier of the process instnace that generated this
   * event.
   * @return process instance identiifier
   */
  public Long getProcessInstanceId() {
    return _pid;
  }

  public void setProcessInstanceId(Long pid) {
    _pid = pid;
  }

  public TYPE getType() {
    return TYPE.instanceLifecycle;
  }

}
